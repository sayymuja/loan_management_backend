
package com.example.loan_management.service;

import com.example.loan_management.auth.entity.User;
import com.example.loan_management.auth.service.CurrentUserService;
import com.example.loan_management.entity.Cmrc;
import com.example.loan_management.entity.FundAccount;
import com.example.loan_management.entity.FundApprovalRequest;
import com.example.loan_management.entity.FundTransaction;
import com.example.loan_management.entity.VoAlf;
import com.example.loan_management.dto.FundTransactionRequest;
import com.example.loan_management.repository.CmrcRepository;
import com.example.loan_management.repository.FundAccountRepository;
import com.example.loan_management.repository.FundApprovalRequestRepository;
import com.example.loan_management.repository.FundTransactionRepository;
import com.example.loan_management.repository.VoAlfRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Transactional
public class FundManagementServiceImpl implements FundManagementService {

    private final FundAccountRepository fundAccountRepository;
    private final FundTransactionRepository fundTransactionRepository;
    private final FundApprovalRequestRepository fundApprovalRequestRepository;
    private final VoAlfRepository voAlfRepository;
    private final CmrcRepository cmrcRepository;
    private final CurrentUserService currentUserService;
    private final ObjectMapper objectMapper;

    private static final BigDecimal ZERO = BigDecimal.ZERO;

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
                                    : BigDecimal.valueOf(voAlf.getReceivedFund());

                    account.setOpeningBalance(existingBalance);
                    account.setBalance(existingBalance);

                    return fundAccountRepository.save(account);
                });
    }

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

    @Override
    public FundTransaction submitTransaction(
            FundTransactionRequest request
    ) {
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
        String role = normalizeRole(currentUserService.getCurrentRole());

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
        transaction.setCreatedBy(currentUserService.getCurrentUserId());

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
                throw new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Unable to create fund approval request"
                );
            }

            FundApprovalRequest savedApproval =
                    fundApprovalRequestRepository.save(approval);

            transaction.setApprovalRequestId(savedApproval.getId());
            transaction.setApprovalStatus(
                    FundTransaction.ApprovalStatus.PENDING
            );

            // Pending transaction does not change either balance.
            return fundTransactionRepository.save(transaction);
        }

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

    @Override
    @Transactional(readOnly = true)
    public List<FundTransaction> getTransactions(Long voAlfId) {
        FundAccount account = getAccount(voAlfId);

        return fundTransactionRepository
                .findByFundAccountIdOrderByTransactionDateDescIdDesc(
                        account.getId()
                );
    }

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

        if (category == FundTransaction.FundCategory.OPENING_BALANCE) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Opening balance requires a separate operation"
            );
        }

        if (request.getTransactionType()
                == FundTransaction.TransactionType.RECEIPT) {

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

        } else {
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
        }
    }

    @Override
    public FundTransaction recordInternalTransaction(
            Long voAlfId,
            FundTransaction.TransactionType transactionType,
            FundTransaction.FundCategory category,
            BigDecimal amount,
            LocalDate transactionDate,
            String remark,
            String referenceType,
            Long referenceId
    ) {
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
            String paymentEventId
    ) {
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
                transactionDate != null ? transactionDate : LocalDate.now()
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

    /*
     * Called for an ADMIN transaction or a pending transaction
     * that has passed ADMIN approval checks.
     *
     * CMRC transfers update both balances in the same transaction.
     * Ordinary VO/ALF receipts and payments do not change CMRC.
     */
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
                account.getBalance() != null ? account.getBalance() : ZERO;

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

        // Only actual CMRC-to-VO/ALF or VO/ALF-to-CMRC transfers
        // change CMRC.totalFund.
        if (addFundTransfer || returnToCmrc) {
            Cmrc cmrc = cmrcRepository.findById(voAlf.getCmrc().getId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "CMRC not found for this VO/ALF"
                    ));

            BigDecimal cmrcBalance = cmrc.getTotalFund() == null
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

    private String normalizeRole(String role) {
        if (role == null) {
            return "";
        }

        return role.trim()
                .toUpperCase(Locale.ROOT)
                .replace("ROLE_", "");
    }

    @Override
    @Transactional
    public FundTransaction getTransactionByPaymentEventId(
            String paymentEventId
    ) {
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
