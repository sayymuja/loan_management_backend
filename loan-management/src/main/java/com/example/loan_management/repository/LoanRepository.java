package com.example.loan_management.repository;

import com.example.loan_management.entity.Loan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoanRepository extends JpaRepository<Loan, Long> {

    // =========================================================
    // GET LOANS BY WOMAN
    // =========================================================

    List<Loan> findByWomanId(Long womanId);


    // =========================================================
    // GET LOANS BY CMRC
    // Loan -> Woman -> Group -> cmrcId
    // =========================================================

    List<Loan> findByWoman_Group_CmrcId(Long cmrcId);


    // =========================================================
    // CHECK LOAN BELONGS TO CURRENT CMRC
    // =========================================================

    boolean existsByIdAndWoman_Group_CmrcId(
            Long loanId,
            Long cmrcId
    );
}