package com.example.loan_management.controller;

import com.example.loan_management.dto.VoAlfBankBalanceDto;
import com.example.loan_management.service.VoAlfBankBalanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vo-alf-bank-balance")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class VoAlfBankBalanceController {

    private final VoAlfBankBalanceService bankBalanceService;

    @PostMapping
    public VoAlfBankBalanceDto create(
            @RequestBody VoAlfBankBalanceDto dto) {
        return bankBalanceService.create(dto);
    }

    @GetMapping
    public List<VoAlfBankBalanceDto> getAll() {
        return bankBalanceService.getAll();
    }

    @GetMapping("/{id}")
    public VoAlfBankBalanceDto getById(
            @PathVariable Long id) {
        return bankBalanceService.getById(id);
    }

    @GetMapping("/vo-alf/{voAlfId}")
    public List<VoAlfBankBalanceDto> getByVoAlfId(
            @PathVariable Long voAlfId) {
        return bankBalanceService.getByVoAlfId(voAlfId);
    }

    @PutMapping("/{id}")
    public VoAlfBankBalanceDto update(
            @PathVariable Long id,
            @RequestBody VoAlfBankBalanceDto dto) {
        return bankBalanceService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id) {
        bankBalanceService.delete(id);
        return "Bank balance deleted successfully";
    }
    @PostMapping("/bulk")
    public List<VoAlfBankBalanceDto> createBulk(
            @RequestBody List<VoAlfBankBalanceDto> dtoList) {

        return bankBalanceService.createBulk(dtoList);
    }
}