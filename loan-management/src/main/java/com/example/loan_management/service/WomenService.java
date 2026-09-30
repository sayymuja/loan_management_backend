package com.example.loan_management.service;

import com.example.loan_management.dto.WomenDto;

import java.util.List;

public interface WomenService {

    // =====================================================
    // CREATE WOMAN
    // =====================================================
    WomenDto create(WomenDto womenDto);

    // =====================================================
    // GET ALL WOMEN
    // =====================================================
    List<WomenDto> getAll();

    // =====================================================
    // GET WOMAN BY ID
    // =====================================================
    WomenDto getById(Long id);

    // =====================================================
    // GET WOMEN BY GROUP ID
    // =====================================================
    List<WomenDto> getByGroupId(Long groupId);

    // =====================================================
    // UPDATE WOMAN
    // =====================================================
    WomenDto update(Long id, WomenDto womenDto);

    // =====================================================
    // DELETE WOMAN
    // =====================================================
    void delete(Long id);
}