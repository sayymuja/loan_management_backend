package com.example.loan_management.dto;

import lombok.Data;

@Data
public class VillageDto {

    // =====================================================
    // Village ID
    // =====================================================
    private Long id;

    // =====================================================
    // VO / ALF ID
    // =====================================================
    private Long voAlfId;

    // =====================================================
    // Village Name
    // =====================================================
    private String villageName;
}