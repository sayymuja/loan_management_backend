
package com.example.loan_management.controller;

import com.example.loan_management.dto.LoanApprovalRequestDto;
import com.example.loan_management.service.LoanApprovalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class LoanApprovalController {

    private final LoanApprovalService approvalService;

    // USER: view their own submitted requests
    @GetMapping("/api/loan-approval-requests/my")
    public ResponseEntity<List<LoanApprovalRequestDto>> getMyRequests() {
        return ResponseEntity.ok(approvalService.getMyRequests());
    }

    // ADMIN: view pending requests for their own CMRC
    @GetMapping("/api/admin/loan-approvals/pending")
    public ResponseEntity<List<LoanApprovalRequestDto>> getPendingRequests() {
        return ResponseEntity.ok(approvalService.getPendingRequests());
    }

    // ADMIN: approve a request
    @PostMapping("/api/admin/loan-approvals/{requestId}/approve")
    public ResponseEntity<LoanApprovalRequestDto> approve(
            @PathVariable Long requestId,
            @RequestBody(required = false) Map<String, String> body
    ) {
        String remarks = body == null ? null : body.get("remarks");

        return ResponseEntity.ok(
                approvalService.approve(requestId, remarks)
        );
    }

    // ADMIN: reject a request
    @PostMapping("/api/admin/loan-approvals/{requestId}/reject")
    public ResponseEntity<LoanApprovalRequestDto> reject(
            @PathVariable Long requestId,
            @RequestBody(required = false) Map<String, String> body
    ) {
        String remarks = body == null ? null : body.get("remarks");

        return ResponseEntity.ok(
                approvalService.reject(requestId, remarks)
        );
    }
    // USER: SUBMIT PAY EMI APPROVAL REQUEST
    @PostMapping("/api/loan-approval-requests/repayment/pay")
    public ResponseEntity<LoanApprovalRequestDto> submitPayEmiRequest(
            @RequestBody Map<String, Object> body
    ) {
        Long repaymentId = getLong(body, "repaymentId");

        Double paidAmount = getDouble(body, "paidAmount");
        Double penaltyAmount = getDouble(body, "penaltyAmount");
        Boolean regularRepayment = getBoolean(body, "regularRepayment");

        return ResponseEntity.status(HttpStatus.CREATED).body(
                approvalService.submitPayEmiRequest(
                        repaymentId,
                        paidAmount,
                        penaltyAmount,
                        regularRepayment
                )
        );
    }


    // USER: SUBMIT EDIT EMI APPROVAL REQUEST
    @PostMapping("/api/loan-approval-requests/repayment/edit")
    public ResponseEntity<LoanApprovalRequestDto> submitEditPaidEmiRequest(
            @RequestBody Map<String, Object> body
    ) {
        Long repaymentId = getLong(body, "repaymentId");

        Double paidAmount = getDouble(body, "paidAmount");
        Double penaltyAmount = getDouble(body, "penaltyAmount");

        return ResponseEntity.status(HttpStatus.CREATED).body(
                approvalService.submitEditPaidEmiRequest(
                        repaymentId,
                        paidAmount,
                        penaltyAmount
                )
        );
    }


    private Long getLong(Map<String, Object> body, String key) {
        Object value = body.get(key);

        if (value instanceof Number number) {
            return number.longValue();
        }

        throw new org.springframework.web.server.ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                key + " is required."
        );
    }


    private Double getDouble(Map<String, Object> body, String key) {
        Object value = body.get(key);

        if (value instanceof Number number) {
            return number.doubleValue();
        }

        throw new org.springframework.web.server.ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                key + " is required."
        );
    }


    private Boolean getBoolean(Map<String, Object> body, String key) {
        Object value = body.get(key);

        if (value == null) {
            return false;
        }

        if (value instanceof Boolean bool) {
            return bool;
        }

        throw new org.springframework.web.server.ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                key + " must be true or false."
        );
    }
}
