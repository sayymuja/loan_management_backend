        package com.example.loan_management.service;

import com.example.loan_management.dto.VoAlfDto;

import java.math.BigDecimal;
import java.util.List;

public interface VoAlfService {

    // Create VO / ALF
    VoAlfDto create(VoAlfDto voAlfDto);

    // Get all VO / ALF records
    List<VoAlfDto> getAll();

    // Get VO / ALF by ID
    VoAlfDto getById(Long id);

    // Update VO / ALF details
    VoAlfDto update(Long id, VoAlfDto voAlfDto);

    // Add fund to existing balance with transaction and approval workflow
    VoAlfDto addFund(Long id, BigDecimal amount);

    // Delete VO / ALF
    void delete(Long id);

    // Get VO / ALF records by CMRC
    List<VoAlfDto> getByCmrcId(Long cmrcId);
}
