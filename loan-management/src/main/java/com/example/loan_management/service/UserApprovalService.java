package com.example.loan_management.auth.service;

import com.example.loan_management.auth.dto.UserApprovalDto;

import java.util.List;

public interface UserApprovalService {

    List<UserApprovalDto> getPendingUsers();

    UserApprovalDto approveUser(Long userId);

    UserApprovalDto rejectUser(Long userId);
}