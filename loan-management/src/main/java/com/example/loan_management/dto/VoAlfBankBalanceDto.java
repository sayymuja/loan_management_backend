package com.example.loan_management.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class VoAlfBankBalanceDto {

    private Long id;

    private Long voAlfId;

    private LocalDate balanceMonth;

    private Double balanceAmount;

    private Integer serialNo;
}