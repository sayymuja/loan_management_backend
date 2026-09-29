package com.example.loan_management.service;

import com.example.loan_management.dto.LoanDto;
import com.example.loan_management.entity.Loan;
import com.example.loan_management.entity.Repayment;
import com.example.loan_management.entity.VoAlf;
import com.example.loan_management.repository.ClScheduleRepository;
import com.example.loan_management.repository.LoanRepository;
import com.example.loan_management.repository.RepaymentRepository;
import com.example.loan_management.repository.VoAlfRepository;
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
    private final VoAlfRepository voAlfRepository;
    private final RepaymentRepository repaymentRepository;
    private final ClScheduleRepository clScheduleRepository;
    private final ModelMapper modelMapper;
    // =========================================================
    // CREATE
    // =========================================================

    @Override
    public LoanDto create(LoanDto loanDto) {

        // -----------------------------------------------------
        // Find VO / ALF
        // -----------------------------------------------------

        VoAlf voAlf =
                voAlfRepository.findById(
                        loanDto.getVoAlfId()
                ).orElseThrow(
                        () -> new RuntimeException(
                                "VO/ALF not found"
                        )
                );

        // -----------------------------------------------------
        // Map DTO -> Entity
        // -----------------------------------------------------

        Loan loan =
                modelMapper.map(
                        loanDto,
                        Loan.class
                );

        loan.setVoAlf(voAlf);

        // -----------------------------------------------------
        // Calculate Disbursed / Loan Amount
        //
        // loanAmount =
        // sanctionedAmount - processingFee
        // -----------------------------------------------------

        loan.setLoanAmount(
                calculateDisbursedAmount(
                        loanDto.getSanctionedAmount(),
                        loanDto.getProcessingFee()
                )
        );

        // -----------------------------------------------------
        // Calculate EMI on SANCTIONED AMOUNT
        // -----------------------------------------------------

        loan.setMonthlyEmi(
                calculateEmi(
                        loan.getLoanAmount(),
                        loanDto.getInterestRate(),
                        loanDto.getRepaymentPeriodMonths(),
                        loanDto.getInterestType()
                )
        );

        // -----------------------------------------------------
        // New loan is ACTIVE
        // -----------------------------------------------------

        loan.setLoanStatus("ACTIVE");

        // -----------------------------------------------------
        // Save
        // -----------------------------------------------------

        Loan savedLoan =
                loanRepository.save(loan);

        // -----------------------------------------------------
        // Entity -> DTO
        // -----------------------------------------------------

        LoanDto response =
                modelMapper.map(
                        savedLoan,
                        LoanDto.class
                );

        if (savedLoan.getVoAlf() != null) {

            response.setVoAlfId(
                    savedLoan.getVoAlf().getId()
            );
        }

        response.setLoanStatus(
                savedLoan.getLoanStatus()
        );

        response.setTotalInterestReceived(
                calculateTotalInterestReceived(
                        savedLoan.getId()
                )
        );

        return response;
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
                                        "Loan not found"
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
        // Find existing loan
        // -----------------------------------------------------

        Loan loan =
                loanRepository.findById(id)
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Loan not found"
                                )
                        );

        // -----------------------------------------------------
        // Find VO / ALF
        // -----------------------------------------------------

        VoAlf voAlf =
                voAlfRepository.findById(
                        loanDto.getVoAlfId()
                ).orElseThrow(
                        () -> new RuntimeException(
                                "VO/ALF not found"
                        )
                );

        loan.setVoAlf(voAlf);

        // =====================================================
        // BORROWER DETAILS
        // =====================================================

        loan.setGroupName(
                loanDto.getGroupName()
        );

        loan.setWomanName(
                loanDto.getWomanName()
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
        // loanAmount = Disbursed Amount
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
        //
        // IMPORTANT:
        // EMI is calculated on SANCTIONED AMOUNT
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
        // SAVE
        // =====================================================

        Loan updatedLoan =
                loanRepository.save(loan);

        return mapLoanToDto(
                updatedLoan
        );
    }

    // =========================================================
    // DELETE
    // =========================================================

    @Override
    @Transactional
    public void delete(Long id) {

        Loan loan =
                loanRepository.findById(id)
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Loan not found"
                                )
                        );

        // -----------------------------------------------------
        // Delete CL Schedule records
        // -----------------------------------------------------

        clScheduleRepository.deleteAll(
                clScheduleRepository.findByLoanId(id)
        );

        // -----------------------------------------------------
        // Delete Repayment records
        // -----------------------------------------------------

        repaymentRepository.deleteByLoanId(id);

        // -----------------------------------------------------
        // Finally delete Loan
        // -----------------------------------------------------

        loanRepository.delete(loan);
    }

    // =========================================================
    // GET BY VO / ALF
    // =========================================================

    @Override
    public List<LoanDto> getByVoAlfId(
            Long voAlfId
    ) {

        return loanRepository
                .findByVoAlfId(voAlfId)
                .stream()
                .map(this::mapLoanToDto)
                .toList();
    }

    // =========================================================
    // ENTITY -> DTO
    // =========================================================

    private LoanDto mapLoanToDto(
            Loan loan
    ) {

        LoanDto dto =
                modelMapper.map(
                        loan,
                        LoanDto.class
                );

        // -----------------------------------------------------
        // VO / ALF ID
        // -----------------------------------------------------

        if (loan.getVoAlf() != null) {

            dto.setVoAlfId(
                    loan.getVoAlf().getId()
            );
        }

        // -----------------------------------------------------
        // Loan Status
        // -----------------------------------------------------

        dto.setLoanStatus(

                loan.getLoanStatus() != null
                        ? loan.getLoanStatus()
                        : "ACTIVE"

        );

        // -----------------------------------------------------
        // Total Interest Received
        // -----------------------------------------------------

        dto.setTotalInterestReceived(

                calculateTotalInterestReceived(
                        loan.getId()
                )

        );

        return dto;
    }

    // =========================================================
    // CALCULATE DISBURSED / LOAN AMOUNT
    // =========================================================
    //
    // Formula:
    //
    // loanAmount =
    // sanctionedAmount - processingFee
    //
    // Example:
    //
    // 4,000,000 - 20,000
    // = 3,980,000
    //
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
    //
    // IMPORTANT:
    //
    // principal = SANCTIONED AMOUNT
    //
    // NOT loanAmount.
    //
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