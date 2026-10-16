
package com.example.loan_management.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;

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

        private int pendingApprovals;
        private List<String> exceptions = new ArrayList<>();
    }
}
