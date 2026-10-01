package com.example.loan_management.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LoanImageDto {

    private Long id;

    private Long loanId;

    private String fileName;

    private String contentType;

    private Long fileSize;

    private String viewUrl;

    private String downloadUrl;

    private LocalDateTime uploadedAt;
}