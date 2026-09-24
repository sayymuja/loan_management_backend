package com.example.loan_management.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class CmrcBalanceDto {

    private Long id;

    private Long cmrcId;

    private LocalDate balanceDate;

    private Double balanceAmount;
}