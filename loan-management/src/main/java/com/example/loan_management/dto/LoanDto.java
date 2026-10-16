        package com.example.loan_management.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class LoanDto {

    private Long id;

    // CMRC
    private Long cmrcId;
    private String cmrcName;

    // VO / ALF
    private Long voAlfId;
    private String voAlfName;

    // GROUP
    private Long groupId;
    private String groupName;
    private String villageName;

    // WOMAN / BORROWER
    private Long womanId;
    private String womanName;

    // LOAN AMOUNT DETAILS
    private Double sanctionedAmount;
    private Double processingFee;

    // Actual disbursed amount:
    // sanctionedAmount - processingFee
    private Double loanAmount;

    // LOAN DETAILS
    private String loanPurpose;
    private LocalDate loanGivenDate;

    // REPAYMENT DETAILS
    private Integer repaymentPeriodMonths;
    private Double interestRate;
    private Integer serialNo;
    private String interestType;
    private Double monthlyEmi;
    private String loanStatus;

    // REPAYMENT SUMMARY
    private BigDecimal totalInterestReceived;
}
