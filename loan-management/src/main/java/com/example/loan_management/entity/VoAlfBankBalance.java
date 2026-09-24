package com.example.loan_management.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Table(name = "vo_alf_bank_balance")
@Data
public class VoAlfBankBalance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "vo_alf_id", nullable = false)
    private VoAlf voAlf;

    @Column(name = "balance_month")
    private LocalDate balanceMonth;

    @Column(name = "balance_amount")
    private Double balanceAmount;

    @Column(name = "serial_no")
    private Integer serialNo;
}