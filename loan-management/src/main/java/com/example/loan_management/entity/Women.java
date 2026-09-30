package com.example.loan_management.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "woman")
@Data
public class Women {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /*
     * =====================================================
     * GROUP RELATIONSHIP
     * =====================================================
     * One Group can have many Women
     * Each Woman belongs to one Group
     */
    @ManyToOne
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    /*
     * =====================================================
     * WOMAN DETAILS
     * =====================================================
     */

    @Column(name = "woman_name", nullable = false)
    private String womanName;

    @Column(name = "husband_name")
    private String husbandName;

    @Column(name = "mobile_no")
    private String mobileNo;

    @Column(name = "address")
    private String address;

    @Column(name = "status")
    private String status;
}