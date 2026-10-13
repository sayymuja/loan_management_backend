
package com.example.loan_management.repository;

import com.example.loan_management.entity.LoanApprovalRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LoanApprovalRequestRepository
        extends JpaRepository<LoanApprovalRequest, Long> {

    List<LoanApprovalRequest> findByCmrcIdOrderByCreatedAtDesc(
            Long cmrcId
    );

    List<LoanApprovalRequest> findByCmrcIdAndStatusOrderByCreatedAtDesc(
            Long cmrcId,
            String status
    );

    List<LoanApprovalRequest> findByRequestedBy_IdOrderByCreatedAtDesc(
            Long userId
    );

    Optional<LoanApprovalRequest> findByIdAndCmrcId(
            Long id,
            Long cmrcId
    );

    boolean existsByLoanIdAndStatusAndRequestType(
            Long loanId,
            String status,
            String requestType
    );
}
