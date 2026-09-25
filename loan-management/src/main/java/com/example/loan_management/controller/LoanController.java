package com.example.loan_management.controller;

import com.example.loan_management.dto.LoanDto;
import com.example.loan_management.service.LoanService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/loan")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class LoanController {

    private final LoanService loanService;

    @PostMapping
    public LoanDto create(@RequestBody LoanDto loanDto) {
        return loanService.create(loanDto);
    }

    @GetMapping
    public List<LoanDto> getAll() {
        return loanService.getAll();
    }

    @GetMapping("/{id}")
    public LoanDto getById(@PathVariable Long id) {
        return loanService.getById(id);
    }

    @PutMapping("/{id}")
    public LoanDto update(
            @PathVariable Long id,
            @RequestBody LoanDto loanDto) {

        return loanService.update(id, loanDto);
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id) {
        loanService.delete(id);
        return "Loan deleted successfully";
    }
    @GetMapping("/vo-alf/{voAlfId}")
    public List<LoanDto> getByVoAlfId(@PathVariable Long voAlfId) {
        return loanService.getByVoAlfId(voAlfId);
    }
}