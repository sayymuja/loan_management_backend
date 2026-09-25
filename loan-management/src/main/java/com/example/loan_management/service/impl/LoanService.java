package com.example.loan_management.service;

import com.example.loan_management.dto.LoanDto;

import java.util.List;

public interface LoanService {

    LoanDto create(LoanDto loanDto);

    List<LoanDto> getAll();

    LoanDto getById(Long id);

    LoanDto update(Long id, LoanDto loanDto);

    void delete(Long id);

    List<LoanDto> getByVoAlfId(Long voAlfId);
}