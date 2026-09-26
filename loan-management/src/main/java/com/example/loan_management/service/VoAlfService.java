package com.example.loan_management.service;

import com.example.loan_management.dto.VoAlfDto;

import java.util.List;

public interface VoAlfService {

    VoAlfDto create(VoAlfDto voAlfDto);

    List<VoAlfDto> getAll();

    VoAlfDto getById(Long id);

    VoAlfDto update(Long id, VoAlfDto voAlfDto);

    void delete(Long id);

    List<VoAlfDto> getByCmrcId(Long cmrcId);
}