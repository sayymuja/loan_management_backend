package com.example.loan_management.service;

import com.example.loan_management.auth.service.CurrentUserService;
import com.example.loan_management.dto.ClScheduleDto;
import com.example.loan_management.entity.ClSchedule;
import com.example.loan_management.entity.Group;
import com.example.loan_management.entity.Loan;
import com.example.loan_management.entity.VoAlf;
import com.example.loan_management.entity.Women;
import com.example.loan_management.repository.ClScheduleRepository;
import com.example.loan_management.repository.LoanRepository;
import com.example.loan_management.repository.VoAlfRepository;
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
    private final VoAlfRepository voAlfRepository;
    private final ModelMapper modelMapper;
    private final CurrentUserService currentUserService;


    // =========================================================
    // CURRENT CMRC
    // =========================================================

    private Long getCurrentCmrcId() {

        return currentUserService.getCurrentCmrcId();
    }


    // =========================================================
    // VALIDATE VO / ALF
    // =========================================================

    private VoAlf getAuthorizedVoAlf(Long voAlfId) {

        if (voAlfId == null) {

            throw new RuntimeException(
                    "VO / ALF ID is required"
            );
        }

        VoAlf voAlf =
                voAlfRepository.findById(voAlfId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "VO / ALF not found with ID: "
                                                + voAlfId
                                )
                        );

        Long currentCmrcId =
                getCurrentCmrcId();

        if (voAlf.getCmrc() == null ||
                !currentCmrcId.equals(
                        voAlf.getCmrc().getId()
                )) {

            throw new RuntimeException(
                    "Access denied for this VO / ALF"
            );
        }

        return voAlf;
    }


    // =========================================================
    // VALIDATE LOAN OWNERSHIP
    //
    // Logged-in User
    //      ↓
    //     CMRC
    //      ↓
    //   VO / ALF
    //      ↓
    //    Group
    //      ↓
    //    Woman
    //      ↓
    //    Loan
    // =========================================================

    private Loan getAuthorizedLoan(Long loanId) {

        if (loanId == null) {

            throw new RuntimeException(
                    "Loan ID is required"
            );
        }

        Loan loan =
                loanRepository.findById(loanId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Loan not found with ID: "
                                                + loanId
                                )
                        );


        // -----------------------------------------------------
        // LOAN -> WOMAN
        // -----------------------------------------------------

        if (loan.getWoman() == null) {

            throw new RuntimeException(
                    "Woman not found for this loan"
            );
        }

        Women woman =
                loan.getWoman();


        // -----------------------------------------------------
        // WOMAN -> GROUP
        // -----------------------------------------------------

        if (woman.getGroup() == null) {

            throw new RuntimeException(
                    "Group not found for this loan"
            );
        }

        Group group =
                woman.getGroup();


        // -----------------------------------------------------
        // GROUP -> CMRC
        // -----------------------------------------------------

        Long currentCmrcId =
                getCurrentCmrcId();

        if (group.getCmrcId() == null ||
                !currentCmrcId.equals(
                        group.getCmrcId()
                )) {

            throw new RuntimeException(
                    "Access denied for this loan"
            );
        }


        // -----------------------------------------------------
        // GROUP -> VO / ALF
        // -----------------------------------------------------

        if (group.getVoAlfId() == null) {

            throw new RuntimeException(
                    "VO / ALF not found for this loan"
            );
        }


        // -----------------------------------------------------
        // VO / ALF -> CMRC
        // -----------------------------------------------------

        getAuthorizedVoAlf(
                group.getVoAlfId()
        );


        // -----------------------------------------------------
        // LOAN -> VO / ALF CONSISTENCY
        // -----------------------------------------------------

        if (loan.getVoAlfId() == null ||
                !group.getVoAlfId().equals(
                        loan.getVoAlfId()
                )) {

            throw new RuntimeException(
                    "Loan VO / ALF does not match woman's group"
            );
        }

        return loan;
    }


    // =========================================================
    // VALIDATE CL SCHEDULE OWNERSHIP
    // =========================================================

    private ClSchedule getAuthorizedSchedule(Long scheduleId) {

        if (scheduleId == null) {

            throw new RuntimeException(
                    "CL Schedule ID is required"
            );
        }

        ClSchedule schedule =
                clScheduleRepository.findById(scheduleId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "CL Schedule not found"
                                )
                        );


        if (schedule.getLoan() == null) {

            throw new RuntimeException(
                    "Loan not found for this CL Schedule"
            );
        }


        // -----------------------------------------------------
        // Schedule -> Loan -> Woman -> Group -> CMRC
        // -----------------------------------------------------

        getAuthorizedLoan(
                schedule.getLoan().getId()
        );

        return schedule;
    }


    // =========================================================
    // CREATE
    // =========================================================

    @Override
    public ClScheduleDto create(
            ClScheduleDto dto) {

        // -----------------------------------------------------
        // Loan must belong to current user's CMRC
        // -----------------------------------------------------

        Loan loan =
                getAuthorizedLoan(
                        dto.getLoanId()
                );


        ClSchedule schedule =
                modelMapper.map(
                        dto,
                        ClSchedule.class
                );


        schedule.setLoan(loan);


        ClSchedule saved =
                clScheduleRepository.save(
                        schedule
                );


        ClScheduleDto response =
                modelMapper.map(
                        saved,
                        ClScheduleDto.class
                );


        response.setLoanId(
                saved.getLoan().getId()
        );


        return response;
    }


    // =========================================================
    // GET ALL
    // =========================================================

    @Override
    public List<ClScheduleDto> getAll() {

        Long currentCmrcId =
                getCurrentCmrcId();


        return clScheduleRepository
                .findAll()
                .stream()
                .filter(schedule ->
                        schedule.getLoan() != null
                                && schedule.getLoan()
                                .getWoman() != null
                                && schedule.getLoan()
                                .getWoman()
                                .getGroup() != null
                                && currentCmrcId.equals(
                                schedule.getLoan()
                                        .getWoman()
                                        .getGroup()
                                        .getCmrcId()
                        )
                )
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


    // =========================================================
    // GET BY ID
    // =========================================================

    @Override
    public ClScheduleDto getById(Long id) {

        ClSchedule schedule =
                getAuthorizedSchedule(id);


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


    // =========================================================
    // UPDATE
    // =========================================================

    @Override
    public ClScheduleDto update(
            Long id,
            ClScheduleDto dto) {


        // -----------------------------------------------------
        // Existing schedule must belong to current CMRC
        // -----------------------------------------------------

        ClSchedule schedule =
                getAuthorizedSchedule(id);


        // -----------------------------------------------------
        // New loan must also belong to current CMRC
        // -----------------------------------------------------

        Loan loan =
                getAuthorizedLoan(
                        dto.getLoanId()
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
                clScheduleRepository.save(
                        schedule
                );


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


    // =========================================================
    // DELETE
    // =========================================================

    @Override
    public void delete(Long id) {

        // -----------------------------------------------------
        // Verify schedule ownership before delete
        // -----------------------------------------------------

        ClSchedule schedule =
                getAuthorizedSchedule(id);

        clScheduleRepository.delete(schedule);
    }


    // =========================================================
    // GENERATE SCHEDULE
    // =========================================================

    @Override
    @Transactional
    public List<ClScheduleDto> generateSchedule(
            Long loanId) {


        // -----------------------------------------------------
        // IMPORTANT:
        // Verify loan belongs to logged-in user's CMRC
        // -----------------------------------------------------

        Loan loan =
                getAuthorizedLoan(loanId);


        // -----------------------------------------------------
        // Delete OLD schedule
        // -----------------------------------------------------

        clScheduleRepository.deleteByLoanId(
                loanId
        );


        int months =
                loan.getRepaymentPeriodMonths();

        double loanAmount =
                loan.getLoanAmount();

        double annualInterestRate =
                loan.getInterestRate();


        // -----------------------------------------------------
        // MONTHLY INTEREST RATE
        // -----------------------------------------------------

        double monthlyRate =
                annualInterestRate / 100 / 12;


        // -----------------------------------------------------
        // EMI
        // -----------------------------------------------------

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


        // -----------------------------------------------------
        // FIRST INSTALLMENT DATE
        // -----------------------------------------------------

        LocalDate loanDate =
                loan.getLoanGivenDate();


        for (int i = 1; i <= months; i++) {


            // =================================================
            // INSTALLMENT DATE
            // =================================================

            LocalDate installmentDate =
                    loanDate.plusMonths(i);


            // =================================================
            // INTEREST
            // =================================================

            double interest =
                    outstanding * monthlyRate;


            // =================================================
            // PRINCIPAL
            // =================================================

            double principal =
                    emi - interest;


            // -------------------------------------------------
            // FINAL INSTALLMENT
            // -------------------------------------------------

            if (i == months) {

                principal =
                        outstanding;
            }


            // =================================================
            // MONTHLY INSTALLMENT
            // =================================================

            double monthlyInstallment =
                    principal + interest;


            // =================================================
            // CLOSING BALANCE
            // =================================================

            double closingBalance =
                    outstanding - principal;


            if (closingBalance < 0) {

                closingBalance = 0;
            }


            // =================================================
            // CREATE SCHEDULE
            // =================================================

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


            // =================================================
            // REMARK
            // =================================================

            if (i == months) {

                schedule.setRemark(
                        "Final"
                );

            } else {

                schedule.setRemark(
                        "Pending"
                );
            }


            // =================================================
            // SAVE
            // =================================================

            ClSchedule saved =
                    clScheduleRepository.save(
                            schedule
                    );


            // =================================================
            // ENTITY -> DTO
            // =================================================

            ClScheduleDto dto =
                    modelMapper.map(
                            saved,
                            ClScheduleDto.class
                    );

            dto.setLoanId(
                    loanId
            );

            result.add(dto);


            // =================================================
            // NEXT OUTSTANDING
            // =================================================

            outstanding =
                    closingBalance;
        }


        return result;
    }


    // =========================================================
    // GET BY LOAN ID
    // =========================================================

    @Override
    public List<ClScheduleDto> getByLoanId(
            Long loanId) {


        // -----------------------------------------------------
        // Loan must belong to current CMRC
        // -----------------------------------------------------

        getAuthorizedLoan(loanId);


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