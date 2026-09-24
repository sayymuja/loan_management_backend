package com.example.loan_management.repository;

import com.example.loan_management.entity.ClSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClScheduleRepository extends JpaRepository<ClSchedule, Long> {

    List<ClSchedule> findByLoanId(Long loanId);
}