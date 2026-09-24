package com.example.loan_management.service;

import com.example.loan_management.dto.RepaymentDto;
import com.example.loan_management.dto.RepaymentSummaryDto;

import java.util.List;

public interface RepaymentService {

    RepaymentDto create(RepaymentDto repaymentDto);

    List<RepaymentDto> getAll();

    RepaymentDto getById(Long id);

    RepaymentDto update(Long id, RepaymentDto repaymentDto);

    RepaymentSummaryDto getSummary(Long loanId);
    void delete(Long id);
}