
package com.example.loan_management.controller;

import com.example.loan_management.entity.FundApprovalRequest;
import com.example.loan_management.service.FundApprovalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/fund-approvals")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class FundApprovalController {

    private final FundApprovalService fundApprovalService;

    /**
     * USER: View my submitted fund approval requests.
     */
    @GetMapping("/my")
    public ResponseEntity<List<FundApprovalRequest>> getMyRequests() {
        return ResponseEntity.ok(
                fundApprovalService.getMyRequests()
        );
    }

    /**
     * ADMIN: View pending requests for the ADMIN's CMRC.
     */
    @GetMapping("/pending")
    public ResponseEntity<List<FundApprovalRequest>> getPendingRequests() {
        return ResponseEntity.ok(
                fundApprovalService.getPendingRequests()
        );
    }

    /**
     * ADMIN: Approve a fund transaction request.
     */
    @PostMapping("/{requestId}/approve")
    public ResponseEntity<FundApprovalRequest> approveRequest(
            @PathVariable Long requestId,
            @RequestBody(required = false) Map<String, String> body) {

        String remarks = body != null
                ? body.get("adminRemarks")
                : null;

        return ResponseEntity.ok(
                fundApprovalService.approveRequest(
                        requestId, remarks
                )
        );
    }

    /**
     * ADMIN: Reject a fund transaction request.
     */
    @PostMapping("/{requestId}/reject")
    public ResponseEntity<FundApprovalRequest> rejectRequest(
            @PathVariable Long requestId,
            @RequestBody(required = false) Map<String, String> body) {

        String remarks = body != null
                ? body.get("adminRemarks")
                : null;

        return ResponseEntity.ok(
                fundApprovalService.rejectRequest(
                        requestId, remarks
                )
        );
    }
}
