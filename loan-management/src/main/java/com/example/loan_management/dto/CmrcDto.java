        package com.example.loan_management.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class CmrcDto {

    private Long id;

    private Integer serialNo;

    // CMRC DETAILS
    private String cmrcName;

    private String accountNo;

    private LocalDate accountOpeningDate;

    // LOCATION
    private String district;

    private String taluka;

    // STATUS
    private String status;

    // SINGLE CMRC BALANCE
    // Existing balance is maintained separately.
    private Double totalFund;
}
