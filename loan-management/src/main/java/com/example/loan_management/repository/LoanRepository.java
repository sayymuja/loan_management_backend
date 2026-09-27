package com.example.loan_management.repository;

import com.example.loan_management.entity.Loan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoanRepository extends JpaRepository<Loan, Long> {

    List<Loan> findByVoAlfId(Long voAlfId);
}