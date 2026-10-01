package com.example.loan_management.repository;

import com.example.loan_management.entity.Cmrc;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CmrcRepository extends JpaRepository<Cmrc, Long> {

    Optional<Cmrc> findByCmrcNameIgnoreCase(String cmrcName);
}