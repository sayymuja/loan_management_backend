package com.example.loan_management.repository;

import com.example.loan_management.entity.Women;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WomenRepository extends JpaRepository<Women, Long> {

    /*
     * Get all women by Group ID
     */
    List<Women> findByGroupId(Long groupId);


    /*
     * Get all women by CMRC
     *
     * Women
     *   ↓
     * Group
     *   ↓
     * cmrcId
     */
    List<Women> findByGroup_CmrcId(Long cmrcId);
}