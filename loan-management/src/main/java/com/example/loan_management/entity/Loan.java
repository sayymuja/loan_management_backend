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

    @ManyToOne
    @JoinColumn(name = "vo_alf_id", nullable = false)
    private VoAlf voAlf;

    @Column(name = "group_name")
    private String groupName;

    @Column(name = "woman_name")
    private String womanName;

    @Column(name = "loan_amount")
    private Double loanAmount;

    @Column(name = "loan_purpose")
    private String loanPurpose;

    @Column(name = "loan_given_date")
    private LocalDate loanGivenDate;

    @Column(name = "repayment_period_months")
    private Integer repaymentPeriodMonths;

    @Column(name = "interest_rate")
    private Double interestRate;

    @Column(name = "serial_no")
    private Integer serialNo;
}