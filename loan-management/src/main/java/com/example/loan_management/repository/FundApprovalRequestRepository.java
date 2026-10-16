
package com.example.loan_management.repository;

import com.example.loan_management.entity.FundApprovalRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FundApprovalRequestRepository
        extends JpaRepository<FundApprovalRequest, Long> {

    List<FundApprovalRequest>
    findByCmrcIdAndStatusOrderByCreatedAtDesc(
            Long cmrcId,
            String status
    );

    List<FundApprovalRequest>
    findByRequestedBy_IdOrderByCreatedAtDesc(
            Long userId
    );

    Optional<FundApprovalRequest>
    findByIdAndCmrcId(Long id, Long cmrcId);
}
