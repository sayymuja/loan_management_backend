package com.example.loan_management.dto;

import lombok.Data;

@Data
public class VoAlfFundDto {

    private Long id;

    private Long voAlfId;

    private Integer allocationGroupCount;
    private Integer allocationWomenCount;
    private Double allocationGroupAmount;
    private Double allocationWomenAmount;

    private Integer repaymentGroupCount;
    private Integer repaymentWomenCount;
    private Double repaymentGroupAmount;
    private Double repaymentWomenAmount;

    private Integer loanGroupCount;
    private Integer loanWomenCount;
    private Double loanGroupAmount;
    private Double loanWomenAmount;

    private Double currentInterestUltraPoor;
    private Double currentInterestDebtTrappedWomen;
    private Double currentInterestTotal;
}