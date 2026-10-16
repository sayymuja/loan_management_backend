
package com.example.loan_management.repository;

import com.example.loan_management.entity.FundAccount;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface FundAccountRepository
        extends JpaRepository<FundAccount, Long> {

    Optional<FundAccount> findByVoAlfId(Long voAlfId);

    boolean existsByVoAlfId(Long voAlfId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT fa
        FROM FundAccount fa
        WHERE fa.voAlf.id = :voAlfId
        """)
    Optional<FundAccount> findByVoAlfIdForUpdate(
            @Param("voAlfId") Long voAlfId
    );
}
