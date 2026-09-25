package com.example.loan_management.service.impl;

import com.example.loan_management.dto.ClScheduleDto;
import com.example.loan_management.entity.ClSchedule;
import com.example.loan_management.entity.Loan;
import com.example.loan_management.repository.ClScheduleRepository;
import com.example.loan_management.repository.LoanRepository;
import com.example.loan_management.service.ClScheduleService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClScheduleServiceImpl implements ClScheduleService {

    private final ClScheduleRepository clScheduleRepository;
    private final LoanRepository loanRepository;
    private final ModelMapper modelMapper;

    @Override
    public ClScheduleDto create(ClScheduleDto dto) {

        Loan loan = loanRepository.findById(dto.getLoanId())
                .orElseThrow(() -> new RuntimeException("Loan not found"));

        ClSchedule schedule = modelMapper.map(dto, ClSchedule.class);

        schedule.setLoan(loan);

        ClSchedule saved = clScheduleRepository.save(schedule);

        ClScheduleDto response =
                modelMapper.map(saved, ClScheduleDto.class);

        response.setLoanId(saved.getLoan().getId());

        return response;
    }

    @Override
    public List<ClScheduleDto> getAll() {

        return clScheduleRepository.findAll()
                .stream()
                .map(schedule -> {

                    ClScheduleDto dto =
                            modelMapper.map(schedule, ClScheduleDto.class);

                    dto.setLoanId(schedule.getLoan().getId());

                    return dto;
                })
                .toList();
    }

    @Override
    public ClScheduleDto getById(Long id) {

        ClSchedule schedule = clScheduleRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("CL Schedule not found"));

        ClScheduleDto dto =
                modelMapper.map(schedule, ClScheduleDto.class);

        dto.setLoanId(schedule.getLoan().getId());

        return dto;
    }

    @Override
    public ClScheduleDto update(Long id, ClScheduleDto dto) {

        ClSchedule schedule = clScheduleRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("CL Schedule not found"));

        Loan loan = loanRepository.findById(dto.getLoanId())
                .orElseThrow(() -> new RuntimeException("Loan not found"));

        schedule.setLoan(loan);
        schedule.setInstallmentNo(dto.getInstallmentNo());
        schedule.setOutstandingAmount(dto.getOutstandingAmount());
        schedule.setPrincipalAmount(dto.getPrincipalAmount());
        schedule.setInterestAmount(dto.getInterestAmount());
        schedule.setMonthlyInstallment(dto.getMonthlyInstallment());
        schedule.setAverageMonthlyInstallment(
                dto.getAverageMonthlyInstallment()
        );
        schedule.setRemark(dto.getRemark());

        ClSchedule updated =
                clScheduleRepository.save(schedule);

        ClScheduleDto response =
                modelMapper.map(updated, ClScheduleDto.class);

        response.setLoanId(updated.getLoan().getId());

        return response;
    }

    @Override
    public void delete(Long id) {

        ClSchedule schedule = clScheduleRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("CL Schedule not found"));

        clScheduleRepository.delete(schedule);
    }
    @Override
    public List<ClScheduleDto> generateSchedule(Long loanId) {

        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found"));

        List<ClSchedule> existingSchedules =
                clScheduleRepository.findByLoanId(loanId);

        if (!existingSchedules.isEmpty()) {
            clScheduleRepository.deleteAll(existingSchedules);
        }

        int months = loan.getRepaymentPeriodMonths();

        double loanAmount = loan.getLoanAmount();
        double annualInterestRate = loan.getInterestRate();

        // Monthly interest rate
        double monthlyRate = annualInterestRate / 100 / 12;

        // EMI calculation
        double emi = loanAmount * monthlyRate *
                Math.pow(1 + monthlyRate, months)
                / (Math.pow(1 + monthlyRate, months) - 1);

        List<ClScheduleDto> result = new java.util.ArrayList<>();

        double outstanding = loanAmount;

        for (int i = 1; i <= months; i++) {

            // Interest on current outstanding balance
            double interest = outstanding * monthlyRate;

            // Principal = EMI - Interest
            double principal = emi - interest;

            // Last installment adjustment
            if (i == months) {
                principal = outstanding;
            }

            double monthlyInstallment = principal + interest;

            ClSchedule schedule = new ClSchedule();

            schedule.setLoan(loan);
            schedule.setInstallmentNo(i);

            // Round to 2 decimal places
            schedule.setOutstandingAmount(
                    Math.round(outstanding * 100.0) / 100.0
            );

            schedule.setPrincipalAmount(
                    Math.round(principal * 100.0) / 100.0
            );

            schedule.setInterestAmount(
                    Math.round(interest * 100.0) / 100.0
            );

            schedule.setMonthlyInstallment(
                    Math.round(monthlyInstallment * 100.0) / 100.0
            );

            schedule.setAverageMonthlyInstallment(0.0);

            schedule.setRemark("");

            ClSchedule saved =
                    clScheduleRepository.save(schedule);

            ClScheduleDto dto =
                    modelMapper.map(saved, ClScheduleDto.class);

            dto.setLoanId(loanId);

            result.add(dto);

            outstanding = outstanding - principal;
        }

        return result;
    }
    @Override
    public List<ClScheduleDto> getByLoanId(Long loanId) {

        return clScheduleRepository.findByLoanId(loanId)
                .stream()
                .map(schedule -> {
                    ClScheduleDto dto =
                            modelMapper.map(schedule, ClScheduleDto.class);

                    dto.setLoanId(schedule.getLoan().getId());

                    return dto;
                })
                .toList();
    }
}