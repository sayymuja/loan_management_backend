package com.example.loan_management.service.impl;

import com.example.loan_management.dto.RepaymentDto;
import com.example.loan_management.dto.RepaymentSummaryDto;
import com.example.loan_management.entity.Loan;
import com.example.loan_management.entity.Repayment;
import com.example.loan_management.repository.LoanRepository;
import com.example.loan_management.repository.RepaymentRepository;
import com.example.loan_management.service.RepaymentService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RepaymentServiceImpl implements RepaymentService {

    private final RepaymentRepository repaymentRepository;
    private final LoanRepository loanRepository;
    private final ModelMapper modelMapper;

    @Override
    public RepaymentDto create(RepaymentDto repaymentDto) {

        Loan loan = loanRepository.findById(repaymentDto.getLoanId())
                .orElseThrow(() -> new RuntimeException("Loan not found"));

        Repayment repayment = modelMapper.map(repaymentDto, Repayment.class);

        repayment.setLoan(loan);

        Repayment savedRepayment = repaymentRepository.save(repayment);

        RepaymentDto response =
                modelMapper.map(savedRepayment, RepaymentDto.class);

        response.setLoanId(savedRepayment.getLoan().getId());

        return response;
    }

    @Override
    public List<RepaymentDto> getAll() {

        return repaymentRepository.findAll()
                .stream()
                .map(repayment -> {

                    RepaymentDto dto =
                            modelMapper.map(repayment, RepaymentDto.class);

                    dto.setLoanId(repayment.getLoan().getId());

                    return dto;
                })
                .toList();
    }

    @Override
    public RepaymentDto getById(Long id) {

        Repayment repayment = repaymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Repayment not found"));

        RepaymentDto dto =
                modelMapper.map(repayment, RepaymentDto.class);

        dto.setLoanId(repayment.getLoan().getId());

        return dto;
    }

    @Override
    public RepaymentDto update(Long id, RepaymentDto repaymentDto) {

        Repayment repayment = repaymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Repayment not found"));

        Loan loan = loanRepository.findById(repaymentDto.getLoanId())
                .orElseThrow(() -> new RuntimeException("Loan not found"));

        repayment.setLoan(loan);
        repayment.setPaymentDate(repaymentDto.getPaymentDate());
        repayment.setPrincipalAmount(repaymentDto.getPrincipalAmount());
        repayment.setInterestAmount(repaymentDto.getInterestAmount());
        repayment.setTotalAmount(repaymentDto.getTotalAmount());
        repayment.setRegularRepayment(repaymentDto.getRegularRepayment());
        repayment.setPenaltyAmount(repaymentDto.getPenaltyAmount());
        repayment.setRemark(repaymentDto.getRemark());

        Repayment updatedRepayment =
                repaymentRepository.save(repayment);

        RepaymentDto response =
                modelMapper.map(updatedRepayment, RepaymentDto.class);

        response.setLoanId(updatedRepayment.getLoan().getId());

        return response;
    }

    @Override
    public void delete(Long id) {

        Repayment repayment = repaymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Repayment not found"));

        repaymentRepository.delete(repayment);
    }
    @Override
    public RepaymentSummaryDto getSummary(Long loanId) {

        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found"));

        List<Repayment> repayments =
                repaymentRepository.findByLoanId(loanId);

        double totalPrincipal = repayments.stream()
                .mapToDouble(r -> r.getPrincipalAmount() != null
                        ? r.getPrincipalAmount() : 0)
                .sum();

        double totalInterest = repayments.stream()
                .mapToDouble(r -> r.getInterestAmount() != null
                        ? r.getInterestAmount() : 0)
                .sum();

        double totalRepayment = repayments.stream()
                .mapToDouble(r -> r.getTotalAmount() != null
                        ? r.getTotalAmount() : 0)
                .sum();

        double totalPenalty = repayments.stream()
                .mapToDouble(r -> r.getPenaltyAmount() != null
                        ? r.getPenaltyAmount() : 0)
                .sum();

        double outstandingAmount =
                loan.getLoanAmount() - totalPrincipal;

        RepaymentSummaryDto summary = new RepaymentSummaryDto();

        summary.setLoanId(loanId);
        summary.setLoanAmount(loan.getLoanAmount());
        summary.setTotalPrincipalPaid(totalPrincipal);
        summary.setTotalInterestPaid(totalInterest);
        summary.setTotalRepayment(totalRepayment);
        summary.setOutstandingAmount(outstandingAmount);
        summary.setTotalPenalty(totalPenalty);

        return summary;
    }
}