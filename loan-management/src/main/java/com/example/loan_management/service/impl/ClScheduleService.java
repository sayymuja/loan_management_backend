package com.example.loan_management.service;

import com.example.loan_management.dto.ClScheduleDto;

import java.util.List;

public interface ClScheduleService {

    ClScheduleDto create(ClScheduleDto dto);

    List<ClScheduleDto> getAll();

    ClScheduleDto getById(Long id);

    ClScheduleDto update(Long id, ClScheduleDto dto);

    List<ClScheduleDto> generateSchedule(Long loanId);

    void delete(Long id);
}