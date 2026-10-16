
package com.example.loan_management.auth.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class SignupRequest {

    private String name;

    private String email;

    private String password;

    // CMRC Details
    private String cmrcName;

    // Location Details
    private String district;

    private String taluka;

    // Bank Account Details
    private String accountNo;

    private LocalDate accountOpeningDate;
}
