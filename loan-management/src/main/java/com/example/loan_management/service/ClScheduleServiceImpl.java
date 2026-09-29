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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ClScheduleServiceImpl implements ClScheduleService {

    private final ClScheduleRepository clScheduleRepository;
    private final LoanRepository loanRepository;
    private final ModelMapper modelMapper;


    // =========================
    // CREATE
    // =========================

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


    // =========================
    // GET ALL
    // =========================

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


    // =========================
    // GET BY ID
    // =========================

    @Override
    public ClScheduleDto getById(Long id) {

        ClSchedule schedule =
                clScheduleRepository.findById(id)
                        .orElseThrow(
                                () -> new RuntimeException(
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


    // =========================
    // UPDATE
    // =========================

    @Override
    public ClScheduleDto update(
            Long id,
            ClScheduleDto dto) {

        ClSchedule schedule =
                clScheduleRepository.findById(id)
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "CL Schedule not found"
                                )
                        );

        Loan loan =
                loanRepository.findById(dto.getLoanId())
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Loan not found"
                                )
                        );

        schedule.setLoan(loan);

        schedule.setInstallmentNo(
                dto.getInstallmentNo()
        );

        schedule.setInstallmentDate(
                dto.getInstallmentDate()
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

        schedule.setClosingBalance(
                dto.getClosingBalance()
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


    // =========================
    // DELETE
    // =========================

    @Override
    public void delete(Long id) {

        ClSchedule schedule =
                clScheduleRepository.findById(id)
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "CL Schedule not found"
                                )
                        );

        clScheduleRepository.delete(schedule);
    }


    // =========================
    // GENERATE SCHEDULE
    // =========================

    @Override
    @Transactional
    public List<ClScheduleDto> generateSchedule(
            Long loanId) {

        Loan loan =
                loanRepository.findById(loanId)
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Loan not found"
                                )
                        );

        // Delete old schedule
        clScheduleRepository.deleteByLoanId(loanId);


        int months =
                loan.getRepaymentPeriodMonths();

        double loanAmount =
                loan.getLoanAmount();

        double annualInterestRate =
                loan.getInterestRate();


        // Monthly interest rate
        double monthlyRate =
                annualInterestRate / 100 / 12;


        // EMI calculation
        double emi;

        if (monthlyRate == 0) {

            emi =
                    loanAmount / months;

        } else {

            emi =
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
        }


        List<ClScheduleDto> result =
                new ArrayList<>();


        double outstanding =
                loanAmount;


        // First installment date
        LocalDate loanDate =
                loan.getLoanGivenDate();


        for (int i = 1; i <= months; i++) {


            // =========================
            // INSTALLMENT DATE
            // =========================

            LocalDate installmentDate =
                    loanDate.plusMonths(i);


            // =========================
            // INTEREST
            // =========================

            double interest =
                    outstanding * monthlyRate;


            // =========================
            // PRINCIPAL
            // =========================

            double principal =
                    emi - interest;


            // Final installment adjustment
            if (i == months) {

                principal =
                        outstanding;
            }


            // =========================
            // MONTHLY INSTALLMENT
            // =========================

            double monthlyInstallment =
                    principal + interest;


            // =========================
            // CLOSING BALANCE
            // =========================

            double closingBalance =
                    outstanding - principal;


            if (closingBalance < 0) {

                closingBalance = 0;
            }


            // =========================
            // CREATE SCHEDULE
            // =========================

            ClSchedule schedule =
                    new ClSchedule();

            schedule.setLoan(loan);

            schedule.setInstallmentNo(i);

            schedule.setInstallmentDate(
                    installmentDate
            );

            schedule.setOutstandingAmount(
                    Math.round(
                            outstanding * 100.0
                    ) / 100.0
            );

            schedule.setPrincipalAmount(
                    Math.round(
                            principal * 100.0
                    ) / 100.0
            );

            schedule.setInterestAmount(
                    Math.round(
                            interest * 100.0
                    ) / 100.0
            );

            schedule.setMonthlyInstallment(
                    Math.round(
                            monthlyInstallment * 100.0
                    ) / 100.0
            );

            schedule.setAverageMonthlyInstallment(
                    Math.round(
                            emi * 100.0
                    ) / 100.0
            );

            schedule.setClosingBalance(
                    Math.round(
                            closingBalance * 100.0
                    ) / 100.0
            );


            // Remark
            if (i == months) {

                schedule.setRemark("Final");

            } else {

                schedule.setRemark("Pending");
            }


            // Save
            ClSchedule saved =
                    clScheduleRepository.save(
                            schedule
                    );


            // Convert to DTO
            ClScheduleDto dto =
                    modelMapper.map(
                            saved,
                            ClScheduleDto.class
                    );

            dto.setLoanId(loanId);

            result.add(dto);


            // Next installment outstanding
            outstanding =
                    closingBalance;
        }


        return result;
    }


    // =========================
    // GET BY LOAN ID
    // =========================

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