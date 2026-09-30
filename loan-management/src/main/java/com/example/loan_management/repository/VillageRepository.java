package com.example.loan_management.repository;

import com.example.loan_management.entity.Village;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VillageRepository extends JpaRepository<Village, Long> {

    // =====================================================
    // Find Villages by VO / ALF
    // =====================================================
    List<Village> findByVoAlfId(Long voAlfId);

}