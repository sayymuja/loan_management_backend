        package com.example.loan_management.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "vo_alf")
@Data
public class VoAlf {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // CMRC Relationship
    @ManyToOne
    @JoinColumn(name = "cmrc_id", nullable = false)
    private Cmrc cmrc;

    // Village Information
    @Column(name = "village_name")
    private String villageName;

    // VO / ALF Information
    @Column(name = "vo_alf_name")
    private String voAlfName;

    // Bank Account Information
    @Column(name = "account_no")
    private String accountNo;

    // Single Balance for VO / ALF
    @Column(name = "received_fund")
    private Double receivedFund = 0.0;

    // Record Creation Date
    @Column(
            name = "created_date",
            nullable = false,
            updatable = false
    )
    private LocalDateTime createdDate;

    @PrePersist
    protected void onCreate() {
        if (createdDate == null) {
            createdDate = LocalDateTime.now();
        }

        if (receivedFund == null) {
            receivedFund = 0.0;
        }
    }
}
