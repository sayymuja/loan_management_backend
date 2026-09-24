package com.example.loan_management.service;

import com.example.loan_management.dto.VoAlfBankBalanceDto;

import java.util.List;

public interface VoAlfBankBalanceService {

    VoAlfBankBalanceDto create(VoAlfBankBalanceDto dto);

    List<VoAlfBankBalanceDto> getAll();

    VoAlfBankBalanceDto getById(Long id);

    List<VoAlfBankBalanceDto> getByVoAlfId(Long voAlfId);

    VoAlfBankBalanceDto update(Long id, VoAlfBankBalanceDto dto);

    List<VoAlfBankBalanceDto> createBulk(
            List<VoAlfBankBalanceDto> dtoList
    );

    void delete(Long id);
}