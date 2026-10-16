        package com.example.loan_management.service;

import com.example.loan_management.entity.FundApprovalRequest;

import java.util.List;

public interface FundApprovalService {

    // Get pending fund approval requests for ADMIN
    List<FundApprovalRequest> getPendingRequests();

    // Get requests submitted by the logged-in user
    List<FundApprovalRequest> getMyRequests();

    // Approve a fund transaction request
    FundApprovalRequest approveRequest(
            Long requestId,
            String adminRemarks
    );

    // Reject a fund transaction request
    FundApprovalRequest rejectRequest(
            Long requestId,
            String adminRemarks
    );
}
