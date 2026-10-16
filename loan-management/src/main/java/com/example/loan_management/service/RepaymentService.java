        package com.example.loan_management.service;

import com.example.loan_management.dto.RepaymentDto;
import com.example.loan_management.dto.RepaymentSummaryDto;

import java.util.List;

public interface RepaymentService {

    // =========================================================
    // CREATE REPAYMENT
    // =========================================================

    RepaymentDto create(RepaymentDto repaymentDto);

    // =========================================================
    // GET ALL REPAYMENTS
    // =========================================================

    List<RepaymentDto> getAll();

    // =========================================================
    // GET REPAYMENT BY ID
    // =========================================================

    RepaymentDto getById(Long id);

    // =========================================================
    // UPDATE REPAYMENT
    // =========================================================

    RepaymentDto update(Long id, RepaymentDto repaymentDto);

    // =========================================================
    // GET REPAYMENT SUMMARY
    // =========================================================

    RepaymentSummaryDto getSummary(Long loanId);

    // =========================================================
    // DELETE REPAYMENT
    // =========================================================

    void delete(Long id);

    // =========================================================
    // GET REPAYMENTS BY LOAN
    // =========================================================

    List<RepaymentDto> getByLoanId(Long loanId);

    // =========================================================
    // GENERATE REPAYMENT SCHEDULE
    // =========================================================

    List<RepaymentDto> generateSchedule(Long loanId);

    // =========================================================
    // PAY EMI - BACKWARD COMPATIBILITY
    // =========================================================

    RepaymentDto payEmi(
            Long repaymentId,
            Double paidAmount,
            Double penaltyAmount,
            Boolean regularRepayment
    );

    // =========================================================
    // PAY EMI - IDEMPOTENT PAYMENT PROCESSING
    // =========================================================

    RepaymentDto payEmi(
            Long repaymentId,
            Double paidAmount,
            Double penaltyAmount,
            Boolean regularRepayment,
            String paymentEventId
    );

    // =========================================================
    // EDIT PAID EMI
    // =========================================================

    RepaymentDto editPaidEmi(
            Long repaymentId,
            Double paidAmount,
            Double penaltyAmount
    );
}
