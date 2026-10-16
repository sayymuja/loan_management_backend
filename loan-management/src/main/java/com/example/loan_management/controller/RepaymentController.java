        package com.example.loan_management.controller;

import com.example.loan_management.dto.RepaymentDto;
import com.example.loan_management.dto.RepaymentSummaryDto;
import com.example.loan_management.service.RepaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/repayment")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class RepaymentController {

    private final RepaymentService repaymentService;

    // =========================================================
    // CREATE REPAYMENT
    // =========================================================

    @PostMapping
    public RepaymentDto create(
            @RequestBody RepaymentDto repaymentDto) {

        return repaymentService.create(repaymentDto);
    }

    // =========================================================
    // GET ALL REPAYMENTS
    // =========================================================

    @GetMapping
    public List<RepaymentDto> getAll() {

        return repaymentService.getAll();
    }

    // =========================================================
    // GET REPAYMENT BY ID
    // =========================================================

    @GetMapping("/{id}")
    public RepaymentDto getById(
            @PathVariable Long id) {

        return repaymentService.getById(id);
    }

    // =========================================================
    // UPDATE REPAYMENT
    // =========================================================

    @PutMapping("/{id}")
    public RepaymentDto update(
            @PathVariable Long id,
            @RequestBody RepaymentDto repaymentDto) {

        return repaymentService.update(id, repaymentDto);
    }

    // =========================================================
    // DELETE REPAYMENT
    // =========================================================

    @DeleteMapping("/{id}")
    public String delete(
            @PathVariable Long id) {

        repaymentService.delete(id);

        return "Repayment deleted successfully";
    }

    // =========================================================
    // GET REPAYMENT SUMMARY
    // =========================================================

    @GetMapping("/summary/{loanId}")
    public RepaymentSummaryDto getSummary(
            @PathVariable Long loanId) {

        return repaymentService.getSummary(loanId);
    }

    // =========================================================
    // GET REPAYMENTS BY LOAN ID
    // =========================================================

    @GetMapping("/loan/{loanId}")
    public List<RepaymentDto> getByLoanId(
            @PathVariable Long loanId) {

        return repaymentService.getByLoanId(loanId);
    }

    // =========================================================
    // GENERATE REPAYMENT SCHEDULE
    // =========================================================

    @PostMapping("/generate/{loanId}")
    public List<RepaymentDto> generateSchedule(
            @PathVariable Long loanId) {

        return repaymentService.generateSchedule(loanId);
    }

    // =========================================================
    // ADMIN: PAY EMI
    // =========================================================

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/pay/{id}")
    public ResponseEntity<RepaymentDto> payEmi(
            @PathVariable Long id,
            @RequestParam Double paidAmount,
            @RequestParam(defaultValue = "0") Double penaltyAmount,
            @RequestParam(defaultValue = "false") Boolean regularRepayment,
            @RequestParam String paymentEventId) {

        RepaymentDto updated = repaymentService.payEmi(
                id,
                paidAmount,
                penaltyAmount,
                regularRepayment,
                paymentEventId
        );

        return ResponseEntity.ok(updated);
    }

    // =========================================================
    // EDIT PAID EMI
    // Service currently blocks editing paid EMI until a proper
    // ledger correction/refund workflow is implemented.
    // =========================================================

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/edit-paid/{repaymentId}")
    public RepaymentDto editPaidEmi(
            @PathVariable Long repaymentId,
            @RequestParam Double paidAmount,
            @RequestParam(defaultValue = "0") Double penaltyAmount) {

        return repaymentService.editPaidEmi(
                repaymentId,
                paidAmount,
                penaltyAmount
        );
    }
}
