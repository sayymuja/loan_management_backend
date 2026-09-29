package com.example.loan_management.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class VoAlfDto {

    private Long id;

    private Long cmrcId;

    private String villageName;

    private String voAlfName;

    private String accountNo;

    private Double receivedFund;

    private LocalDateTime createdDate;
}