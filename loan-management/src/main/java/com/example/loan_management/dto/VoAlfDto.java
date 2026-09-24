package com.example.loan_management.dto;

import lombok.Data;

@Data
public class VoAlfDto {

    private Long id;

    private Integer serialNo;

    private Long cmrcId;

    private String villageName;

    private String voAlfName;

    private String accountNo;

    private Double receivedFund;
}