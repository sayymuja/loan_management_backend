package com.example.loan_management.service;

import com.example.loan_management.dto.LoanDto;
import com.example.loan_management.entity.Group;
import com.example.loan_management.entity.Loan;
import com.example.loan_management.entity.Repayment;
import com.example.loan_management.entity.Women;
import com.example.loan_management.repository.ClScheduleRepository;
import com.example.loan_management.repository.LoanRepository;
import com.example.loan_management.repository.RepaymentRepository;
import com.example.loan_management.repository.WomenRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LoanServiceImpl implements LoanService {

    private final LoanRepository loanRepository;
    private final WomenRepository womenRepository;
    private final RepaymentRepository repaymentRepository;
    private final ClScheduleRepository clScheduleRepository;

    // ADD THIS
    private final LoanImageService loanImageService;

    private final ModelMapper modelMapper;


    // =========================================================
    // CREATE
    // =========================================================

    @Override
    public LoanDto create(LoanDto loanDto) {

        // -----------------------------------------------------
        // Validate Woman
        // -----------------------------------------------------

        if (loanDto.getWomanId() == null) {
            throw new RuntimeException("Woman ID is required");
        }

        Women woman = womenRepository.findById(
                loanDto.getWomanId()
        ).orElseThrow(
                () -> new RuntimeException(
                        "Woman not found with ID: "
                                + loanDto.getWomanId()
                )
        );


        // -----------------------------------------------------
        // Validate Group
        // -----------------------------------------------------

        if (woman.getGroup() == null) {
            throw new RuntimeException(
                    "Group not found for selected woman"
            );
        }

        Group group = woman.getGroup();


        // -----------------------------------------------------
        // Validate VO / ALF
        // -----------------------------------------------------

        if (group.getVoAlfId() == null) {
            throw new RuntimeException(
                    "VO / ALF ID not found for selected group"
            );
        }


        // =====================================================
        // MAP DTO -> ENTITY
        // =====================================================

        Loan loan = modelMapper.map(
                loanDto,
                Loan.class
        );


        // =====================================================
        // HIERARCHY
        // =====================================================

        // Woman
        loan.setWoman(woman);

        // VO / ALF
        loan.setVoAlfId(
                group.getVoAlfId()
        );


        // =====================================================
        // LOAN AMOUNT
        // =====================================================

        loan.setSanctionedAmount(
                loanDto.getSanctionedAmount()
        );

        loan.setProcessingFee(
                loanDto.getProcessingFee()
        );


        // -----------------------------------------------------
        // Calculate Disbursed / Loan Amount
        // -----------------------------------------------------

        loan.setLoanAmount(
                calculateDisbursedAmount(
                        loanDto.getSanctionedAmount(),
                        loanDto.getProcessingFee()
                )
        );


        // =====================================================
        // LOAN DETAILS
        // =====================================================

        loan.setLoanPurpose(
                loanDto.getLoanPurpose()
        );

        loan.setLoanGivenDate(
                loanDto.getLoanGivenDate()
        );


        // =====================================================
        // REPAYMENT DETAILS
        // =====================================================

        loan.setRepaymentPeriodMonths(
                loanDto.getRepaymentPeriodMonths()
        );

        loan.setInterestRate(
                loanDto.getInterestRate()
        );

        loan.setInterestType(
                loanDto.getInterestType()
        );


        // =====================================================
        // EMI
        // =====================================================

        loan.setMonthlyEmi(
                calculateEmi(
                        loan.getLoanAmount(),
                        loan.getInterestRate(),
                        loan.getRepaymentPeriodMonths(),
                        loan.getInterestType()
                )
        );


        // =====================================================
        // STATUS
        // =====================================================

        loan.setLoanStatus(
                loanDto.getLoanStatus() != null
                        ? loanDto.getLoanStatus()
                        : "ACTIVE"
        );


        // =====================================================
        // SAVE
        // =====================================================

        Loan savedLoan =
                loanRepository.save(loan);


        // =====================================================
        // ENTITY -> DTO
        // =====================================================

        return mapLoanToDto(savedLoan);
    }


    // =========================================================
    // GET ALL
    // =========================================================

    @Override
    public List<LoanDto> getAll() {

        return loanRepository.findAll()
                .stream()
                .map(this::mapLoanToDto)
                .toList();
    }


    // =========================================================
    // GET BY ID
    // =========================================================

    @Override
    public LoanDto getById(Long id) {

        Loan loan =
                loanRepository.findById(id)
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Loan not found with ID: "
                                                + id
                                )
                        );

        return mapLoanToDto(loan);
    }


    // =========================================================
    // UPDATE
    // =========================================================

    @Override
    public LoanDto update(
            Long id,
            LoanDto loanDto
    ) {

        // -----------------------------------------------------
        // Find Existing Loan
        // -----------------------------------------------------

        Loan loan =
                loanRepository.findById(id)
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Loan not found with ID: "
                                                + id
                                )
                        );


        // -----------------------------------------------------
        // Validate Woman
        // -----------------------------------------------------

        if (loanDto.getWomanId() == null) {
            throw new RuntimeException(
                    "Woman ID is required"
            );
        }

        Women woman =
                womenRepository.findById(
                        loanDto.getWomanId()
                ).orElseThrow(
                        () -> new RuntimeException(
                                "Woman not found with ID: "
                                        + loanDto.getWomanId()
                        )
                );


        // -----------------------------------------------------
        // Validate Group
        // -----------------------------------------------------

        if (woman.getGroup() == null) {
            throw new RuntimeException(
                    "Group not found for selected woman"
            );
        }

        Group group = woman.getGroup();


        // -----------------------------------------------------
        // Validate VO / ALF
        // -----------------------------------------------------

        if (group.getVoAlfId() == null) {
            throw new RuntimeException(
                    "VO / ALF ID not found for selected group"
            );
        }


        // =====================================================
        // HIERARCHY
        // =====================================================

        loan.setWoman(woman);

        loan.setVoAlfId(
                group.getVoAlfId()
        );


        // =====================================================
        // LOAN AMOUNT
        // =====================================================

        loan.setSanctionedAmount(
                loanDto.getSanctionedAmount()
        );

        loan.setProcessingFee(
                loanDto.getProcessingFee()
        );


        // -----------------------------------------------------
        // Calculate Disbursed Amount
        // -----------------------------------------------------

        loan.setLoanAmount(
                calculateDisbursedAmount(
                        loanDto.getSanctionedAmount(),
                        loanDto.getProcessingFee()
                )
        );


        // =====================================================
        // LOAN DETAILS
        // =====================================================

        loan.setLoanPurpose(
                loanDto.getLoanPurpose()
        );

        loan.setLoanGivenDate(
                loanDto.getLoanGivenDate()
        );


        // =====================================================
        // REPAYMENT DETAILS
        // =====================================================

        loan.setRepaymentPeriodMonths(
                loanDto.getRepaymentPeriodMonths()
        );

        loan.setInterestRate(
                loanDto.getInterestRate()
        );

        loan.setInterestType(
                loanDto.getInterestType()
        );


        // =====================================================
        // EMI
        // =====================================================

        loan.setMonthlyEmi(
                calculateEmi(
                        loan.getLoanAmount(),
                        loan.getInterestRate(),
                        loan.getRepaymentPeriodMonths(),
                        loan.getInterestType()
                )
        );


        // =====================================================
        // STATUS
        // =====================================================

        if (loanDto.getLoanStatus() != null) {
            loan.setLoanStatus(
                    loanDto.getLoanStatus()
            );
        }


        // =====================================================
        // SAVE
        // =====================================================

        Loan updatedLoan =
                loanRepository.save(loan);


        return mapLoanToDto(updatedLoan);
    }


    // =========================================================
    // DELETE
    // =========================================================
    @Transactional
    @Override
    public void delete(Long id) {

        Loan loan = loanRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Loan not found with ID: " + id)
                );

        // 1. Delete CL Schedule
        clScheduleRepository.deleteAll(
                clScheduleRepository.findByLoanId(id)
        );

        // 2. Delete Repayment
        repaymentRepository.deleteByLoanId(id);

        // 3. Delete Loan Images
        loanImageService.deleteByLoanId(id);

        // 4. Finally delete Loan
        loanRepository.delete(loan);
    }


    // =========================================================
    // GET LOANS BY WOMAN
    // =========================================================

    @Override
    public List<LoanDto> getByWomanId(
            Long womanId
    ) {

        return loanRepository
                .findByWomanId(womanId)
                .stream()
                .map(this::mapLoanToDto)
                .toList();
    }


    // =========================================================
    // ENTITY -> DTO
    // =========================================================

    private LoanDto mapLoanToDto(Loan loan) {

        LoanDto dto = new LoanDto();

        // =========================================================
        // LOAN BASIC DETAILS
        // =========================================================

        dto.setId(loan.getId());

        dto.setSanctionedAmount(loan.getSanctionedAmount());
        dto.setProcessingFee(loan.getProcessingFee());
        dto.setLoanAmount(loan.getLoanAmount());

        dto.setLoanPurpose(loan.getLoanPurpose());
        dto.setLoanGivenDate(loan.getLoanGivenDate());

        dto.setRepaymentPeriodMonths(
                loan.getRepaymentPeriodMonths()
        );

        dto.setInterestRate(loan.getInterestRate());
        dto.setInterestType(loan.getInterestType());
        dto.setMonthlyEmi(loan.getMonthlyEmi());
        dto.setLoanStatus(loan.getLoanStatus());



        // =========================================================
        // VO / ALF ID
        // =========================================================

        dto.setVoAlfId(loan.getVoAlfId());


        // =========================================================
        // WOMAN
        // =========================================================

        if (loan.getWoman() != null) {

            Women woman = loan.getWoman();

            dto.setWomanId(woman.getId());
            dto.setWomanName(woman.getWomanName());


            // =====================================================
            // GROUP
            // =====================================================

            if (woman.getGroup() != null) {

                Group group = woman.getGroup();

                dto.setGroupId(group.getId());
                dto.setGroupName(group.getGroupName());
                dto.setVillageName(group.getVillageName());

                // -------------------------------------------------
                // CMRC
                // -------------------------------------------------

                dto.setCmrcId(group.getCmrcId());

                // -------------------------------------------------
                // VO / ALF
                // -------------------------------------------------

                dto.setVoAlfId(group.getVoAlfId());
            }
        }


        // =========================================================
        // TOTAL INTEREST RECEIVED
        // =========================================================

        dto.setTotalInterestReceived(
                calculateTotalInterestReceived(loan.getId())
        );


        return dto;
    }

    // =========================================================
    // CALCULATE DISBURSED / LOAN AMOUNT
    // =========================================================

    private Double calculateDisbursedAmount(
            Double sanctionedAmount,
            Double processingFee
    ) {

        double sanctioned =
                sanctionedAmount != null
                        ? sanctionedAmount
                        : 0.0;

        double fee =
                processingFee != null
                        ? processingFee
                        : 0.0;


        if (sanctioned <= 0) {
            return 0.0;
        }


        return Math.max(
                sanctioned - fee,
                0.0
        );
    }


    // =========================================================
    // TOTAL INTEREST RECEIVED
    // =========================================================

    private BigDecimal calculateTotalInterestReceived(
            Long loanId
    ) {

        if (loanId == null) {
            return BigDecimal.ZERO;
        }


        List<Repayment> repayments =
                repaymentRepository
                        .findByLoanId(loanId);


        return repayments.stream()

                .map(
                        repayment ->
                                repayment.getInterestAmount() != null
                                        ? BigDecimal.valueOf(
                                        repayment
                                                .getInterestAmount()
                                )
                                        : BigDecimal.ZERO
                )

                .reduce(
                        BigDecimal.ZERO,
                        BigDecimal::add
                );
    }


    // =========================================================
    // CALCULATE EMI
    // =========================================================

    private Double calculateEmi(
            Double principal,
            Double annualRate,
            Integer months,
            String interestType
    ) {

        if (
                principal == null
                        || annualRate == null
                        || months == null
                        || months <= 0
        ) {

            return 0.0;
        }


        // =====================================================
        // FLAT INTEREST
        // =====================================================

        if (
                "FLAT".equalsIgnoreCase(
                        interestType
                )
        ) {

            double totalInterest =
                    principal
                            * annualRate
                            / 100
                            * months
                            / 12;

            return (
                    principal
                            + totalInterest
            ) / months;
        }


        // =====================================================
        // REDUCING BALANCE
        // =====================================================

        double monthlyRate =
                annualRate
                        / 12
                        / 100;


        // -----------------------------------------------------
        // ZERO INTEREST
        // -----------------------------------------------------

        if (monthlyRate == 0) {

            return principal / months;
        }


        // -----------------------------------------------------
        // EMI FORMULA
        // -----------------------------------------------------

        double factor =
                Math.pow(
                        1 + monthlyRate,
                        months
                );


        return principal
                * monthlyRate
                * factor
                /
                (factor - 1);
    }
}