package com.example.loan_management.repository;

import com.example.loan_management.entity.Repayment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RepaymentRepository extends JpaRepository<Repayment, Long> {

    List<Repayment> findByLoanId(Long loanId);

    void deleteByLoanId(Long loanId);
}