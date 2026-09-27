package com.example.loan_management.service;

import com.example.loan_management.dto.ClScheduleDto;
import com.example.loan_management.entity.ClSchedule;
import com.example.loan_management.entity.Loan;
import com.example.loan_management.repository.ClScheduleRepository;
import com.example.loan_management.repository.LoanRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClScheduleServiceImpl implements ClScheduleService {

    private final ClScheduleRepository clScheduleRepository;
    private final LoanRepository loanRepository;
    private final ModelMapper modelMapper;


    // ==========================================
    // CREATE
    // ==========================================

    @Override
    public ClScheduleDto create(ClScheduleDto dto) {

        Loan loan = loanRepository.findById(dto.getLoanId())
                .orElseThrow(() ->
                        new RuntimeException("Loan not found"));

        ClSchedule schedule =
                modelMapper.map(dto, ClSchedule.class);

        schedule.setLoan(loan);

        ClSchedule saved =
                clScheduleRepository.save(schedule);

        ClScheduleDto response =
                modelMapper.map(saved, ClScheduleDto.class);

        response.setLoanId(saved.getLoan().getId());

        return response;
    }


    // ==========================================
    // GET ALL
    // ==========================================

    @Override
    public List<ClScheduleDto> getAll() {

        return clScheduleRepository.findAll()
                .stream()
                .map(schedule -> {

                    ClScheduleDto dto =
                            modelMapper.map(
                                    schedule,
                                    ClScheduleDto.class
                            );

                    dto.setLoanId(
                            schedule.getLoan().getId()
                    );

                    return dto;

                })
                .toList();
    }


    // ==========================================
    // GET BY ID
    // ==========================================

    @Override
    public ClScheduleDto getById(Long id) {

        ClSchedule schedule =
                clScheduleRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "CL Schedule not found"
                                )
                        );

        ClScheduleDto dto =
                modelMapper.map(
                        schedule,
                        ClScheduleDto.class
                );

        dto.setLoanId(
                schedule.getLoan().getId()
        );

        return dto;
    }


    // ==========================================
    // UPDATE
    // ==========================================

    @Override
    public ClScheduleDto update(
            Long id,
            ClScheduleDto dto) {

        ClSchedule schedule =
                clScheduleRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "CL Schedule not found"
                                )
                        );

        Loan loan =
                loanRepository.findById(dto.getLoanId())
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Loan not found"
                                )
                        );

        schedule.setLoan(loan);

        schedule.setInstallmentNo(
                dto.getInstallmentNo()
        );

        schedule.setOutstandingAmount(
                dto.getOutstandingAmount()
        );

        schedule.setPrincipalAmount(
                dto.getPrincipalAmount()
        );

        schedule.setInterestAmount(
                dto.getInterestAmount()
        );

        schedule.setMonthlyInstallment(
                dto.getMonthlyInstallment()
        );

        schedule.setAverageMonthlyInstallment(
                dto.getAverageMonthlyInstallment()
        );

        schedule.setRemark(
                dto.getRemark()
        );

        ClSchedule updated =
                clScheduleRepository.save(schedule);

        ClScheduleDto response =
                modelMapper.map(
                        updated,
                        ClScheduleDto.class
                );

        response.setLoanId(
                updated.getLoan().getId()
        );

        return response;
    }


    // ==========================================
    // DELETE BY ID
    // ==========================================

    @Override
    public void delete(Long id) {

        ClSchedule schedule =
                clScheduleRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "CL Schedule not found"
                                )
                        );

        clScheduleRepository.delete(schedule);
    }


    // ==========================================
    // GENERATE / REPLACE CL SCHEDULE
    // ==========================================

    @Override
    @Transactional
    public List<ClScheduleDto> generateSchedule(
            Long loanId) {

        // ==========================================
        // 1. FIND LOAN
        // ==========================================

        Loan loan =
                loanRepository.findById(loanId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Loan not found"
                                )
                        );


        // ==========================================
        // 2. DELETE EXISTING SCHEDULE
        // ==========================================

        clScheduleRepository.deleteByLoanId(loanId);


        // ==========================================
        // 3. GET LOAN DETAILS
        // ==========================================

        int months =
                loan.getRepaymentPeriodMonths();

        double loanAmount =
                loan.getLoanAmount();

        double annualInterestRate =
                loan.getInterestRate();


        // ==========================================
        // 4. MONTHLY INTEREST RATE
        // ==========================================

        double monthlyRate =
                annualInterestRate / 100 / 12;


        // ==========================================
        // 5. EMI CALCULATION
        // ==========================================

        double emi =
                loanAmount
                        * monthlyRate
                        * Math.pow(
                        1 + monthlyRate,
                        months
                )
                        /
                        (
                                Math.pow(
                                        1 + monthlyRate,
                                        months
                                ) - 1
                        );


        List<ClScheduleDto> result =
                new java.util.ArrayList<>();

        double outstanding =
                loanAmount;


        // ==========================================
        // 6. GENERATE NEW SCHEDULE
        // ==========================================

        for (int i = 1; i <= months; i++) {

            // --------------------------------------
            // Interest on current outstanding
            // --------------------------------------

            double interest =
                    outstanding * monthlyRate;


            // --------------------------------------
            // Principal
            // --------------------------------------

            double principal =
                    emi - interest;


            // --------------------------------------
            // Last installment adjustment
            // --------------------------------------

            if (i == months) {

                principal =
                        outstanding;
            }


            // --------------------------------------
            // Monthly installment
            // --------------------------------------

            double monthlyInstallment =
                    principal + interest;


            // --------------------------------------
            // Create Schedule
            // --------------------------------------

            ClSchedule schedule =
                    new ClSchedule();

            schedule.setLoan(loan);

            schedule.setInstallmentNo(i);


            // --------------------------------------
            // Outstanding Amount
            // --------------------------------------

            schedule.setOutstandingAmount(
                    Math.round(
                            outstanding * 100.0
                    ) / 100.0
            );


            // --------------------------------------
            // Principal Amount
            // --------------------------------------

            schedule.setPrincipalAmount(
                    Math.round(
                            principal * 100.0
                    ) / 100.0
            );


            // --------------------------------------
            // Interest Amount
            // --------------------------------------

            schedule.setInterestAmount(
                    Math.round(
                            interest * 100.0
                    ) / 100.0
            );


            // --------------------------------------
            // Monthly Installment
            // --------------------------------------

            schedule.setMonthlyInstallment(
                    Math.round(
                            monthlyInstallment * 100.0
                    ) / 100.0
            );


            // --------------------------------------
            // Average Monthly Installment
            // --------------------------------------

            schedule.setAverageMonthlyInstallment(
                    0.0
            );


            // --------------------------------------
            // Remark
            // --------------------------------------

            schedule.setRemark("");


            // --------------------------------------
            // Save
            // --------------------------------------

            ClSchedule saved =
                    clScheduleRepository.save(
                            schedule
                    );


            // --------------------------------------
            // Convert Entity -> DTO
            // --------------------------------------

            ClScheduleDto dto =
                    modelMapper.map(
                            saved,
                            ClScheduleDto.class
                    );

            dto.setLoanId(loanId);

            result.add(dto);


            // --------------------------------------
            // Update Outstanding
            // --------------------------------------

            outstanding =
                    outstanding - principal;
        }


        // ==========================================
        // 7. RETURN NEW SCHEDULE
        // ==========================================

        return result;
    }


    // ==========================================
    // GET BY LOAN ID
    // ==========================================

    @Override
    public List<ClScheduleDto> getByLoanId(
            Long loanId) {

        return clScheduleRepository
                .findByLoanId(loanId)
                .stream()
                .map(schedule -> {

                    ClScheduleDto dto =
                            modelMapper.map(
                                    schedule,
                                    ClScheduleDto.class
                            );

                    dto.setLoanId(
                            schedule.getLoan().getId()
                    );

                    return dto;

                })
                .toList();
    }
}