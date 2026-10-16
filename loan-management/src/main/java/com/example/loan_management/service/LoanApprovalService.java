        package com.example.loan_management.service;

import com.example.loan_management.dto.LoanApprovalRequestDto;
import com.example.loan_management.dto.LoanDto;

import java.util.List;

public interface LoanApprovalService {

    // =========================================================
    // LOAN APPROVAL REQUESTS
    // =========================================================

    LoanApprovalRequestDto submitCreateRequest(LoanDto loanDto);

    LoanApprovalRequestDto submitUpdateRequest(
            Long loanId,
            LoanDto loanDto
    );

    LoanApprovalRequestDto submitDeleteRequest(Long loanId);

    // =========================================================
    // GET REQUESTS
    // =========================================================

    List<LoanApprovalRequestDto> getPendingRequests();

    List<LoanApprovalRequestDto> getMyRequests();

    // =========================================================
    // APPROVE / REJECT
    // =========================================================

    LoanApprovalRequestDto approve(
            Long requestId,
            String remarks
    );

    LoanApprovalRequestDto reject(
            Long requestId,
            String remarks
    );

    // =========================================================
    // EMI PAYMENT APPROVAL
    // No CASH/BANK paymentMode
    // =========================================================

    LoanApprovalRequestDto submitPayEmiRequest(
            Long repaymentId,
            Double paidAmount,
            Double penaltyAmount,
            Boolean regularRepayment
    );

    // =========================================================
    // EDIT PAID EMI REQUEST
    // =========================================================

    LoanApprovalRequestDto submitEditPaidEmiRequest(
            Long repaymentId,
            Double paidAmount,
            Double penaltyAmount
    );
}
