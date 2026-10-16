        package com.example.loan_management.service;

import com.example.loan_management.dto.CmrcDto;

import java.util.List;

public interface CmrcService {

    // CREATE CMRC
    CmrcDto create(CmrcDto cmrcDto);

    // ADD TO SINGLE CMRC BALANCE
    CmrcDto addBalance(Double amount);

    // GET ALL CMRC
    List<CmrcDto> getAll();

    // GET CMRC BY ID
    CmrcDto getById(Long id);

    // UPDATE CMRC DETAILS
    // Existing totalFund must remain unchanged.
    CmrcDto update(Long id, CmrcDto cmrcDto);

    // DELETE CMRC
    void delete(Long id);
}
