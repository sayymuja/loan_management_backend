
package com.example.loan_management.service;

import com.example.loan_management.dto.FundTransactionRequest;
import com.example.loan_management.entity.FundAccount;
import com.example.loan_management.entity.FundTransaction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface FundManagementService {

    // =========================================================
    // FUND ACCOUNT
    // =========================================================

    FundAccount getOrCreateAccount(Long voAlfId);

    FundAccount getAccount(Long voAlfId);

    // =========================================================
    // FUND RECEIPT / PAYMENT TRANSACTIONS
    // =========================================================

    FundTransaction submitTransaction(
            FundTransactionRequest request
    );

    List<FundTransaction> getTransactions(Long voAlfId);

    // =========================================================
    // FUND APPROVAL REQUESTS
    // =========================================================

    List<Map<String, Object>> getPendingFundApprovals();

    Map<String, Object> approveFundRequest(
            Long requestId,
            String remarks
    );

    Map<String, Object> rejectFundRequest(
            Long requestId,
            String remarks
    );

    // =========================================================
    // INTERNAL APPROVED TRANSACTIONS
    // Prevent duplicate balance updates.
    // =========================================================

    FundTransaction recordInternalTransaction(
            Long voAlfId,
            FundTransaction.TransactionType transactionType,
            FundTransaction.FundCategory category,
            BigDecimal amount,
            LocalDate transactionDate,
            String remark,
            String referenceType,
            Long referenceId
    );

    FundTransaction recordInternalTransaction(
            Long voAlfId,
            FundTransaction.TransactionType transactionType,
            FundTransaction.FundCategory category,
            BigDecimal amount,
            LocalDate transactionDate,
            String remark,
            String referenceType,
            Long referenceId,
            String paymentEventId
    );

    FundTransaction getTransactionByPaymentEventId(
            String paymentEventId
    );
}
