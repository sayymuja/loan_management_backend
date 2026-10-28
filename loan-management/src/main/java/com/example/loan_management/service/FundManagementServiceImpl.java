
package com.example.loan_management.service;

import com.example.loan_management.auth.entity.User;
import com.example.loan_management.auth.service.CurrentUserService;
import com.example.loan_management.dto.FundTransactionRequest;
import com.example.loan_management.entity.Cmrc;
import com.example.loan_management.entity.FundAccount;
import com.example.loan_management.entity.FundApprovalRequest;
import com.example.loan_management.entity.FundTransaction;
import com.example.loan_management.entity.VoAlf;
import com.example.loan_management.repository.CmrcRepository;
import com.example.loan_management.repository.FundAccountRepository;
import com.example.loan_management.repository.FundApprovalRequestRepository;
import com.example.loan_management.repository.FundTransactionRepository;
import com.example.loan_management.repository.VoAlfRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class FundManagementServiceImpl implements FundManagementService {

    private static final Logger log =
            LoggerFactory.getLogger(FundManagementServiceImpl.class);

    private static final BigDecimal ZERO = BigDecimal.ZERO;

    private final FundAccountRepository fundAccountRepository;
    private final FundTransactionRepository fundTransactionRepository;
    private final FundApprovalRequestRepository fundApprovalRequestRepository;
    private final VoAlfRepository voAlfRepository;
    private final CmrcRepository cmrcRepository;
    private final CurrentUserService currentUserService;
    private final ObjectMapper objectMapper;

    // =========================================================
    // GET OR CREATE FUND ACCOUNT
    // =========================================================

    @Override
    public FundAccount getOrCreateAccount(Long voAlfId) {

        VoAlf voAlf = getAuthorizedVoAlf(voAlfId);

        return fundAccountRepository.findByVoAlfId(voAlfId)
                .orElseGet(() -> {
                    FundAccount account = new FundAccount();
                    account.setVoAlf(voAlf);

                    BigDecimal existingBalance =
                            voAlf.getReceivedFund() == null
                                    ? ZERO
                                    : BigDecimal.valueOf(
                                    voAlf.getReceivedFund()
                            );

                    account.setOpeningBalance(existingBalance);
                    account.setBalance(existingBalance);

                    return fundAccountRepository.save(account);
                });
    }

    // =========================================================
    // GET EXISTING ACCOUNT
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public FundAccount getAccount(Long voAlfId) {

        getAuthorizedVoAlf(voAlfId);

        return fundAccountRepository.findByVoAlfId(voAlfId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Fund account not found for this VO/ALF"
                ));
    }

    // =========================================================
    // SUBMIT FUND TRANSACTION
    // =========================================================

    @Override
    public FundTransaction submitTransaction(
            FundTransactionRequest request) {

        if (request == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Transaction request is required"
            );
        }

        validateRequest(request);

        VoAlf voAlf = getAuthorizedVoAlf(request.getVoAlfId());
        FundAccount account = getOrCreateAccount(voAlf.getId());
        User currentUser = currentUserService.getCurrentUser();

        if (currentUser == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Logged-in user not found"
            );
        }

        String role = normalizeRole(
                currentUserService.getCurrentRole()
        );

        FundTransaction transaction = new FundTransaction();

        transaction.setFundAccount(account);
        transaction.setTransactionType(request.getTransactionType());
        transaction.setCategory(request.getCategory());
        transaction.setAmount(request.getAmount());
        transaction.setTransactionDate(
                request.getTransactionDate() != null
                        ? request.getTransactionDate()
                        : LocalDate.now()
        );
        transaction.setRemark(request.getRemark());
        transaction.setCreatedBy(currentUser.getId());

        // USER: submit for ADMIN approval
        if ("USER".equals(role)) {

            FundApprovalRequest approval = new FundApprovalRequest();

            approval.setRequestType("TRANSACTION");
            approval.setStatus("PENDING");
            approval.setCmrcId(voAlf.getCmrc().getId());
            approval.setRequestedBy(currentUser);

            try {
                approval.setRequestData(
                        objectMapper.writeValueAsString(request)
                );
            } catch (JsonProcessingException exception) {
                log.error(
                        "Fund request JSON serialization failed. User ID: {}",
                        currentUser.getId(),
                        exception
                );

                throw new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Unable to serialize fund approval request",
                        exception
                );
            }

            FundApprovalRequest savedApproval =
                    fundApprovalRequestRepository.save(approval);

            transaction.setApprovalRequestId(savedApproval.getId());
            transaction.setApprovalStatus(
                    FundTransaction.ApprovalStatus.PENDING
            );

            // No balance changes before approval.
            return fundTransactionRepository.save(transaction);
        }

        // ADMIN: process directly
        if ("ADMIN".equals(role)) {
            transaction.setApprovalStatus(
                    FundTransaction.ApprovalStatus.APPROVED
            );

            applyBalanceChange(transaction);

            return fundTransactionRepository.save(transaction);
        }

        throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "You are not authorized to create fund transactions"
        );
    }

    // =========================================================
    // GET TRANSACTIONS
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public List<FundTransaction> getTransactions(Long voAlfId) {

        FundAccount account = getAccount(voAlfId);

        return fundTransactionRepository
                .findByFundAccountIdOrderByTransactionDateDescIdDesc(
                        account.getId()
                );
    }

    // =========================================================
    // GET PENDING FUND APPROVALS
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getPendingFundApprovals() {

        requireAdmin();

        Long cmrcId = currentUserService.getCurrentCmrcId();

        if (cmrcId == null) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Your account is not linked to a CMRC"
            );
        }

        List<FundApprovalRequest> approvals =
                fundApprovalRequestRepository
                        .findByCmrcIdAndStatusOrderByCreatedAtDesc(
                                cmrcId,
                                "PENDING"
                        );

        List<Map<String, Object>> result = new ArrayList<>();

        for (FundApprovalRequest approval : approvals) {

            FundTransaction transaction =
                    fundTransactionRepository
                            .findByApprovalRequestId(approval.getId())
                            .orElse(null);

            // Ignore approval records without a pending transaction.
            if (transaction == null
                    || transaction.getApprovalStatus()
                    != FundTransaction.ApprovalStatus.PENDING) {
                continue;
            }

            Map<String, Object> item = new LinkedHashMap<>();

            item.put("id", approval.getId());
            item.put("requestType", approval.getRequestType());
            item.put("status", approval.getStatus());
            item.put("requestData", approval.getRequestData());
            item.put("cmrcId", approval.getCmrcId());
            item.put("createdAt", approval.getCreatedAt());
            item.put(
                    "requestedByUserId",
                    approval.getRequestedBy() != null
                            ? approval.getRequestedBy().getId()
                            : null
            );
            item.put(
                    "requestedByName",
                    approval.getRequestedBy() != null
                            ? approval.getRequestedBy().getName()
                            : "-"
            );
            item.put("adminRemarks", approval.getAdminRemarks());

            item.put("transactionId", transaction.getId());
            item.put("transactionType", transaction.getTransactionType());
            item.put("category", transaction.getCategory());
            item.put("amount", transaction.getAmount());
            item.put("transactionDate", transaction.getTransactionDate());
            item.put("remark", transaction.getRemark());

            if (transaction.getFundAccount() != null
                    && transaction.getFundAccount().getVoAlf() != null) {

                VoAlf voAlf = transaction.getFundAccount().getVoAlf();

                item.put("voAlfId", voAlf.getId());
                item.put("voAlfName", voAlf.getVoAlfName());
                item.put("villageName", voAlf.getVillageName());
            }

            result.add(item);
        }

        return result;
    }

    // =========================================================
    // APPROVE FUND REQUEST
    // =========================================================

    @Override
    public Map<String, Object> approveFundRequest(
            Long requestId,
            String remarks) {

        return reviewFundRequest(requestId, true, remarks);
    }

    // =========================================================
    // REJECT FUND REQUEST
    // =========================================================

    @Override
    public Map<String, Object> rejectFundRequest(
            Long requestId,
            String remarks) {

        return reviewFundRequest(requestId, false, remarks);
    }

    // =========================================================
    // REVIEW FUND REQUEST
    // =========================================================

    private Map<String, Object> reviewFundRequest(
            Long requestId,
            boolean approve,
            String remarks) {

        requireAdmin();

        if (requestId == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Fund approval request ID is required"
            );
        }

        Long cmrcId = currentUserService.getCurrentCmrcId();

        if (cmrcId == null) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Your account is not linked to a CMRC"
            );
        }

        FundApprovalRequest approval =
                fundApprovalRequestRepository
                        .findByIdAndCmrcIdForUpdate(requestId, cmrcId)
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Fund approval request not found for your CMRC"
                        ));

        if (!"PENDING".equalsIgnoreCase(approval.getStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This fund request has already been reviewed"
            );
        }

        FundTransaction transaction =
                fundTransactionRepository
                        .findByApprovalRequestIdForUpdate(requestId)
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Linked fund transaction not found"
                        ));

        if (transaction.getApprovalStatus()
                != FundTransaction.ApprovalStatus.PENDING) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Linked fund transaction is not pending"
            );
        }

        User reviewer = currentUserService.getCurrentUser();

        if (reviewer == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Logged-in admin not found"
            );
        }

        if (approve) {

            transaction.setApprovalStatus(
                    FundTransaction.ApprovalStatus.APPROVED
            );

            // The balance update and review are in one transaction.
            applyBalanceChange(transaction);

            fundTransactionRepository.save(transaction);
            approval.setStatus("APPROVED");

        } else {

            // Rejection does not modify any balance.
            transaction.setApprovalStatus(
                    FundTransaction.ApprovalStatus.REJECTED
            );

            fundTransactionRepository.save(transaction);
            approval.setStatus("REJECTED");
        }

        approval.setReviewedBy(reviewer);
        approval.setReviewedAt(LocalDateTime.now());
        approval.setAdminRemarks(
                remarks == null || remarks.isBlank()
                        ? (approve
                        ? "Approved by Admin"
                        : "Rejected by Admin")
                        : remarks.trim()
        );

        fundApprovalRequestRepository.save(approval);

        Map<String, Object> response = new LinkedHashMap<>();

        response.put("id", approval.getId());
        response.put("requestType", approval.getRequestType());
        response.put("status", approval.getStatus());
        response.put("transactionId", transaction.getId());
        response.put("transactionType", transaction.getTransactionType());
        response.put("category", transaction.getCategory());
        response.put("amount", transaction.getAmount());
        response.put("reviewedAt", approval.getReviewedAt());
        response.put("adminRemarks", approval.getAdminRemarks());
        response.put(
                "message",
                approve
                        ? "Fund request approved successfully"
                        : "Fund request rejected successfully"
        );

        return response;
    }

    // =========================================================
    // ADMIN ROLE CHECK
    // =========================================================

    private void requireAdmin() {

        String role = normalizeRole(
                currentUserService.getCurrentRole()
        );

        if (!"ADMIN".equals(role)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only ADMIN can review fund approval requests"
            );
        }
    }

    // =========================================================
    // AUTHORIZE VO/ALF BY LOGGED-IN USER'S CMRC
    // =========================================================

    private VoAlf getAuthorizedVoAlf(Long voAlfId) {

        if (voAlfId == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "VO/ALF ID is required"
            );
        }

        VoAlf voAlf = voAlfRepository.findById(voAlfId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "VO/ALF not found"
                ));

        Long currentCmrcId = currentUserService.getCurrentCmrcId();

        if (currentCmrcId == null
                || voAlf.getCmrc() == null
                || !currentCmrcId.equals(voAlf.getCmrc().getId())) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You cannot access another CMRC's VO/ALF"
            );
        }

        return voAlf;
    }

    // =========================================================
    // VALIDATE TRANSACTION REQUEST
    // =========================================================

    private void validateRequest(FundTransactionRequest request) {

        if (request.getVoAlfId() == null
                || request.getTransactionType() == null
                || request.getCategory() == null
                || request.getAmount() == null
                || request.getAmount().compareTo(ZERO) <= 0) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "VO/ALF, transaction type, category and a positive amount are required"
            );
        }

        if (request.getTransactionDate() != null
                && request.getTransactionDate().isAfter(LocalDate.now())) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Transaction date cannot be in the future"
            );
        }

        FundTransaction.FundCategory category = request.getCategory();
        FundTransaction.TransactionType type = request.getTransactionType();

        if (category == FundTransaction.FundCategory.OPENING_BALANCE) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Opening balance requires a separate operation"
            );
        }

        if (type == FundTransaction.TransactionType.RECEIPT) {

            boolean validReceipt = switch (category) {
                case FUND_RECEIVED,
                     BANK_FUND_RECEIVED,
                     LOAN_REPAYMENT,
                     INTEREST_RECEIVED,
                     PENALTY_RECEIVED,
                     OTHER_INCOME -> true;
                default -> false;
            };

            if (!validReceipt) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Invalid category for Receipt"
                );
            }

        } else if (type == FundTransaction.TransactionType.PAYMENT) {

            boolean validPayment = switch (category) {
                case LOAN_DISBURSEMENT,
                     BANK_CHARGES,
                     SMS_CHARGES,
                     RECORD_FEES,
                     ACCOUNTANT_EXPENSE,
                     CMRC_PAYMENT,
                     KIT_EXPENSE,
                     OTHER_EXPENSE,
                     TRANSFER_TO_CMRC -> true;
                default -> false;
            };

            if (!validPayment) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Invalid category for Payment"
                );
            }

        } else {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid transaction type"
            );
        }
    }

    // =========================================================
    // INTERNAL TRANSACTION - WITHOUT PAYMENT EVENT ID
    // =========================================================

    @Override
    public FundTransaction recordInternalTransaction(
            Long voAlfId,
            FundTransaction.TransactionType transactionType,
            FundTransaction.FundCategory category,
            BigDecimal amount,
            LocalDate transactionDate,
            String remark,
            String referenceType,
            Long referenceId) {

        return recordInternalTransaction(
                voAlfId,
                transactionType,
                category,
                amount,
                transactionDate,
                remark,
                referenceType,
                referenceId,
                null
        );
    }

    // =========================================================
    // INTERNAL TRANSACTION - IDEMPOTENT PAYMENT EVENT
    // =========================================================

    @Override
    public FundTransaction recordInternalTransaction(
            Long voAlfId,
            FundTransaction.TransactionType transactionType,
            FundTransaction.FundCategory category,
            BigDecimal amount,
            LocalDate transactionDate,
            String remark,
            String referenceType,
            Long referenceId,
            String paymentEventId) {

        if (voAlfId == null
                || transactionType == null
                || category == null
                || amount == null
                || amount.compareTo(ZERO) <= 0) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Valid VO/ALF, transaction details and positive amount are required"
            );
        }

        VoAlf voAlf = getAuthorizedVoAlf(voAlfId);
        FundAccount account = getOrCreateAccount(voAlf.getId());

        if (paymentEventId != null && !paymentEventId.isBlank()) {

            var existing = fundTransactionRepository.findByPaymentEventId(
                    paymentEventId.trim()
            );

            if (existing.isPresent()) {

                FundTransaction old = existing.get();

                boolean same =
                        old.getFundAccount().getId().equals(account.getId())
                                && old.getTransactionType() == transactionType
                                && old.getCategory() == category
                                && old.getAmount().compareTo(amount) == 0;

                if (!same) {
                    throw new ResponseStatusException(
                            HttpStatus.CONFLICT,
                            "Payment event ID is already used for a different transaction"
                    );
                }

                return old;
            }
        }

        if ((paymentEventId == null || paymentEventId.isBlank())
                && referenceType != null
                && referenceId != null
                && fundTransactionRepository
                .existsByReferenceTypeAndReferenceIdAndCategory(
                        referenceType,
                        referenceId,
                        category
                )) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "A fund transaction already exists for this reference"
            );
        }

        FundTransaction transaction = new FundTransaction();

        transaction.setFundAccount(account);
        transaction.setTransactionType(transactionType);
        transaction.setCategory(category);
        transaction.setAmount(amount);
        transaction.setTransactionDate(
                transactionDate != null
                        ? transactionDate
                        : LocalDate.now()
        );
        transaction.setRemark(remark);
        transaction.setReferenceType(referenceType);
        transaction.setReferenceId(referenceId);

        if (paymentEventId != null && !paymentEventId.isBlank()) {
            transaction.setPaymentEventId(paymentEventId.trim());
        }

        transaction.setCreatedBy(currentUserService.getCurrentUserId());
        transaction.setApprovalStatus(
                FundTransaction.ApprovalStatus.APPROVED
        );

        applyBalanceChange(transaction);

        return fundTransactionRepository.save(transaction);
    }

    // =========================================================
    // APPLY APPROVED TRANSACTION TO BALANCES
    // =========================================================

    void applyBalanceChange(FundTransaction transaction) {

        FundAccount account = transaction.getFundAccount();
        BigDecimal amount = transaction.getAmount();

        if (account == null
                || amount == null
                || amount.compareTo(ZERO) <= 0
                || transaction.getTransactionType() == null
                || transaction.getCategory() == null) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid fund transaction"
            );
        }

        VoAlf voAlf = account.getVoAlf();

        if (voAlf == null || voAlf.getCmrc() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "VO/ALF or its CMRC is missing"
            );
        }

        BigDecimal currentBalance =
                account.getBalance() != null
                        ? account.getBalance()
                        : ZERO;

        boolean receipt =
                transaction.getTransactionType()
                        == FundTransaction.TransactionType.RECEIPT;

        boolean addFundTransfer =
                receipt
                        && (transaction.getCategory()
                        == FundTransaction.FundCategory.FUND_RECEIVED
                        || transaction.getCategory()
                        == FundTransaction.FundCategory.BANK_FUND_RECEIVED);

        boolean returnToCmrc =
                transaction.getTransactionType()
                        == FundTransaction.TransactionType.PAYMENT
                        && transaction.getCategory()
                        == FundTransaction.FundCategory.TRANSFER_TO_CMRC;

        BigDecimal updatedVoAlfBalance;

        if (receipt) {
            updatedVoAlfBalance = currentBalance.add(amount);
        } else {

            if (currentBalance.compareTo(amount) < 0) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Insufficient VO/ALF available balance"
                );
            }

            updatedVoAlfBalance = currentBalance.subtract(amount);
        }

        // CMRC changes only for actual transfers.
        if (addFundTransfer || returnToCmrc) {

            Cmrc cmrc = cmrcRepository.findById(
                    voAlf.getCmrc().getId()
            ).orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "CMRC not found for this VO/ALF"
            ));

            BigDecimal cmrcBalance =
                    cmrc.getTotalFund() == null
                            ? ZERO
                            : BigDecimal.valueOf(cmrc.getTotalFund());

            BigDecimal updatedCmrcBalance;

            if (addFundTransfer) {

                if (cmrcBalance.compareTo(amount) < 0) {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "Insufficient CMRC available balance"
                    );
                }

                updatedCmrcBalance = cmrcBalance.subtract(amount);

            } else {
                updatedCmrcBalance = cmrcBalance.add(amount);
            }

            cmrc.setTotalFund(updatedCmrcBalance.doubleValue());
            cmrcRepository.save(cmrc);
        }

        account.setBalance(updatedVoAlfBalance);
        fundAccountRepository.save(account);

        voAlf.setReceivedFund(updatedVoAlfBalance.doubleValue());
        voAlfRepository.save(voAlf);
    }

    // =========================================================
    // ROLE NORMALIZATION
    // =========================================================

    private String normalizeRole(String role) {

        if (role == null) {
            return "";
        }

        return role.trim()
                .toUpperCase(Locale.ROOT)
                .replace("ROLE_", "");
    }

    // =========================================================
    // GET TRANSACTION BY PAYMENT EVENT ID
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public FundTransaction getTransactionByPaymentEventId(
            String paymentEventId) {

        if (paymentEventId == null || paymentEventId.isBlank()) {
            throw new IllegalArgumentException(
                    "Payment event ID is required"
            );
        }

        return fundTransactionRepository
                .findByPaymentEventId(paymentEventId.trim())
                .orElse(null);
    }
}
