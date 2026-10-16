
package com.example.loan_management.controller;

import com.example.loan_management.entity.FundAccount;
import com.example.loan_management.entity.FundTransaction;
import com.example.loan_management.dto.FundTransactionRequest;
import com.example.loan_management.service.FundManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/fund-management")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class FundManagementController {

    private final FundManagementService fundManagementService;

    /**
     * Get an existing fund account or create one for the VO/ALF.
     */
    @GetMapping("/account/{voAlfId}")
    public ResponseEntity<FundAccount> getAccount(
            @PathVariable Long voAlfId) {

        return ResponseEntity.ok(
                fundManagementService.getOrCreateAccount(voAlfId)
        );
    }

    /**
     * Submit a Receipt or Payment transaction.
     * USER requests remain pending until approved.
     */
    @PostMapping("/transactions")
    public ResponseEntity<FundTransaction> submitTransaction(
            @Valid @RequestBody FundTransactionRequest request) {

        FundTransaction transaction =
                fundManagementService.submitTransaction(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(transaction);
    }

    /**
     * Get transaction history for a VO/ALF.
     */
    @GetMapping("/transactions/{voAlfId}")
    public ResponseEntity<List<FundTransaction>> getTransactions(
            @PathVariable Long voAlfId) {

        return ResponseEntity.ok(
                fundManagementService.getTransactions(voAlfId)
        );
    }
}
