package com.example.loan_management.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Table(name = "repayment")
@Data
public class Repayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "loan_id", nullable = false)
    private Loan loan;

    @Column(name = "repayment_date")
    private LocalDate repaymentDate;

    @Column(name = "principal_amount")
    private Double principalAmount;

    @Column(name = "interest_amount")
    private Double interestAmount;

    @Column(name = "total_amount")
    private Double totalAmount;

    @Column(name = "regular_repayment")
    private Boolean regularRepayment;

    @Column(name = "penalty_amount")
    private Double penaltyAmount;

    private String remark;

    @Column(name = "paid_amount")
    private Double paidAmount;

    @Column(name = "installment_no")
    private Integer installmentNo;

    @Column(name = "installment_date")
    private LocalDate installmentDate;

    @Column(name = "scheduled_amount")
    private Double scheduledAmount;

    @Column(name = "payment_status")
    private String paymentStatus;


}