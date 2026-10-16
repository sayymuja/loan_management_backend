        package com.example.loan_management.controller;

import com.example.loan_management.dto.LoanApprovalRequestDto;
import com.example.loan_management.service.LoanApprovalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class LoanApprovalController {

    private final LoanApprovalService approvalService;

    // =========================================================
    // USER: VIEW OWN SUBMITTED REQUESTS
    // =========================================================

    @GetMapping("/api/loan-approval-requests/my")
    public ResponseEntity<List<LoanApprovalRequestDto>> getMyRequests() {

        return ResponseEntity.ok(
                approvalService.getMyRequests()
        );
    }

    // =========================================================
    // ADMIN: VIEW PENDING REQUESTS FOR THEIR CMRC
    // =========================================================

    @GetMapping("/api/admin/loan-approvals/pending")
    public ResponseEntity<List<LoanApprovalRequestDto>> getPendingRequests() {

        return ResponseEntity.ok(
                approvalService.getPendingRequests()
        );
    }

    // =========================================================
    // ADMIN: APPROVE REQUEST
    // =========================================================

    @PostMapping("/api/admin/loan-approvals/{requestId}/approve")
    public ResponseEntity<LoanApprovalRequestDto> approve(
            @PathVariable Long requestId,
            @RequestBody(required = false) Map<String, String> body) {

        String remarks = body == null
                ? null
                : body.get("remarks");

        return ResponseEntity.ok(
                approvalService.approve(requestId, remarks)
        );
    }

    // =========================================================
    // ADMIN: REJECT REQUEST
    // =========================================================

    @PostMapping("/api/admin/loan-approvals/{requestId}/reject")
    public ResponseEntity<LoanApprovalRequestDto> reject(
            @PathVariable Long requestId,
            @RequestBody(required = false) Map<String, String> body) {

        String remarks = body == null
                ? null
                : body.get("remarks");

        return ResponseEntity.ok(
                approvalService.reject(requestId, remarks)
        );
    }

    // =========================================================
    // USER: SUBMIT PAY EMI APPROVAL REQUEST
    // =========================================================

    @PostMapping("/api/loan-approval-requests/repayment/pay")
    public ResponseEntity<LoanApprovalRequestDto> submitPayEmiRequest(
            @RequestBody Map<String, Object> body) {

        Long repaymentId = getLong(body, "repaymentId");

        Double paidAmount = getDouble(body, "paidAmount");

        Double penaltyAmount = getOptionalDouble(
                body,
                "penaltyAmount"
        );

        Boolean regularRepayment = getBoolean(
                body,
                "regularRepayment"
        );

        LoanApprovalRequestDto response =
                approvalService.submitPayEmiRequest(
                        repaymentId,
                        paidAmount,
                        penaltyAmount,
                        regularRepayment
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // =========================================================
    // USER: SUBMIT EDIT PAID EMI APPROVAL REQUEST
    // =========================================================

    @PostMapping("/api/loan-approval-requests/repayment/edit")
    public ResponseEntity<LoanApprovalRequestDto> submitEditPaidEmiRequest(
            @RequestBody Map<String, Object> body) {

        Long repaymentId = getLong(body, "repaymentId");

        Double paidAmount = getDouble(body, "paidAmount");

        Double penaltyAmount = getOptionalDouble(
                body,
                "penaltyAmount"
        );

        LoanApprovalRequestDto response =
                approvalService.submitEditPaidEmiRequest(
                        repaymentId,
                        paidAmount,
                        penaltyAmount
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // =========================================================
    // REQUEST BODY HELPERS
    // =========================================================

    private Long getLong(
            Map<String, Object> body,
            String key) {

        Object value = body.get(key);

        if (value instanceof Number number) {
            return number.longValue();
        }

        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                key + " is required."
        );
    }

    private Double getDouble(
            Map<String, Object> body,
            String key) {

        Object value = body.get(key);

        if (value instanceof Number number) {
            return number.doubleValue();
        }

        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                key + " is required."
        );
    }

    private Double getOptionalDouble(
            Map<String, Object> body,
            String key) {

        Object value = body.get(key);

        if (value == null) {
            return 0.0;
        }

        if (value instanceof Number number) {
            return number.doubleValue();
        }

        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                key + " must be a valid number."
        );
    }

    private Boolean getBoolean(
            Map<String, Object> body,
            String key) {

        Object value = body.get(key);

        if (value == null) {
            return false;
        }

        if (value instanceof Boolean bool) {
            return bool;
        }

        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                key + " must be true or false."
        );
    }
}
