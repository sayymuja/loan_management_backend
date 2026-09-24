package com.example.loan_management.dto;

import lombok.Data;

@Data
public class InterestDto {

    private Long id;

    private Long cmrcId;

    private String villageName;

    private Double voAlfReceivedAmount;

    private String financialYear;

    private Integer totalNilWomen;

    private Double totalInterestAmount;

    private Integer serialNo;
}