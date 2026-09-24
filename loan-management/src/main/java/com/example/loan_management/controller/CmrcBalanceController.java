package com.example.loan_management.controller;

import com.example.loan_management.dto.CmrcBalanceDto;
import com.example.loan_management.service.CmrcBalanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cmrc-balance")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class CmrcBalanceController {

    private final CmrcBalanceService cmrcBalanceService;

    @PostMapping
    public CmrcBalanceDto create(
            @RequestBody CmrcBalanceDto dto) {

        return cmrcBalanceService.create(dto);
    }

    @GetMapping
    public List<CmrcBalanceDto> getAll() {

        return cmrcBalanceService.getAll();
    }

    @GetMapping("/{id}")
    public CmrcBalanceDto getById(
            @PathVariable Long id) {

        return cmrcBalanceService.getById(id);
    }

    @GetMapping("/cmrc/{cmrcId}")
    public List<CmrcBalanceDto> getByCmrcId(
            @PathVariable Long cmrcId) {

        return cmrcBalanceService.getByCmrcId(cmrcId);
    }

    @PutMapping("/{id}")
    public CmrcBalanceDto update(
            @PathVariable Long id,
            @RequestBody CmrcBalanceDto dto) {

        return cmrcBalanceService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public String delete(
            @PathVariable Long id) {

        cmrcBalanceService.delete(id);

        return "CMRC balance deleted successfully";
    }
}