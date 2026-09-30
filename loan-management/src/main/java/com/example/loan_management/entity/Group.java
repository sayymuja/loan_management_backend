package com.example.loan_management.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "loan_group")
@Data
public class Group {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /*
     * Group belongs to CMRC
     */
    @Column(name = "cmrc_id", nullable = false)
    private Long cmrcId;

    /*
     * Group belongs to VO / ALF
     */
    @Column(name = "vo_alf_id", nullable = false)
    private Long voAlfId;

    /*
     * Village Name
     * Same approach as VO / ALF
     */
    @Column(name = "village_name", nullable = false)
    private String villageName;

    /*
     * Group Name
     */
    @Column(name = "group_name", nullable = false)
    private String groupName;
}