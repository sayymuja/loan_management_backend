package com.example.loan_management.repository;

import com.example.loan_management.entity.CmrcBalance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CmrcBalanceRepository
        extends JpaRepository<CmrcBalance, Long> {

    List<CmrcBalance> findByCmrcId(Long cmrcId);
}