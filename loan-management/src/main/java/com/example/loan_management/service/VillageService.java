package com.example.loan_management.service;

import com.example.loan_management.dto.VillageDto;

import java.util.List;

public interface VillageService {

    // =====================================================
    // Create Village
    // =====================================================
    VillageDto create(VillageDto villageDto);

    // =====================================================
    // Get All Villages
    // =====================================================
    List<VillageDto> getAll();

    // =====================================================
    // Get Village By ID
    // =====================================================
    VillageDto getById(Long id);

    // =====================================================
    // Get Villages By VO / ALF ID
    // =====================================================
    List<VillageDto> getByVoAlfId(Long voAlfId);

    // =====================================================
    // Update Village
    // =====================================================
    VillageDto update(Long id, VillageDto villageDto);

    // =====================================================
    // Delete Village
    // =====================================================
    void delete(Long id);
}