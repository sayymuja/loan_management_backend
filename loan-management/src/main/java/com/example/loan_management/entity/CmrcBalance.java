package com.example.loan_management.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Table(name = "cmrc_balance")
@Data
public class CmrcBalance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "cmrc_id", nullable = false)
    private Cmrc cmrc;

    @Column(name = "balance_date")
    private LocalDate balanceDate;

    @Column(name = "balance_amount")
    private Double balanceAmount;
}