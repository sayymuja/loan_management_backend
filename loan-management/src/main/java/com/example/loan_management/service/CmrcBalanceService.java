package com.example.loan_management.service;

import com.example.loan_management.dto.CmrcBalanceDto;

import java.util.List;

public interface CmrcBalanceService {

    CmrcBalanceDto create(CmrcBalanceDto dto);

    List<CmrcBalanceDto> getAll();

    CmrcBalanceDto getById(Long id);

    List<CmrcBalanceDto> getByCmrcId(Long cmrcId);

    CmrcBalanceDto update(Long id, CmrcBalanceDto dto);

    void delete(Long id);
}