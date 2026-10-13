
package com.example.loan_management.controller;

import com.example.loan_management.auth.entity.User;
import com.example.loan_management.auth.service.CurrentUserService;
import com.example.loan_management.dto.LoanDto;
import com.example.loan_management.dto.LoanApprovalRequestDto;
import com.example.loan_management.service.LoanApprovalService;
import com.example.loan_management.service.LoanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/api/loan")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class LoanController {

    private final LoanService loanService;
    private final LoanApprovalService loanApprovalService;
    private final CurrentUserService currentUserService;

    // =========================================================
    // CREATE
    // USER -> PENDING APPROVAL REQUEST
    // ADMIN -> DIRECT CREATE
    // =========================================================

    @PostMapping
    public ResponseEntity<?> create(@RequestBody LoanDto loanDto) {
        String role = getCurrentRole();

        if ("USER".equals(role)) {
            LoanApprovalRequestDto request =
                    loanApprovalService.submitCreateRequest(loanDto);

            return ResponseEntity.status(HttpStatus.CREATED).body(request);
        }

        if ("ADMIN".equals(role)) {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(loanService.create(loanDto));
        }

        throw forbidden();
    }

    // =========================================================
    // GET ALL
    // =========================================================

    @GetMapping
    public List<LoanDto> getAll() {
        return loanService.getAll();
    }

    // =========================================================
    // GET BY ID
    // =========================================================

    @GetMapping("/{id}")
    public LoanDto getById(@PathVariable Long id) {
        return loanService.getById(id);
    }

    // =========================================================
    // UPDATE
    // USER -> PENDING APPROVAL REQUEST
    // ADMIN -> DIRECT UPDATE
    // =========================================================

    @PutMapping("/{id}")
    public ResponseEntity<?> update(
            @PathVariable Long id,
            @RequestBody LoanDto loanDto
    ) {
        String role = getCurrentRole();

        if ("USER".equals(role)) {
            LoanApprovalRequestDto request =
                    loanApprovalService.submitUpdateRequest(id, loanDto);

            return ResponseEntity.status(HttpStatus.CREATED).body(request);
        }

        if ("ADMIN".equals(role)) {
            return ResponseEntity.ok(loanService.update(id, loanDto));
        }

        throw forbidden();
    }

    // =========================================================
    // DELETE
    // USER -> PENDING APPROVAL REQUEST
    // ADMIN -> DIRECT DELETE
    // =========================================================

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        String role = getCurrentRole();

        if ("USER".equals(role)) {
            LoanApprovalRequestDto request =
                    loanApprovalService.submitDeleteRequest(id);

            return ResponseEntity.status(HttpStatus.CREATED).body(request);
        }

        if ("ADMIN".equals(role)) {
            loanService.delete(id);
            return ResponseEntity.ok("Loan deleted successfully");
        }

        throw forbidden();
    }

    // =========================================================
    // GET LOANS BY WOMAN
    // =========================================================

    @GetMapping("/woman/{womanId}")
    public ResponseEntity<List<LoanDto>> getByWomanId(
            @PathVariable Long womanId
    ) {
        return ResponseEntity.ok(loanService.getByWomanId(womanId));
    }

    // =========================================================
    // ROLE HELPERS
    // =========================================================

    private String getCurrentRole() {
        User user = currentUserService.getCurrentUser();

        if (user == null || user.getRole() == null) {
            throw forbidden();
        }

        return user.getRole()
                .trim()
                .replace("ROLE_", "")
                .toUpperCase(Locale.ROOT);
    }

    private ResponseStatusException forbidden() {
        return new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "You are not allowed to perform this loan operation"
        );
    }
}
