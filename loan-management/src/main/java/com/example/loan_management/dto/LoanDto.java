package com.example.loan_management.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class LoanDto {

    private Long id;

    private Long voAlfId;

    // =========================================================
    // BORROWER DETAILS
    // =========================================================

    private String groupName;

    private String womanName;

    // =========================================================
    // LOAN AMOUNT DETAILS
    // =========================================================

    /*
     * Actual loan principal.
     *
     * Example:
     * 4000000
     */
    private Double sanctionedAmount;


    /*
     * Processing fee deducted from sanctioned amount.
     *
     * Example:
     * 20000
     */
    private Double processingFee;


    /*
     * Actual amount disbursed.
     *
     * sanctionedAmount - processingFee
     *
     * Example:
     * 3980000
     */
    private Double loanAmount;

    // =========================================================
    // LOAN DETAILS
    // =========================================================

    private String loanPurpose;

    private LocalDate loanGivenDate;

    // =========================================================
    // REPAYMENT DETAILS
    // =========================================================

    private Integer repaymentPeriodMonths;

    private Double interestRate;

    private Integer serialNo;

    private String interestType;

    private Double monthlyEmi;

    private String loanStatus;

    // =========================================================
    // REPAYMENT SUMMARY
    // =========================================================

    private BigDecimal totalInterestReceived;
}