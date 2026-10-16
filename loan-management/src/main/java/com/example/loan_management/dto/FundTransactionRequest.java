package com.example.loan_management.dto;

import com.example.loan_management.entity.FundTransaction;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class FundTransactionRequest {

    @NotNull(message = "VO/ALF ID is required")
    private Long voAlfId;

    @NotNull(message = "Transaction type is required")
    private FundTransaction.TransactionType transactionType;

    @NotNull(message = "Category is required")
    private FundTransaction.FundCategory category;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    private BigDecimal amount;

    private LocalDate transactionDate;

    @Size(max = 500, message = "Remark cannot exceed 500 characters")
    private String remark;
}