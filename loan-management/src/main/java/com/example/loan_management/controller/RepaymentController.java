package com.example.loan_management.controller;

import com.example.loan_management.dto.RepaymentDto;
import com.example.loan_management.dto.RepaymentSummaryDto;
import com.example.loan_management.service.RepaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/repayment")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class RepaymentController {

    private final RepaymentService repaymentService;

    @PostMapping
    public RepaymentDto create(@RequestBody RepaymentDto repaymentDto) {
        return repaymentService.create(repaymentDto);
    }

    @GetMapping
    public List<RepaymentDto> getAll() {
        return repaymentService.getAll();
    }

    @GetMapping("/{id}")
    public RepaymentDto getById(@PathVariable Long id) {
        return repaymentService.getById(id);
    }

    @PutMapping("/{id}")
    public RepaymentDto update(
            @PathVariable Long id,
            @RequestBody RepaymentDto repaymentDto) {

        return repaymentService.update(id, repaymentDto);
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id) {
        repaymentService.delete(id);
        return "Repayment deleted successfully";
    }
    @GetMapping("/summary/{loanId}")
    public RepaymentSummaryDto getSummary(@PathVariable Long loanId) {
        return repaymentService.getSummary(loanId);
    }
    @GetMapping("/loan/{loanId}")
    public List<RepaymentDto> getByLoanId(@PathVariable Long loanId) {
        return repaymentService.getByLoanId(loanId);
    }
}