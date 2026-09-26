package com.example.loan_management.service;

import com.example.loan_management.dto.CmrcDto;

import java.util.List;

public interface CmrcService {
    CmrcDto create(CmrcDto cmrcDto);

    List<CmrcDto> getAll();

    CmrcDto getById(Long id);

    CmrcDto update(Long id, CmrcDto cmrcDto);

    void delete(Long id);
}
