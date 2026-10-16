
package com.example.loan_management.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class AuditReportDto {

    private String reportTitle;
    private String periodLabel;
    private LocalDateTime generatedAt;

    private List<AuditLine> lines = new ArrayList<>();
    private List<String> exceptions = new ArrayList<>();

    private int pendingApprovals;
    private boolean isReconciled;
    private String message;

    // Consolidated totals
    private BigDecimal totalOpeningBalance = BigDecimal.ZERO;
    private BigDecimal totalReceipts = BigDecimal.ZERO;
    private BigDecimal totalPayments = BigDecimal.ZERO;
    private BigDecimal totalClosingBalance = BigDecimal.ZERO;

    private BigDecimal totalLoanDisbursement = BigDecimal.ZERO;
    private BigDecimal totalPrincipalRecovered = BigDecimal.ZERO;
    private BigDecimal totalInterestReceived = BigDecimal.ZERO;
    private BigDecimal totalPenaltyReceived = BigDecimal.ZERO;

    @Data
    public static class AuditLine {

        private Long cmrcId;
        private String cmrcName;

        private String villageName;
        private Long voAlfId;
        private String voAlfName;
        private String accountNo;

        private BigDecimal openingBalance = BigDecimal.ZERO;
        private BigDecimal receipts = BigDecimal.ZERO;
        private BigDecimal payments = BigDecimal.ZERO;
        private BigDecimal closingBalance = BigDecimal.ZERO;

        private BigDecimal loanDisbursement = BigDecimal.ZERO;
        private BigDecimal principalRecovered = BigDecimal.ZERO;
        private BigDecimal interestReceived = BigDecimal.ZERO;
        private BigDecimal penaltyReceived = BigDecimal.ZERO;

        private BigDecimal currentVoAlfBalance = BigDecimal.ZERO;
        private BigDecimal fundAccountBalance = BigDecimal.ZERO;

        // Receipt categories
        private BigDecimal fundReceived = BigDecimal.ZERO;
        private BigDecimal bankFundReceived = BigDecimal.ZERO;
        private BigDecimal loanRepayment = BigDecimal.ZERO;
        private BigDecimal otherIncome = BigDecimal.ZERO;

        // Payment categories
        private BigDecimal bankCharges = BigDecimal.ZERO;
        private BigDecimal smsCharges = BigDecimal.ZERO;
        private BigDecimal recordFees = BigDecimal.ZERO;
        private BigDecimal accountantExpense = BigDecimal.ZERO;
        private BigDecimal cmrcPayment = BigDecimal.ZERO;
        private BigDecimal kitExpense = BigDecimal.ZERO;
        private BigDecimal otherExpense = BigDecimal.ZERO;
        private BigDecimal transferToCmrc = BigDecimal.ZERO;

        private int pendingApprovals;
        private List<String> exceptions = new ArrayList<>();

        // Detailed hierarchy
        private List<GroupAudit> groups = new ArrayList<>();
        private List<TransactionAudit> transactions = new ArrayList<>();
    }

    @Data
    public static class GroupAudit {

        private Long groupId;
        private String groupName;
        private String villageName;

        private BigDecimal totalLoanDisbursement = BigDecimal.ZERO;
        private BigDecimal totalPrincipalRecovered = BigDecimal.ZERO;
        private BigDecimal totalInterestReceived = BigDecimal.ZERO;
        private BigDecimal totalPenaltyReceived = BigDecimal.ZERO;
        private BigDecimal totalOutstanding = BigDecimal.ZERO;

        private List<WomanAudit> women = new ArrayList<>();
    }

    @Data
    public static class WomanAudit {

        private Long womanId;
        private String womanName;
        private String husbandName;
        private String mobileNo;
        private String status;

        private BigDecimal totalLoanAmount = BigDecimal.ZERO;
        private BigDecimal totalPrincipalRecovered = BigDecimal.ZERO;
        private BigDecimal totalInterestPaid = BigDecimal.ZERO;
        private BigDecimal totalPenaltyPaid = BigDecimal.ZERO;
        private BigDecimal totalOutstanding = BigDecimal.ZERO;

        private List<LoanAudit> loans = new ArrayList<>();
    }

    @Data
    public static class LoanAudit {

        private Long loanId;
        private LocalDate loanGivenDate;
        private String loanPurpose;
        private String loanStatus;
        private String interestType;

        private BigDecimal sanctionedAmount = BigDecimal.ZERO;
        private BigDecimal processingFee = BigDecimal.ZERO;
        private BigDecimal disbursedAmount = BigDecimal.ZERO;
        private BigDecimal interestRate = BigDecimal.ZERO;
        private Integer repaymentPeriodMonths;

        private BigDecimal scheduledAmount = BigDecimal.ZERO;
        private BigDecimal totalPaid = BigDecimal.ZERO;
        private BigDecimal principalRecovered = BigDecimal.ZERO;
        private BigDecimal interestReceived = BigDecimal.ZERO;
        private BigDecimal penaltyReceived = BigDecimal.ZERO;
        private BigDecimal outstandingAmount = BigDecimal.ZERO;

        private List<EmiAudit> installments = new ArrayList<>();
    }

    @Data
    public static class EmiAudit {

        private Long repaymentId;
        private Integer installmentNo;

        private LocalDate installmentDate;
        private LocalDate repaymentDate;

        private BigDecimal scheduledAmount = BigDecimal.ZERO;
        private BigDecimal paidAmount = BigDecimal.ZERO;
        private BigDecimal remainingAmount = BigDecimal.ZERO;

        private BigDecimal principalAmount = BigDecimal.ZERO;
        private BigDecimal interestAmount = BigDecimal.ZERO;
        private BigDecimal penaltyAmount = BigDecimal.ZERO;

        private String paymentStatus;
        private Boolean regularRepayment;
        private String remark;
    }

    @Data
    public static class TransactionAudit {

        private Long transactionId;
        private LocalDate transactionDate;

        private String transactionType;
        private String category;
        private String approvalStatus;

        private BigDecimal amount = BigDecimal.ZERO;

        private String remark;
        private String referenceType;
        private Long referenceId;
    }
}
