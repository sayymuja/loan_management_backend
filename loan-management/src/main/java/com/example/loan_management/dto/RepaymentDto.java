package com.example.loan_management.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class RepaymentDto {

    private Long id;

    private Long loanId;

    private LocalDate paymentDate;

    private Double principalAmount;

    private Double interestAmount;

    private Double totalAmount;

    private Boolean regularRepayment;

    private Double penaltyAmount;

    private String remark;
}