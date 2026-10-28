
package com.example.loan_management.repository;

import com.example.loan_management.entity.FundApprovalRequest;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
    findByRequestedBy_IdOrderByCreatedAtDesc(Long userId);

    Optional<FundApprovalRequest> findByIdAndCmrcId(
            Long id,
            Long cmrcId
    );

    // Lock approval row while approving or rejecting.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
           SELECT f
           FROM FundApprovalRequest f
           WHERE f.id = :id
             AND f.cmrcId = :cmrcId
           """)
    Optional<FundApprovalRequest> findByIdAndCmrcIdForUpdate(
            @Param("id") Long id,
            @Param("cmrcId") Long cmrcId
    );
}
