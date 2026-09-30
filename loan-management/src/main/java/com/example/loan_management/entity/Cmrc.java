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

    @Column(name = "serial_no")
    private Integer serialNo;

    @Column(name = "cmrc_name")
    private String cmrcName;

    @Column(name = "account_no")
    private String accountNo;

    @Column(name = "account_opening_date")
    private LocalDate accountOpeningDate;

    // ==============================
    // LOCATION
    // ==============================

    @Column(name = "district")
    private String district;

    @Column(name = "taluka")
    private String taluka;

    // ==============================
    // STATUS
    // ==============================

    @Column(name = "status")
    private String status;

    // ==============================
    // CMRC AMOUNT
    // ==============================

    @Column(name = "total_fund")
    private Double totalFund;

    @Column(name = "service_fee_received")
    private Double serviceFeeReceived;

    @Column(name = "records_printed")
    private Integer recordsPrinted;

    @Column(name = "printed_records_amount")
    private Double printedRecordsAmount;

    @Column(name = "records_distributed_villages")
    private Integer recordsDistributedVillages;

    @Column(name = "expected_record_amount")
    private Double expectedRecordAmount;

    @Column(name = "actual_record_amount_received")
    private Double actualRecordAmountReceived;

    // ==============================
    // TEZSHREE FUND RECEIVED
    // ==============================

    @Column(name = "tezshree_fund_received_ultra_poor")
    private Double tezshreeFundReceivedUltraPoor;

    @Column(name = "tezshree_fund_received_debt_trapped_women")
    private Double tezshreeFundReceivedDebtTrappedWomen;

    @Column(name = "tezshree_fund_received_total")
    private Double tezshreeFundReceivedTotal;

    // ==============================
    // FUND DISTRIBUTION
    // ==============================

    @Column(name = "fund_distributed_village_count")
    private Integer fundDistributedVillageCount;

    @Column(name = "distributed_ultra_poor_women_count")
    private Integer distributedUltraPoorWomenCount;

    @Column(name = "distributed_ultra_poor_fund")
    private Double distributedUltraPoorFund;

    @Column(name = "distributed_debt_trapped_women_count")
    private Integer distributedDebtTrappedWomenCount;

    @Column(name = "distributed_debt_trapped_fund")
    private Double distributedDebtTrappedFund;

    @Column(name = "distributed_total_fund")
    private Double distributedTotalFund;
}