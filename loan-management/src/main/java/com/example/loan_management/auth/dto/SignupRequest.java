package com.example.loan_management.auth.dto;

import lombok.Data;

@Data
public class SignupRequest {

    private String name;

    private String email;

    private String password;

    // CMRC Name entered during signup
    private String cmrcName;

    // Location details
    private String district;

    private String taluka;
}