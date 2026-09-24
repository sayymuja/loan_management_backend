package com.example.loan_management.dto;

import lombok.Data;

@Data
public class ClScheduleDto {

    private Long id;

    private Long loanId;

    private Integer installmentNo;

    private Double outstandingAmount;

    private Double principalAmount;

    private Double interestAmount;

    private Double monthlyInstallment;

    private Double averageMonthlyInstallment;

    private String remark;
}