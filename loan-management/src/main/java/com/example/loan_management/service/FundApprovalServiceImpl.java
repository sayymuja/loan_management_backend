        package com.example.loan_management.service;

import com.example.loan_management.auth.entity.User;
import com.example.loan_management.auth.service.CurrentUserService;
import com.example.loan_management.entity.FundAccount;
import com.example.loan_management.entity.FundApprovalRequest;
import com.example.loan_management.entity.FundTransaction;
import com.example.loan_management.repository.FundAccountRepository;
import com.example.loan_management.repository.FundApprovalRequestRepository;
import com.example.loan_management.repository.FundTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
public class FundApprovalServiceImpl implements FundApprovalService {

    private final FundApprovalRequestRepository approvalRepository;
    private final FundTransactionRepository transactionRepository;
    private final FundAccountRepository accountRepository;
    private final FundManagementServiceImpl fundManagementService;
    private final CurrentUserService currentUserService;

    // =========================================================
    // GET PENDING REQUESTS - ADMIN
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public List<FundApprovalRequest> getPendingRequests() {

        validateAdmin();

        Long cmrcId = currentUserService.getCurrentCmrcId();

        if (cmrcId == null) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "CMRC is not associated with the logged-in user."
            );
        }

        return approvalRepository
                .findByCmrcIdAndStatusOrderByCreatedAtDesc(
                        cmrcId,
                        "PENDING"
                );
    }

    // =========================================================
    // GET MY REQUESTS
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public List<FundApprovalRequest> getMyRequests() {

        Long userId = currentUserService.getCurrentUserId();

        if (userId == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "User is not authenticated."
            );
        }

        return approvalRepository
                .findByRequestedBy_IdOrderByCreatedAtDesc(userId);
    }

    // =========================================================
    // APPROVE FUND REQUEST - ADMIN
    // =========================================================

    @Override
    public FundApprovalRequest approveRequest(
            Long requestId,
            String adminRemarks
    ) {

        validateAdmin();

        FundApprovalRequest approval = getAuthorizedRequest(requestId);

        if (!"PENDING".equalsIgnoreCase(approval.getStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "This request has already been reviewed."
            );
        }

        if (!"TRANSACTION".equalsIgnoreCase(approval.getRequestType())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Unsupported fund approval request type."
            );
        }

        FundTransaction transaction = transactionRepository
                .findByApprovalRequestId(requestId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Fund transaction not found for this request."
                ));

        if (transaction.getApprovalStatus()
                != FundTransaction.ApprovalStatus.PENDING) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Fund transaction is not pending."
            );
        }

        // Validate that the transaction belongs to the ADMIN's CMRC.
        FundAccount account = transaction.getFundAccount();

        if (account == null
                || account.getVoAlf() == null
                || account.getVoAlf().getCmrc() == null
                || !Objects.equals(
                approval.getCmrcId(),
                account.getVoAlf().getCmrc().getId()
        )) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Transaction and approval request do not match."
            );
        }

        /*
         * Update the single FundAccount balance only after approval.
         * FundManagementServiceImpl is responsible for applying
         * the transaction to the account balance.
         *
         * If balance validation fails, the transaction rolls back.
         */
        fundManagementService.applyBalanceChange(transaction);

        transaction.setApprovalStatus(
                FundTransaction.ApprovalStatus.APPROVED
        );

        transactionRepository.save(transaction);

        approval.setStatus("APPROVED");
        approval.setReviewedBy(currentUserService.getCurrentUser());
        approval.setReviewedAt(LocalDateTime.now());
        approval.setAdminRemarks(adminRemarks);

        return approvalRepository.save(approval);
    }

    // =========================================================
    // REJECT FUND REQUEST - ADMIN
    // =========================================================

    @Override
    public FundApprovalRequest rejectRequest(
            Long requestId,
            String adminRemarks
    ) {

        validateAdmin();

        FundApprovalRequest approval = getAuthorizedRequest(requestId);

        if (!"PENDING".equalsIgnoreCase(approval.getStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "This request has already been reviewed."
            );
        }

        if ("TRANSACTION".equalsIgnoreCase(approval.getRequestType())) {

            transactionRepository.findByApprovalRequestId(requestId)
                    .ifPresent(transaction -> {

                        if (transaction.getApprovalStatus()
                                == FundTransaction.ApprovalStatus.PENDING) {

                            transaction.setApprovalStatus(
                                    FundTransaction.ApprovalStatus.REJECTED
                            );

                            transactionRepository.save(transaction);
                        }
                    });
        }

        // Rejection must not change the fund account balance.
        approval.setStatus("REJECTED");
        approval.setReviewedBy(currentUserService.getCurrentUser());
        approval.setReviewedAt(LocalDateTime.now());
        approval.setAdminRemarks(adminRemarks);

        return approvalRepository.save(approval);
    }

    // =========================================================
    // GET AUTHORIZED REQUEST
    // =========================================================

    private FundApprovalRequest getAuthorizedRequest(Long requestId) {

        Long cmrcId = currentUserService.getCurrentCmrcId();

        if (requestId == null || cmrcId == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Request ID and CMRC are required."
            );
        }

        return approvalRepository
                .findByIdAndCmrcId(requestId, cmrcId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Fund approval request not found."
                ));
    }

    // =========================================================
    // ADMIN VALIDATION
    // =========================================================

    private void validateAdmin() {

        String role = currentUserService.getCurrentRole();

        if (role == null
                || !"ADMIN".equals(
                role.trim()
                        .toUpperCase(Locale.ROOT)
                        .replace("ROLE_", "")
        )) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only ADMIN can review fund approval requests."
            );
        }
    }
}
