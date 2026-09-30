package com.example.loan_management.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "village")
@Data
public class Village {

    // =====================================================
    // Primary Key
    // =====================================================
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    // =====================================================
    // VO / ALF Relationship
    // =====================================================
    @ManyToOne
    @JoinColumn(name = "vo_alf_id", nullable = false)
    private VoAlf voAlf;


    // =====================================================
    // Village Name
    // =====================================================
    @Column(name = "village_name", nullable = false)
    private String villageName;
}