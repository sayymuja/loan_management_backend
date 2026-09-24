package com.example.loan_management.dto;

import lombok.Data;

@Data
public class RepaymentSummaryDto {

    private Long loanId;

    private Double loanAmount;

    private Double totalPrincipalPaid;

    private Double totalInterestPaid;

    private Double totalRepayment;

    private Double outstandingAmount;

    private Double totalPenalty;
}