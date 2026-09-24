package com.example.loan_management.service.impl;

import com.example.loan_management.dto.CmrcDto;
import com.example.loan_management.entity.Cmrc;

import java.util.List;

public interface CmrcService {
    CmrcDto create(CmrcDto cmrcDto);

    List<CmrcDto> getAll();

    CmrcDto getById(Long id);

    CmrcDto update(Long id, CmrcDto cmrcDto);

    void delete(Long id);
}
