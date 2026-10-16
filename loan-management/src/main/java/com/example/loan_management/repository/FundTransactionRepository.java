
package com.example.loan_management.repository;

import com.example.loan_management.entity.FundTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

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

    Optional<FundTransaction> findByApprovalRequestId(Long approvalRequestId);

    Optional<FundTransaction> findByPaymentEventId(String paymentEventId);
}
