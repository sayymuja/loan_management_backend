package com.example.loan_management.repository;

import com.example.loan_management.entity.Interest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InterestRepository extends JpaRepository<Interest, Long> {

    List<Interest> findByCmrcId(Long cmrcId);

    List<Interest> findByFinancialYear(String financialYear);
}