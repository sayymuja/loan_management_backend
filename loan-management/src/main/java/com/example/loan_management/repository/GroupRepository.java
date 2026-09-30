package com.example.loan_management.repository;

import com.example.loan_management.entity.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupRepository
        extends JpaRepository<Group, Long> {

    // =====================================================
    // GET GROUPS BY CMRC
    // =====================================================
    List<Group> findByCmrcId(Long cmrcId);

    // =====================================================
    // GET GROUPS BY VO / ALF
    // =====================================================
    List<Group> findByVoAlfId(Long voAlfId);
}