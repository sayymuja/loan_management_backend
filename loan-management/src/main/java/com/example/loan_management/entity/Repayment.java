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

    @Column(name = "payment_date")
    private LocalDate paymentDate;

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
}