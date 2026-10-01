package com.example.loan_management.service;

import com.example.loan_management.auth.service.CurrentUserService;
import com.example.loan_management.dto.RepaymentDto;
import com.example.loan_management.dto.RepaymentSummaryDto;
import com.example.loan_management.entity.Group;
import com.example.loan_management.entity.Loan;
import com.example.loan_management.entity.Repayment;
import com.example.loan_management.entity.VoAlf;
import com.example.loan_management.entity.Women;
import com.example.loan_management.repository.LoanRepository;
import com.example.loan_management.repository.RepaymentRepository;
import com.example.loan_management.repository.VoAlfRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RepaymentServiceImpl implements RepaymentService {

    private final RepaymentRepository repaymentRepository;
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
    // AUTHORIZE VO / ALF
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
    // AUTHORIZE LOAN
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
    // AUTHORIZE REPAYMENT
    //
    // Repayment -> Loan -> Woman -> Group -> CMRC
    // =========================================================

    private Repayment getAuthorizedRepayment(
            Long repaymentId) {

        if (repaymentId == null) {

            throw new RuntimeException(
                    "Repayment ID is required"
            );
        }

        Repayment repayment =
                repaymentRepository.findById(repaymentId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Repayment not found with id: "
                                                + repaymentId
                                )
                        );

        if (repayment.getLoan() == null) {

            throw new RuntimeException(
                    "Loan not found for this repayment"
            );
        }


        // -----------------------------------------------------
        // Validate repayment's loan ownership
        // -----------------------------------------------------

        getAuthorizedLoan(
                repayment.getLoan().getId()
        );

        return repayment;
    }


    // =========================================================
    // CREATE
    // =========================================================

    @Override
    public RepaymentDto create(
            RepaymentDto repaymentDto) {

        // -----------------------------------------------------
        // Loan must belong to current user's CMRC
        // -----------------------------------------------------

        Loan loan =
                getAuthorizedLoan(
                        repaymentDto.getLoanId()
                );


        Repayment repayment =
                modelMapper.map(
                        repaymentDto,
                        Repayment.class
                );


        repayment.setLoan(loan);

        calculateRepayment(
                repayment,
                loan
        );


        Repayment savedRepayment =
                repaymentRepository.save(
                        repayment
                );


        RepaymentDto response =
                modelMapper.map(
                        savedRepayment,
                        RepaymentDto.class
                );


        response.setLoanId(
                savedRepayment.getLoan().getId()
        );


        return response;
    }


    // =========================================================
    // GET ALL
    // =========================================================

    @Override
    public List<RepaymentDto> getAll() {

        Long currentCmrcId =
                getCurrentCmrcId();


        return repaymentRepository
                .findAll()
                .stream()
                .filter(repayment ->
                        repayment.getLoan() != null
                                && repayment.getLoan()
                                .getWoman() != null
                                && repayment.getLoan()
                                .getWoman()
                                .getGroup() != null
                                && currentCmrcId.equals(
                                repayment.getLoan()
                                        .getWoman()
                                        .getGroup()
                                        .getCmrcId()
                        )
                )
                .map(repayment -> {

                    RepaymentDto dto =
                            modelMapper.map(
                                    repayment,
                                    RepaymentDto.class
                            );

                    dto.setLoanId(
                            repayment.getLoan().getId()
                    );

                    return dto;
                })
                .toList();
    }


    // =========================================================
    // GET BY ID
    // =========================================================

    @Override
    public RepaymentDto getById(Long id) {

        Repayment repayment =
                getAuthorizedRepayment(id);


        RepaymentDto dto =
                modelMapper.map(
                        repayment,
                        RepaymentDto.class
                );


        dto.setLoanId(
                repayment.getLoan().getId()
        );


        return dto;
    }


    // =========================================================
    // UPDATE
    // =========================================================

    @Override
    public RepaymentDto update(
            Long id,
            RepaymentDto repaymentDto) {

        // -----------------------------------------------------
        // Existing repayment must belong to current CMRC
        // -----------------------------------------------------

        Repayment repayment =
                getAuthorizedRepayment(id);


        // -----------------------------------------------------
        // New loan must also belong to current CMRC
        // -----------------------------------------------------

        Loan loan =
                getAuthorizedLoan(
                        repaymentDto.getLoanId()
                );


        repayment.setLoan(loan);

        repayment.setRepaymentDate(
                repaymentDto.getRepaymentDate()
        );

        repayment.setPaidAmount(
                repaymentDto.getPaidAmount()
        );


        calculateRepayment(
                repayment,
                loan
        );


        repayment.setRegularRepayment(
                repaymentDto.getRegularRepayment()
        );

        repayment.setPenaltyAmount(
                repaymentDto.getPenaltyAmount()
        );

        repayment.setRemark(
                repaymentDto.getRemark()
        );


        Repayment updatedRepayment =
                repaymentRepository.save(
                        repayment
                );


        RepaymentDto response =
                modelMapper.map(
                        updatedRepayment,
                        RepaymentDto.class
                );


        response.setLoanId(
                updatedRepayment.getLoan().getId()
        );


        return response;
    }


    // =========================================================
    // DELETE
    // =========================================================

    @Override
    public void delete(Long id) {

        // -----------------------------------------------------
        // Verify repayment belongs to current CMRC
        // -----------------------------------------------------

        Repayment repayment =
                getAuthorizedRepayment(id);


        Long loanId =
                repayment.getLoan().getId();


        repaymentRepository.delete(
                repayment
        );


        updateLoanStatus(
                loanId
        );
    }


    // =========================================================
    // REPAYMENT SUMMARY
    // =========================================================

    @Override
    public RepaymentSummaryDto getSummary(
            Long loanId) {

        // -----------------------------------------------------
        // Verify loan ownership first
        // -----------------------------------------------------

        Loan loan =
                getAuthorizedLoan(loanId);


        List<Repayment> repayments =
                repaymentRepository
                        .findByLoanId(loanId);


        double totalPrincipal =
                repayments.stream()
                        .mapToDouble(r ->
                                r.getPrincipalAmount() != null
                                        ? r.getPrincipalAmount()
                                        : 0
                        )
                        .sum();


        double totalInterest =
                repayments.stream()
                        .mapToDouble(r ->
                                r.getInterestAmount() != null
                                        ? r.getInterestAmount()
                                        : 0
                        )
                        .sum();


        double totalRepayment =
                repayments.stream()
                        .mapToDouble(r ->
                                r.getTotalAmount() != null
                                        ? r.getTotalAmount()
                                        : 0
                        )
                        .sum();


        double totalPenalty =
                repayments.stream()
                        .mapToDouble(r ->
                                r.getPenaltyAmount() != null
                                        ? r.getPenaltyAmount()
                                        : 0
                        )
                        .sum();


        double outstandingAmount =
                Math.max(
                        loan.getLoanAmount()
                                - totalPrincipal,
                        0.0
                );


        RepaymentSummaryDto summary =
                new RepaymentSummaryDto();


        summary.setLoanId(
                loanId
        );

        summary.setLoanAmount(
                loan.getLoanAmount()
        );

        summary.setTotalPrincipalPaid(
                totalPrincipal
        );

        summary.setTotalInterestPaid(
                totalInterest
        );

        summary.setTotalRepayment(
                totalRepayment
        );

        summary.setOutstandingAmount(
                outstandingAmount
        );

        summary.setTotalPenalty(
                totalPenalty
        );


        return summary;
    }


    // =========================================================
    // GET BY LOAN ID
    // =========================================================

    @Override
    public List<RepaymentDto> getByLoanId(
            Long loanId) {

        // -----------------------------------------------------
        // Verify loan ownership
        // -----------------------------------------------------

        getAuthorizedLoan(loanId);


        return repaymentRepository
                .findByLoanId(loanId)
                .stream()
                .map(repayment -> {

                    RepaymentDto dto =
                            modelMapper.map(
                                    repayment,
                                    RepaymentDto.class
                            );

                    dto.setLoanId(
                            repayment.getLoan().getId()
                    );

                    return dto;
                })
                .toList();
    }


    // =========================================================
    // GENERATE REPAYMENT SCHEDULE
    // =========================================================

    @Override
    @Transactional
    public List<RepaymentDto> generateSchedule(
            Long loanId) {

        // -----------------------------------------------------
        // IMPORTANT:
        // Verify loan belongs to current CMRC
        // -----------------------------------------------------

        Loan loan =
                getAuthorizedLoan(loanId);


        // =====================================================
        // 1. VALIDATION
        // =====================================================

        if (loan.getLoanAmount() == null ||
                loan.getLoanAmount() <= 0) {

            throw new RuntimeException(
                    "Loan amount is invalid"
            );
        }


        if (loan.getInterestRate() == null ||
                loan.getInterestRate() < 0) {

            throw new RuntimeException(
                    "Interest rate is invalid"
            );
        }


        if (loan.getMonthlyEmi() == null ||
                loan.getMonthlyEmi() <= 0) {

            throw new RuntimeException(
                    "Monthly EMI is invalid"
            );
        }


        if (loan.getRepaymentPeriodMonths() == null ||
                loan.getRepaymentPeriodMonths() <= 0) {

            throw new RuntimeException(
                    "Repayment period is invalid"
            );
        }


        if (loan.getLoanGivenDate() == null) {

            throw new RuntimeException(
                    "Loan given date is required"
            );
        }


        // =====================================================
        // 2. DELETE EXISTING SCHEDULE
        // =====================================================

        List<Repayment> existingRepayments =
                repaymentRepository
                        .findByLoanId(loanId);


        if (!existingRepayments.isEmpty()) {

            repaymentRepository.deleteAll(
                    existingRepayments
            );

            repaymentRepository.flush();
        }


        // =====================================================
        // 3. LOAN CALCULATION
        // =====================================================

        double outstandingPrincipal =
                loan.getLoanAmount();


        double monthlyRate =
                loan.getInterestRate()
                        / 12.0
                        / 100.0;


        double emi =
                loan.getMonthlyEmi();


        int totalInstallments =
                loan.getRepaymentPeriodMonths();


        LocalDate firstInstallmentDate =
                loan.getLoanGivenDate()
                        .plusMonths(1);


        // =====================================================
        // 4. GENERATE NEW SCHEDULE
        // =====================================================

        List<Repayment> repaymentList =
                new ArrayList<>();


        for (
                int i = 1;
                i <= totalInstallments;
                i++
        ) {

            Repayment repayment =
                    new Repayment();


            repayment.setLoan(
                    loan
            );


            // -------------------------------------------------
            // Installment number
            // -------------------------------------------------

            repayment.setInstallmentNo(
                    i
            );


            // -------------------------------------------------
            // Installment date
            // -------------------------------------------------

            repayment.setInstallmentDate(
                    firstInstallmentDate
                            .plusMonths(i - 1)
            );


            // -------------------------------------------------
            // Monthly interest
            // -------------------------------------------------

            double interestAmount =
                    outstandingPrincipal
                            * monthlyRate;


            // -------------------------------------------------
            // Principal
            // -------------------------------------------------

            double principalAmount =
                    emi - interestAmount;


            if (principalAmount < 0) {

                principalAmount = 0;
            }


            if (principalAmount >
                    outstandingPrincipal) {

                principalAmount =
                        outstandingPrincipal;
            }


            // -------------------------------------------------
            // Scheduled amount
            // -------------------------------------------------

            double scheduledAmount =
                    principalAmount
                            + interestAmount;


            // -------------------------------------------------
            // Last installment adjustment
            // -------------------------------------------------

            if (i == totalInstallments) {

                principalAmount =
                        outstandingPrincipal;

                scheduledAmount =
                        principalAmount
                                + interestAmount;
            }


            // =================================================
            // SET VALUES
            // =================================================

            repayment.setPrincipalAmount(
                    round(principalAmount)
            );


            repayment.setInterestAmount(
                    round(interestAmount)
            );


            repayment.setScheduledAmount(
                    round(scheduledAmount)
            );


            repayment.setPaidAmount(
                    0.0
            );


            repayment.setPenaltyAmount(
                    0.0
            );


            repayment.setTotalAmount(
                    0.0
            );


            repayment.setRegularRepayment(
                    false
            );


            repayment.setPaymentStatus(
                    "PENDING"
            );


            repayment.setRepaymentDate(
                    null
            );


            repayment.setRemark(
                    null
            );


            repaymentList.add(
                    repayment
            );


            // -------------------------------------------------
            // Reduce outstanding principal
            // -------------------------------------------------

            outstandingPrincipal =
                    outstandingPrincipal
                            - principalAmount;


            if (outstandingPrincipal < 0.01) {

                outstandingPrincipal = 0.0;
            }
        }


        // =====================================================
        // 5. SAVE NEW SCHEDULE
        // =====================================================

        List<Repayment> savedList =
                repaymentRepository.saveAll(
                        repaymentList
                );


        // =====================================================
        // 6. RETURN DTO
        // =====================================================

        return savedList.stream()
                .map(repayment -> {

                    RepaymentDto dto =
                            modelMapper.map(
                                    repayment,
                                    RepaymentDto.class
                            );

                    dto.setLoanId(
                            repayment.getLoan().getId()
                    );

                    return dto;

                })
                .collect(
                        Collectors.toList()
                );
    }


    // =========================================================
    // CALCULATE REPAYMENT
    // =========================================================

    private void calculateRepayment(
            Repayment repayment,
            Loan loan) {

        double paidAmount =
                repayment.getPaidAmount() != null
                        ? repayment.getPaidAmount()
                        : 0.0;


        double loanAmount =
                loan.getLoanAmount() != null
                        ? loan.getLoanAmount()
                        : 0.0;


        double annualRate =
                loan.getInterestRate() != null
                        ? loan.getInterestRate()
                        : 0.0;


        // =====================================================
        // GET PREVIOUS PAID PRINCIPAL
        // =====================================================

        List<Repayment> previousRepayments =
                repaymentRepository
                        .findByLoanId(
                                loan.getId()
                        );


        double paidPrincipal =
                previousRepayments.stream()

                        .filter(r ->
                                r.getId() == null
                                        || !r.getId()
                                        .equals(
                                                repayment.getId()
                                        )
                        )

                        .filter(r ->
                                "PAID".equalsIgnoreCase(
                                        r.getPaymentStatus()
                                )
                                        ||
                                        "PARTIAL".equalsIgnoreCase(
                                                r.getPaymentStatus()
                                        )
                        )

                        .mapToDouble(r ->
                                r.getPrincipalAmount() != null
                                        ? Math.max(
                                        r.getPrincipalAmount(),
                                        0.0
                                )
                                        : 0.0
                        )

                        .sum();


        // =====================================================
        // OUTSTANDING PRINCIPAL
        // =====================================================

        double outstandingPrincipal =
                Math.max(
                        loanAmount
                                - paidPrincipal,
                        0.0
                );


        // =====================================================
        // MONTHLY INTEREST
        // =====================================================

        double monthlyInterest =
                outstandingPrincipal
                        * annualRate
                        / 12
                        / 100;


        // =====================================================
        // CURRENT PAYMENT INTEREST
        // =====================================================

        double interest =
                Math.max(
                        0.0,
                        Math.min(
                                paidAmount,
                                monthlyInterest
                        )
                );


        // =====================================================
        // CURRENT PAYMENT PRINCIPAL
        // =====================================================

        double principalPaid =
                Math.max(
                        0.0,
                        paidAmount
                                - interest
                );


        // =====================================================
        // SET VALUES
        // =====================================================

        repayment.setInterestAmount(
                round(interest)
        );


        repayment.setPrincipalAmount(
                round(principalPaid)
        );
    }


    // =========================================================
    // PAY EMI
    // =========================================================

    @Override
    @Transactional
    public RepaymentDto payEmi(
            Long repaymentId,
            Double paidAmount,
            Double penaltyAmount,
            Boolean regularRepayment) {

        // =====================================================
        // 1. FIND + AUTHORIZE REPAYMENT
        // =====================================================

        Repayment repayment =
                getAuthorizedRepayment(
                        repaymentId
                );


        // =====================================================
        // 2. SAFE VALUES
        // =====================================================

        double paid =
                paidAmount != null
                        ? paidAmount
                        : 0.0;


        double penalty =
                penaltyAmount != null
                        ? penaltyAmount
                        : 0.0;


        double scheduled =
                repayment.getScheduledAmount() != null
                        ? repayment.getScheduledAmount()
                        : 0.0;


        // =====================================================
        // 3. SET PAYMENT VALUES
        // =====================================================

        repayment.setPaidAmount(
                paid
        );


        repayment.setPenaltyAmount(
                penalty
        );


        repayment.setRegularRepayment(
                regularRepayment != null
                        ? regularRepayment
                        : false
        );


        // =====================================================
        // 4. PAYMENT STATUS
        // =====================================================

        if (paid <= 0) {

            repayment.setPaymentStatus(
                    "PENDING"
            );

        } else if (paid < scheduled) {

            repayment.setPaymentStatus(
                    "PARTIAL"
            );

        } else {

            repayment.setPaymentStatus(
                    "PAID"
            );
        }


        // =====================================================
        // 5. REPAYMENT DATE
        // =====================================================

        if (paid > 0) {

            repayment.setRepaymentDate(
                    LocalDate.now()
            );

        } else {

            repayment.setRepaymentDate(
                    null
            );
        }


        // =====================================================
        // 6. CALCULATE PRINCIPAL + INTEREST
        // =====================================================

        calculateRepayment(
                repayment,
                repayment.getLoan()
        );


        // =====================================================
        // 7. TOTAL AMOUNT
        // =====================================================

        repayment.setTotalAmount(
                paid + penalty
        );


        // =====================================================
        // 8. SAVE
        // =====================================================

        Repayment saved =
                repaymentRepository.save(
                        repayment
                );


        // =====================================================
        // 9. UPDATE LOAN STATUS
        // =====================================================

        updateLoanStatus(
                saved.getLoan().getId()
        );


        // =====================================================
        // 10. RESPONSE
        // =====================================================

        RepaymentDto response =
                modelMapper.map(
                        saved,
                        RepaymentDto.class
                );


        response.setLoanId(
                saved.getLoan().getId()
        );


        return response;
    }


    // =========================================================
    // EDIT PAID EMI
    // =========================================================

    @Override
    @Transactional
    public RepaymentDto editPaidEmi(
            Long repaymentId,
            Double paidAmount,
            Double penaltyAmount) {

        // -----------------------------------------------------
        // Authorize repayment first
        // -----------------------------------------------------

        Repayment repayment =
                getAuthorizedRepayment(
                        repaymentId
                );


        double paid =
                paidAmount != null
                        ? paidAmount
                        : 0.0;


        double penalty =
                penaltyAmount != null
                        ? penaltyAmount
                        : 0.0;


        double scheduled =
                repayment.getScheduledAmount() != null
                        ? repayment.getScheduledAmount()
                        : 0.0;


        repayment.setPaidAmount(
                paid
        );


        repayment.setPenaltyAmount(
                penalty
        );


        // =====================================================
        // STATUS
        // =====================================================

        if (paid <= 0) {

            repayment.setPaymentStatus(
                    "PENDING"
            );

        } else if (paid < scheduled) {

            repayment.setPaymentStatus(
                    "PARTIAL"
            );

        } else {

            repayment.setPaymentStatus(
                    "PAID"
            );
        }


        // =====================================================
        // DATE
        // =====================================================

        if (paid > 0) {

            repayment.setRepaymentDate(
                    LocalDate.now()
            );

        } else {

            repayment.setRepaymentDate(
                    null
            );
        }


        // =====================================================
        // CALCULATE PRINCIPAL / INTEREST
        // =====================================================

        calculateRepayment(
                repayment,
                repayment.getLoan()
        );


        repayment.setTotalAmount(
                paid + penalty
        );


        Repayment saved =
                repaymentRepository.save(
                        repayment
                );


        updateLoanStatus(
                saved.getLoan().getId()
        );


        RepaymentDto response =
                modelMapper.map(
                        saved,
                        RepaymentDto.class
                );


        response.setLoanId(
                saved.getLoan().getId()
        );


        return response;
    }


    // =========================================================
    // UPDATE LOAN STATUS
    // =========================================================

    private void updateLoanStatus(
            Long loanId) {

        // -----------------------------------------------------
        // Also authorize the loan here.
        // This protects internal status updates.
        // -----------------------------------------------------

        Loan loan =
                getAuthorizedLoan(
                        loanId
                );


        List<Repayment> repayments =
                repaymentRepository
                        .findByLoanId(
                                loanId
                        );


        Integer totalInstallments =
                loan.getRepaymentPeriodMonths();


        if (totalInstallments == null ||
                totalInstallments <= 0) {

            loan.setLoanStatus(
                    "ACTIVE"
            );

            loanRepository.save(
                    loan
            );

            return;
        }


        long paidInstallments =
                repayments.stream()
                        .filter(r ->
                                "PAID".equalsIgnoreCase(
                                        r.getPaymentStatus()
                                )
                        )
                        .count();


        if (paidInstallments >=
                totalInstallments) {

            loan.setLoanStatus(
                    "CLOSED"
            );

        } else {

            loan.setLoanStatus(
                    "ACTIVE"
            );
        }


        loanRepository.save(
                loan
        );
    }


    // =========================================================
    // ROUND
    // =========================================================

    private double round(
            double value) {

        return Math.round(
                value * 100.0
        ) / 100.0;
    }
}