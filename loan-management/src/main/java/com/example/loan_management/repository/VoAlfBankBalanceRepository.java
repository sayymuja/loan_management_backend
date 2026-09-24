package com.example.loan_management.repository;

import com.example.loan_management.entity.VoAlfBankBalance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VoAlfBankBalanceRepository
        extends JpaRepository<VoAlfBankBalance, Long> {

    List<VoAlfBankBalance> findByVoAlfId(Long voAlfId);
}