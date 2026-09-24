package com.example.loan_management.repository;

import com.example.loan_management.entity.VoAlf;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VoAlfRepository extends JpaRepository<VoAlf, Long> {
    List<VoAlf> findByCmrcId(Long cmrcId);
}