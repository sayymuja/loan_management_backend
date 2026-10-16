
package com.example.loan_management.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "fund_transaction",
        indexes = {
                @Index(
                        name = "idx_fund_tx_account_date",
                        columnList = "fund_account_id, transaction_date"
                ),
                @Index(
                        name = "idx_fund_tx_status",
                        columnList = "approval_status"
                )
        }
)
@Data
public class FundTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fund_account_id", nullable = false)
    private FundAccount fundAccount;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, length = 20)
    private TransactionType transactionType;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 50)
    private FundCategory category;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(name = "transaction_date", nullable = false)
    private LocalDate transactionDate;

    @Column(length = 500)
    private String remark;

    @Column(name = "reference_type", length = 30)
    private String referenceType;

    @Column(name = "reference_id")
    private Long referenceId;

    @Column(name = "approval_request_id")
    private Long approvalRequestId;

    @Enumerated(EnumType.STRING)
    @Column(name = "approval_status", nullable = false, length = 20)
    private ApprovalStatus approvalStatus = ApprovalStatus.PENDING;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "payment_event_id", length = 100)
    private String paymentEventId;

    @PrePersist
    protected void onCreate() {
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalStateException(
                    "Transaction amount must be greater than zero"
            );
        }

        if (transactionType == null) {
            throw new IllegalStateException(
                    "Transaction type is required"
            );
        }

        if (category == null) {
            throw new IllegalStateException(
                    "Transaction category is required"
            );
        }

        if (transactionDate == null) {
            transactionDate = LocalDate.now();
        }

        if (approvalStatus == null) {
            approvalStatus = ApprovalStatus.PENDING;
        }

        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public enum TransactionType {
        RECEIPT,
        PAYMENT
    }

    public enum ApprovalStatus {
        PENDING,
        APPROVED,
        REJECTED
    }

    public enum FundCategory {
        OPENING_BALANCE,

        FUND_RECEIVED,
        BANK_FUND_RECEIVED,

        LOAN_REPAYMENT,
        INTEREST_RECEIVED,
        PENALTY_RECEIVED,
        OTHER_INCOME,

        LOAN_DISBURSEMENT,

        BANK_CHARGES,
        SMS_CHARGES,
        RECORD_FEES,
        ACCOUNTANT_EXPENSE,
        CMRC_PAYMENT,
        KIT_EXPENSE,
        OTHER_EXPENSE,

        // VO/ALF se CMRC ko actual fund transfer
        TRANSFER_TO_CMRC
    }
}
