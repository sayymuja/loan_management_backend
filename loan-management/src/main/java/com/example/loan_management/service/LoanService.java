        package com.example.loan_management.service;

import com.example.loan_management.dto.LoanDto;

import java.util.List;

public interface LoanService {

    // =========================================================
    // CREATE LOAN
    // =========================================================

    LoanDto create(LoanDto loanDto);


    // =========================================================
    // GET ALL LOANS
    // =========================================================

    List<LoanDto> getAll();


    // =========================================================
    // GET LOAN BY ID
    // =========================================================

    LoanDto getById(Long id);


    // =========================================================
    // UPDATE LOAN
    // =========================================================

    LoanDto update(Long id, LoanDto loanDto);


    // =========================================================
    // DELETE LOAN
    // =========================================================

    void delete(Long id);


    // =========================================================
    // GET LOANS BY WOMAN
    // =========================================================

    List<LoanDto> getByWomanId(Long womanId);
}
