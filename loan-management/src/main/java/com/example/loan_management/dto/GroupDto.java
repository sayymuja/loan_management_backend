package com.example.loan_management.dto;

import lombok.Data;

@Data
public class GroupDto {

    private Long id;

    // CMRC
    private Long cmrcId;

    // VO / ALF
    private Long voAlfId;

    // Village
    private String villageName;

    // Group
    private String groupName;

    // Display Names
    private String cmrcName;
    private String voAlfName;
}