package com.example.loan_management.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "interest")
@Data
public class Interest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "cmrc_id", nullable = false)
    private Cmrc cmrc;

    @Column(name = "village_name")
    private String villageName;

    @Column(name = "vo_alf_received_amount")
    private Double voAlfReceivedAmount;

    @Column(name = "financial_year")
    private String financialYear;

    @Column(name = "total_nil_women")
    private Integer totalNilWomen;

    @Column(name = "total_interest_amount")
    private Double totalInterestAmount;

    @Column(name = "serial_no")
    private Integer serialNo;
}