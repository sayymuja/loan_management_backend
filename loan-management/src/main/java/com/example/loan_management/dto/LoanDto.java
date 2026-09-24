package com.example.loan_management.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class LoanDto {

    private Long id;

    private Long voAlfId;

    private String groupName;

    private String womanName;

    private Double loanAmount;

    private String loanPurpose;

    private LocalDate loanGivenDate;

    private Integer repaymentPeriodMonths;

    private Double interestRate;

    private Integer serialNo;

}