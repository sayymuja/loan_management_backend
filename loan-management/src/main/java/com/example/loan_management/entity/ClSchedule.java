package com.example.loan_management.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "cl_schedule")
@Data
public class ClSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "loan_id", nullable = false)
    private Loan loan;

    @Column(name = "installment_no")
    private Integer installmentNo;

    @Column(name = "outstanding_amount")
    private Double outstandingAmount;

    @Column(name = "principal_amount")
    private Double principalAmount;

    @Column(name = "interest_amount")
    private Double interestAmount;

    @Column(name = "monthly_installment")
    private Double monthlyInstallment;

    @Column(name = "average_monthly_installment")
    private Double averageMonthlyInstallment;

    private String remark;
}