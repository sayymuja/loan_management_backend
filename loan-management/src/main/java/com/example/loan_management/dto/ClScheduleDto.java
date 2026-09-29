package com.example.loan_management.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class ClScheduleDto {

    private Long id;

    private Long loanId;

    private Integer installmentNo;

    private LocalDate installmentDate;

    private Double outstandingAmount;

    private Double principalAmount;

    private Double interestAmount;

    private Double monthlyInstallment;

    private Double averageMonthlyInstallment;

    private Double closingBalance;

    private String remark;
}