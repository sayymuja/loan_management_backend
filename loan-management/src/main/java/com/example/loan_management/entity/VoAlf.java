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

    @ManyToOne
    @JoinColumn(name = "cmrc_id", nullable = false)
    private Cmrc cmrc;

    @Column(name = "village_name")
    private String villageName;

    @Column(name = "vo_alf_name")
    private String voAlfName;

    @Column(name = "account_no")
    private String accountNo;

    @Column(name = "received_fund")
    private Double receivedFund;

    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @PrePersist
    protected void onCreate() {
        if (createdDate == null) {
            createdDate = LocalDateTime.now();
        }
    }
}