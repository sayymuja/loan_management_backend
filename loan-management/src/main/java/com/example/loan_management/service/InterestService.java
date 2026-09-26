package com.example.loan_management.service;

import com.example.loan_management.dto.InterestDto;

import java.util.List;

public interface InterestService {

    InterestDto create(InterestDto dto);

    List<InterestDto> getAll();

    InterestDto getById(Long id);

    List<InterestDto> getByCmrcId(Long cmrcId);

    List<InterestDto> getByFinancialYear(String financialYear);

    InterestDto update(Long id, InterestDto dto);

    void delete(Long id);
}