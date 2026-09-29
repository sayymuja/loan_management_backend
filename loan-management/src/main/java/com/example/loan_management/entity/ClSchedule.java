package com.example.loan_management.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Table(name = "cl_schedule")
@Data
public class ClSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    // ==========================================
    // LOAN
    // ==========================================

    @ManyToOne
    @JoinColumn(name = "loan_id", nullable = false)
    private Loan loan;


    // ==========================================
    // INSTALLMENT NUMBER
    // ==========================================

    @Column(name = "installment_no")
    private Integer installmentNo;


    // ==========================================
    // INSTALLMENT DATE
    // ==========================================

    @Column(name = "installment_date")
    private LocalDate installmentDate;


    // ==========================================
    // OUTSTANDING AMOUNT
    // ==========================================

    @Column(name = "outstanding_amount")
    private Double outstandingAmount;


    // ==========================================
    // PRINCIPAL AMOUNT
    // ==========================================

    @Column(name = "principal_amount")
    private Double principalAmount;


    // ==========================================
    // INTEREST AMOUNT
    // ==========================================

    @Column(name = "interest_amount")
    private Double interestAmount;


    // ==========================================
    // MONTHLY INSTALLMENT
    // ==========================================

    @Column(name = "monthly_installment")
    private Double monthlyInstallment;


    // ==========================================
    // AVERAGE MONTHLY INSTALLMENT
    // ==========================================

    @Column(name = "average_monthly_installment")
    private Double averageMonthlyInstallment;


    // ==========================================
    // CLOSING BALANCE
    // ==========================================

    @Column(name = "closing_balance")
    private Double closingBalance;


    // ==========================================
    // REMARK
    // ==========================================

    @Column(name = "remark")
    private String remark;
}