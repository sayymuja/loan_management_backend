package com.example.loan_management.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Table(name = "loan")
@Data
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // =========================================================
    // VO / ALF
    // =========================================================

    @ManyToOne
    @JoinColumn(name = "vo_alf_id", nullable = false)
    private VoAlf voAlf;

    // =========================================================
    // BORROWER DETAILS
    // =========================================================

    @Column(name = "group_name")
    private String groupName;

    @Column(name = "woman_name")
    private String womanName;

    // =========================================================
    // LOAN AMOUNT DETAILS
    // =========================================================

    /*
     * Actual sanctioned loan principal.
     *
     * Example:
     * ₹40,00,000
     */
    @Column(name = "sanctioned_amount")
    private Double sanctionedAmount;


    /*
     * Processing fee deducted from sanctioned amount.
     *
     * Example:
     * ₹20,000
     */
    @Column(name = "processing_fee")
    private Double processingFee;




    /*
     * Actual amount disbursed to borrower.
     *
     * Formula:
     *
     * Disbursed Amount =
     * Sanctioned Amount - Processing Fee
     *
     * Example:
     * ₹40,00,000 - ₹20,000
     * = ₹39,80,000
     */

    @Column(name = "disbursed_amount")
    private Double loanAmount;

    // =========================================================
    // LOAN DETAILS
    // =========================================================

    @Column(name = "loan_purpose")
    private String loanPurpose;

    @Column(name = "loan_given_date")
    private LocalDate loanGivenDate;

    // =========================================================
    // REPAYMENT DETAILS
    // =========================================================

    @Column(name = "repayment_period_months")
    private Integer repaymentPeriodMonths;

    @Column(name = "interest_rate")
    private Double interestRate;

    @Column(name = "interest_type")
    private String interestType;

    @Column(name = "monthly_emi")
    private Double monthlyEmi;

    // =========================================================
    // OTHER
    // =========================================================


    @Column(name = "loan_status")
    private String loanStatus;
}