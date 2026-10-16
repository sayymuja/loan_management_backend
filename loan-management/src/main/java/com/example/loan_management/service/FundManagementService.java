        package com.example.loan_management.service;

import com.example.loan_management.entity.FundAccount;
import com.example.loan_management.entity.FundTransaction;
import com.example.loan_management.dto.FundTransactionRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface FundManagementService {

    FundAccount getOrCreateAccount(Long voAlfId);

    FundAccount getAccount(Long voAlfId);

    FundTransaction submitTransaction(FundTransactionRequest request);

    List<FundTransaction> getTransactions(Long voAlfId);

    /**
     * Record an approved internal transaction, such as an EMI receipt
     * or a loan disbursement.
     *
     * The implementation must prevent duplicate balance updates.
     */
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

    FundTransaction getTransactionByPaymentEventId(String paymentEventId);
}
