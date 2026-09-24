package com.example.loan_management.controller;

import com.example.loan_management.dto.InterestDto;
import com.example.loan_management.service.InterestService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/interest")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class InterestController {

    private final InterestService interestService;

    @PostMapping
    public InterestDto create(@RequestBody InterestDto dto) {
        return interestService.create(dto);
    }

    @GetMapping
    public List<InterestDto> getAll() {
        return interestService.getAll();
    }

    @GetMapping("/{id}")
    public InterestDto getById(@PathVariable Long id) {
        return interestService.getById(id);
    }

    @GetMapping("/cmrc/{cmrcId}")
    public List<InterestDto> getByCmrcId(
            @PathVariable Long cmrcId) {
        return interestService.getByCmrcId(cmrcId);
    }

    @GetMapping("/financial-year/{financialYear}")
    public List<InterestDto> getByFinancialYear(
            @PathVariable String financialYear) {
        return interestService.getByFinancialYear(financialYear);
    }

    @PutMapping("/{id}")
    public InterestDto update(
            @PathVariable Long id,
            @RequestBody InterestDto dto) {
        return interestService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id) {
        interestService.delete(id);
        return "Interest record deleted successfully";
    }
}