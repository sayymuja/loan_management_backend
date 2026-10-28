
package com.example.loan_management.controller;

import com.example.loan_management.dto.FundTransactionRequest;
import com.example.loan_management.entity.FundAccount;
import com.example.loan_management.entity.FundTransaction;
import com.example.loan_management.service.FundManagementService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/fund-management")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class FundManagementController {

    private final FundManagementService fundManagementService;

    @GetMapping("/account/{voAlfId}")
    public ResponseEntity<FundAccount> getAccount(
            @PathVariable Long voAlfId) {

        return ResponseEntity.ok(
                fundManagementService.getAccount(voAlfId)
        );
    }

    @PostMapping("/transactions")
    public ResponseEntity<FundTransaction> submitTransaction(
            @Valid @RequestBody FundTransactionRequest request) {

        return ResponseEntity.ok(
                fundManagementService.submitTransaction(request)
        );
    }

    @GetMapping("/transactions/{voAlfId}")
    public ResponseEntity<List<FundTransaction>> getTransactions(
            @PathVariable Long voAlfId) {

        return ResponseEntity.ok(
                fundManagementService.getTransactions(voAlfId)
        );
    }

    // Get pending Receipt/Payment approval requests
    @GetMapping("/approvals/pending")
    public ResponseEntity<List<Map<String, Object>>>
    getPendingFundApprovals() {

        return ResponseEntity.ok(
                fundManagementService.getPendingFundApprovals()
        );
    }

    // Approve Receipt/Payment
    @PostMapping("/approvals/{requestId}/approve")
    public ResponseEntity<Map<String, Object>> approveFundRequest(
            @PathVariable Long requestId,
            @RequestBody(required = false) Map<String, String> body) {

        String remarks = body != null
                ? body.get("remarks")
                : "Approved by Admin";

        return ResponseEntity.ok(
                fundManagementService.approveFundRequest(
                        requestId, remarks
                )
        );
    }

    // Reject Receipt/Payment
    @PostMapping("/approvals/{requestId}/reject")
    public ResponseEntity<Map<String, Object>> rejectFundRequest(
            @PathVariable Long requestId,
            @RequestBody(required = false) Map<String, String> body) {

        String remarks = body != null
                ? body.get("remarks")
                : "Rejected by Admin";

        return ResponseEntity.ok(
                fundManagementService.rejectFundRequest(
                        requestId, remarks
                )
        );
    }
}
