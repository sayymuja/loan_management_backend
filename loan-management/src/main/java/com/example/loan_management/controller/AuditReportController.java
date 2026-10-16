
package com.example.loan_management.controller;

import com.example.loan_management.dto.AuditReportDto;
import com.example.loan_management.service.AuditReportService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/audit-report")
@RequiredArgsConstructor
public class AuditReportController {

    private final AuditReportService auditReportService;

    @GetMapping
    public ResponseEntity<?> getAuditReport(
            @RequestParam(defaultValue = "MONTHLY") String reportType,
            @RequestParam int financialYear,
            @RequestParam(required = false) Integer month) {

        try {
            AuditReportDto report = auditReportService.generateReport(
                    reportType,
                    financialYear,
                    month);

            return ResponseEntity.ok(report);

        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(
                    java.util.Map.of("message", ex.getMessage()));

        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(java.util.Map.of(
                            "message", "Unable to generate audit report."));
        }
    }
}
