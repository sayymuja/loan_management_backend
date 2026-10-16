        package com.example.loan_management.repository;

import com.example.loan_management.entity.LoanApprovalRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LoanApprovalRequestRepository
        extends JpaRepository<LoanApprovalRequest, Long> {

    // =========================================================
    // GET ALL REQUESTS BY CMRC
    // =========================================================

    List<LoanApprovalRequest> findByCmrcIdOrderByCreatedAtDesc(
            Long cmrcId
    );

    // =========================================================
    // GET REQUESTS BY CMRC AND STATUS
    // Example: PENDING, APPROVED, REJECTED
    // =========================================================

    List<LoanApprovalRequest> findByCmrcIdAndStatusOrderByCreatedAtDesc(
            Long cmrcId,
            String status
    );

    // =========================================================
    // GET REQUESTS SUBMITTED BY USER
    // =========================================================

    List<LoanApprovalRequest> findByRequestedBy_IdOrderByCreatedAtDesc(
            Long userId
    );

    // =========================================================
    // GET REQUEST BY ID AND CMRC
    // =========================================================

    Optional<LoanApprovalRequest> findByIdAndCmrcId(
            Long id,
            Long cmrcId
    );

    // =========================================================
    // CHECK EXISTING LOAN REQUEST
    // =========================================================

    boolean existsByLoanIdAndStatusAndRequestType(
            Long loanId,
            String status,
            String requestType
    );
}
