package com.example.loan_management.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserApprovalDto {

    private Long id;

    private String name;

    private String email;

    private Long cmrcId;

    private String cmrcName;

    private String district;

    private String taluka;

    private String accountNo;

    private LocalDate accountOpeningDate;

    private String role;

    private String accountStatus;
}