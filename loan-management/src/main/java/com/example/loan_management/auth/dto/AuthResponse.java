package com.example.loan_management.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponse {

    private Long id;

    private String name;

    private String email;

    private Long cmrcId;

    private String cmrcName;

    private String district;

    private String taluka;

    private String token;
}