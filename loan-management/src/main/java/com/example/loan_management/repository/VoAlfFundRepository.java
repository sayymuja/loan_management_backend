package com.example.loan_management.repository;

import com.example.loan_management.entity.VoAlfFund;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface VoAlfFundRepository extends JpaRepository<VoAlfFund, Long> {
    Optional<VoAlfFund> findByVoAlfId(Long voAlfId);
}