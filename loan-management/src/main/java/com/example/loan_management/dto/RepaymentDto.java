package com.example.loan_management.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class RepaymentDto {

    private Long id;

    private Long loanId;

    private LocalDate repaymentDate;

    private Double principalAmount;

    private Double interestAmount;

    private Double totalAmount;

    private String regularRepayment;

    private Double penaltyAmount;

    private String remark;

    private Double paidAmount;

    private Integer installmentNo;

    private LocalDate installmentDate;

    private Double scheduledAmount;

    private String paymentStatus;

}