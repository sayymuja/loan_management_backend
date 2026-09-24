package com.example.loan_management.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class CmrcDto {

    private Long id;

    private Integer serialNo;

    private String cmrcName;

    private String accountNo;

    private LocalDate accountOpeningDate;

    private Double totalFund;

    private Double serviceFeeReceived;

    private Integer recordsPrinted;

    private Double printedRecordsAmount;

    private Integer recordsDistributedVillages;

    private Double expectedRecordAmount;

    private Double actualRecordAmountReceived;

    private Double tezshreeFundReceivedUltraPoor;

    private Double tezshreeFundReceivedDebtTrappedWomen;

    private Double tezshreeFundReceivedTotal;

    private Integer fundDistributedVillageCount;

    private Integer distributedUltraPoorWomenCount;

    private Double distributedUltraPoorFund;

    private Integer distributedDebtTrappedWomenCount;

    private Double distributedDebtTrappedFund;

    private Double distributedTotalFund;
}