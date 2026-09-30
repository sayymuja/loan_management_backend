package com.example.loan_management.dto;

import lombok.Data;

@Data
public class WomenDto {

    private Long id;

    // =====================================================
    // HIERARCHY
    // =====================================================

    private Long cmrcId;

    private Long voAlfId;

    private Long groupId;

    private String cmrcName;

    private String voAlfName;

    private String groupName;

    private String villageName;


    // =====================================================
    // WOMAN DETAILS
    // =====================================================

    private String womanName;

    private String husbandName;

    private String mobileNo;

    private String address;

    private String status;
}