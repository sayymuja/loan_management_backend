package com.example.loan_management.repository;

import com.example.loan_management.entity.LoanImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoanImageRepository extends JpaRepository<LoanImage, Long> {

    // =========================================================
    // GET ALL IMAGES BY LOAN
    // =========================================================

    List<LoanImage> findByLoanId(Long loanId);


    // =========================================================
    // DELETE ALL IMAGES BY LOAN
    // =========================================================

    void deleteByLoanId(Long loanId);
}