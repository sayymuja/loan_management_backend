package com.example.loan_management.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "vo_alf_fund")
@Data
public class VoAlfFund {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "vo_alf_id", nullable = false)
    private VoAlf voAlf;

    // वाटप निधी
    private Integer allocationGroupCount;
    private Integer allocationWomenCount;
    private Double allocationGroupAmount;
    private Double allocationWomenAmount;

    // परतफेड करून आलेला निधी
    private Integer repaymentGroupCount;
    private Integer repaymentWomenCount;
    private Double repaymentGroupAmount;
    private Double repaymentWomenAmount;

    // VO मधून कर्ज दिलेली माहिती 2025-26
    private Integer loanGroupCount;
    private Integer loanWomenCount;
    private Double loanGroupAmount;
    private Double loanWomenAmount;

    private Double currentInterestUltraPoor;
    private Double currentInterestDebtTrappedWomen;
    private Double currentInterestTotal;
}