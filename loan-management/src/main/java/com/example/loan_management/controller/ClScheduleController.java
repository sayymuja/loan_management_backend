package com.example.loan_management.controller;

import com.example.loan_management.dto.ClScheduleDto;
import com.example.loan_management.service.ClScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cl-schedule")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class ClScheduleController {

    private final ClScheduleService clScheduleService;

    @PostMapping
    public ClScheduleDto create(@RequestBody ClScheduleDto dto) {
        return clScheduleService.create(dto);
    }

    @GetMapping
    public List<ClScheduleDto> getAll() {
        return clScheduleService.getAll();
    }

    @GetMapping("/{id}")
    public ClScheduleDto getById(@PathVariable Long id) {
        return clScheduleService.getById(id);
    }

    @PutMapping("/{id}")
    public ClScheduleDto update(
            @PathVariable Long id,
            @RequestBody ClScheduleDto dto) {
        return clScheduleService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id) {
        clScheduleService.delete(id);
        return "CL Schedule deleted successfully";
    }
    @PostMapping("/generate/{loanId}")
    public List<ClScheduleDto> generateSchedule(
            @PathVariable Long loanId) {

        return clScheduleService.generateSchedule(loanId);
    }
    @GetMapping("/loan/{loanId}")
    public List<ClScheduleDto> getByLoanId(
            @PathVariable Long loanId) {

        return clScheduleService.getByLoanId(loanId);
    }
}