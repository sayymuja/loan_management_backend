        package com.example.loan_management.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class VoAlfDto {

    private Long id;

    private Long cmrcId;

    // Village Information
    private String villageName;

    // VO / ALF Information
    private String voAlfName;

    // Bank Account Information
    private String accountNo;

    // Single VO / ALF Balance
    private Double receivedFund = 0.0;

    // Record Creation Date
    private LocalDateTime createdDate;
}
