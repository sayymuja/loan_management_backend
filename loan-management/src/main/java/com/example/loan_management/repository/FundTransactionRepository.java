
package com.example.loan_management.repository;

import com.example.loan_management.entity.FundTransaction;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface FundTransactionRepository
        extends JpaRepository<FundTransaction, Long> {

    boolean existsByPaymentEventId(String paymentEventId);

    List<FundTransaction>
    findByFundAccountIdOrderByTransactionDateDescIdDesc(
            Long fundAccountId
    );

    List<FundTransaction>
    findByFundAccountIdAndTransactionDateBetweenOrderByTransactionDateAscIdAsc(
            Long fundAccountId,
            LocalDate startDate,
            LocalDate endDate
    );

    boolean existsByReferenceTypeAndReferenceIdAndCategory(
            String referenceType,
            Long referenceId,
            FundTransaction.FundCategory category
    );

    Optional<FundTransaction> findByApprovalRequestId(
            Long approvalRequestId
    );

    Optional<FundTransaction> findByPaymentEventId(
            String paymentEventId
    );

    // Lock transaction row during approval or rejection.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
           SELECT t
           FROM FundTransaction t
           WHERE t.approvalRequestId = :approvalRequestId
           """)
    Optional<FundTransaction> findByApprovalRequestIdForUpdate(
            @Param("approvalRequestId") Long approvalRequestId
    );
}
