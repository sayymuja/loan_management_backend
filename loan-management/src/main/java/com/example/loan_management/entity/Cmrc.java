        package com.example.loan_management.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Table(name = "cmrc")
@Data
public class Cmrc {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cmrc_name")
    private String cmrcName;

    @Column(name = "account_no")
    private String accountNo;

    @Column(name = "account_opening_date")
    private LocalDate accountOpeningDate;

    // LOCATION
    @Column(name = "district")
    private String district;

    @Column(name = "taluka")
    private String taluka;

    // STATUS
    @Column(name = "status")
    private String status;

    // SINGLE CMRC BALANCE
    // Existing balance must not change automatically.
    @Column(name = "total_fund")
    private Double totalFund;
}
