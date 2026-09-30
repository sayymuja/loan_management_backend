package com.example.loan_management.service;

import com.example.loan_management.dto.GroupDto;

import java.util.List;

public interface GroupService {

    // =====================================================
    // CREATE
    // =====================================================
    GroupDto create(GroupDto groupDto);

    // =====================================================
    // GET ALL
    // =====================================================
    List<GroupDto> getAll();

    // =====================================================
    // GET BY ID
    // =====================================================
    GroupDto getById(Long id);

    // =====================================================
    // GET GROUPS BY CMRC
    // =====================================================
    List<GroupDto> getByCmrcId(Long cmrcId);

    // =====================================================
    // GET GROUPS BY VO / ALF
    // =====================================================
    List<GroupDto> getByVoAlfId(Long voAlfId);

    // =====================================================
    // UPDATE
    // =====================================================
    GroupDto update(Long id, GroupDto groupDto);

    // =====================================================
    // DELETE
    // =====================================================
    void delete(Long id);
}