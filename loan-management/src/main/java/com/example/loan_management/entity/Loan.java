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
    // WOMAN
    // =========================================================

    @ManyToOne
    @JoinColumn(name = "woman_id", nullable = false)
    private Women woman;


    // =========================================================
    // VO / ALF
    // =========================================================

    @Column(name = "vo_alf_id", nullable = false)
    private Long voAlfId;


    // =========================================================
    // LOAN AMOUNT
    // =========================================================

    @Column(name = "sanctioned_amount")
    private Double sanctionedAmount;

    @Column(name = "processing_fee")
    private Double processingFee;

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
    // REPAYMENT
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
    // STATUS
    // =========================================================

    @Column(name = "loan_status")
    private String loanStatus;
}