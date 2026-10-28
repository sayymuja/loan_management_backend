package com.example.loan_management.auth.controller;

import com.example.loan_management.auth.dto.UserApprovalDto;
import com.example.loan_management.auth.service.UserApprovalService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
@PreAuthorize("hasRole('ADMIN')")
public class UserApprovalController {

    private final UserApprovalService userApprovalService;

    // =========================================================
    // ADMIN: VIEW PENDING USERS OF OWN CMRC
    // GET /api/admin/users/pending
    // =========================================================

    @GetMapping("/pending")
    public ResponseEntity<List<UserApprovalDto>> getPendingUsers() {

        return ResponseEntity.ok(
                userApprovalService.getPendingUsers()
        );
    }

    // =========================================================
    // ADMIN: APPROVE USER
    // POST /api/admin/users/{userId}/approve
    // =========================================================

    @PostMapping("/{userId}/approve")
    public ResponseEntity<UserApprovalDto> approveUser(
            @PathVariable Long userId) {

        return ResponseEntity.ok(
                userApprovalService.approveUser(userId)
        );
    }

    // =========================================================
    // ADMIN: REJECT USER
    // POST /api/admin/users/{userId}/reject
    // =========================================================

    @PostMapping("/{userId}/reject")
    public ResponseEntity<UserApprovalDto> rejectUser(
            @PathVariable Long userId) {

        return ResponseEntity.ok(
                userApprovalService.rejectUser(userId)
        );
    }
}