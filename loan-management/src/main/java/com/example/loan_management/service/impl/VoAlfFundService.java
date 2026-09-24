package com.example.loan_management.service;

import com.example.loan_management.dto.VoAlfFundDto;

import java.util.List;

public interface VoAlfFundService {

    VoAlfFundDto create(VoAlfFundDto dto);

    List<VoAlfFundDto> getAll();

    VoAlfFundDto getById(Long id);

    VoAlfFundDto update(Long id, VoAlfFundDto dto);

    void delete(Long id);
}