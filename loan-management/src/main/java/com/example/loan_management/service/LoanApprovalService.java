
package com.example.loan_management.service;

import com.example.loan_management.dto.LoanApprovalRequestDto;
import com.example.loan_management.dto.LoanDto;

import java.util.List;

public interface LoanApprovalService {

    LoanApprovalRequestDto submitCreateRequest(LoanDto loanDto);

    LoanApprovalRequestDto submitUpdateRequest(
            Long loanId,
            LoanDto loanDto
    );

    LoanApprovalRequestDto submitDeleteRequest(Long loanId);

    List<LoanApprovalRequestDto> getPendingRequests();

    List<LoanApprovalRequestDto> getMyRequests();

    LoanApprovalRequestDto approve(
            Long requestId,
            String remarks
    );

    LoanApprovalRequestDto reject(
            Long requestId,
            String remarks
    );
    LoanApprovalRequestDto submitPayEmiRequest(
            Long repaymentId,
            Double paidAmount,
            Double penaltyAmount,
            Boolean regularRepayment
    );

    LoanApprovalRequestDto submitEditPaidEmiRequest(
            Long repaymentId,
            Double paidAmount,
            Double penaltyAmount
    );
}
