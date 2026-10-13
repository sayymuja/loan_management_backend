
package com.example.loan_management.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
public class LoanApprovalRequestDto {

    private Long id;

    private String requestType; // CREATE, UPDATE, DELETE

    private String status; // PENDING, APPROVED, REJECTED

    private Long loanId;

    private String requestData;

    private Long requestedByUserId;

    private String requestedByName;

    private Long cmrcId;

    private Long reviewedByUserId;

    private String reviewedByName;

    private LocalDateTime createdAt;

    private LocalDateTime reviewedAt;

    private String adminRemarks;

    private Map<String, Object> repaymentDetails;
    private Map<String, Object> linkedLoanDetails;
}
