package com.example.loan_management.service;

import com.example.loan_management.auth.service.CurrentUserService;
import com.example.loan_management.dto.LoanDto;
import com.example.loan_management.entity.Group;
import com.example.loan_management.entity.Loan;
import com.example.loan_management.entity.Repayment;
import com.example.loan_management.entity.VoAlf;
import com.example.loan_management.entity.Women;
import com.example.loan_management.repository.ClScheduleRepository;
import com.example.loan_management.repository.LoanRepository;
import com.example.loan_management.repository.RepaymentRepository;
import com.example.loan_management.repository.VoAlfRepository;
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
    private final VoAlfRepository voAlfRepository;
    private final RepaymentRepository repaymentRepository;
    private final ClScheduleRepository clScheduleRepository;

    private final LoanImageService loanImageService;

    private final ModelMapper modelMapper;

    private final CurrentUserService currentUserService;


    // =========================================================
    // CURRENT CMRC
    // =========================================================

    private Long getCurrentCmrcId() {

        return currentUserService.getCurrentCmrcId();
    }


    // =========================================================
    // VALIDATE WOMAN OWNERSHIP
    //
    // Logged-in User
    //      ↓
    //     CMRC
    //      ↓
    //    Group
    //      ↓
    //    Woman
    // =========================================================

    private Women getAuthorizedWoman(Long womanId) {

        if (womanId == null) {
            throw new RuntimeException(
                    "Woman ID is required"
            );
        }

        Women woman =
                womenRepository.findById(womanId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Woman not found with ID: "
                                                + womanId
                                )
                        );

        if (woman.getGroup() == null) {

            throw new RuntimeException(
                    "Group not found for selected woman"
            );
        }

        Group group = woman.getGroup();

        Long currentCmrcId =
                getCurrentCmrcId();

        // -----------------------------------------------------
        // GROUP MUST BELONG TO CURRENT CMRC
        // -----------------------------------------------------

        if (group.getCmrcId() == null ||
                !currentCmrcId.equals(
                        group.getCmrcId()
                )) {

            throw new RuntimeException(
                    "Access denied. Woman does not belong to your CMRC"
            );
        }

        // -----------------------------------------------------
        // GROUP MUST HAVE VO / ALF
        // -----------------------------------------------------

        if (group.getVoAlfId() == null) {

            throw new RuntimeException(
                    "VO / ALF ID not found for selected group"
            );
        }

        // -----------------------------------------------------
        // VERIFY VO / ALF ALSO BELONGS TO CURRENT CMRC
        // -----------------------------------------------------

        getAuthorizedVoAlf(
                group.getVoAlfId()
        );

        return woman;
    }


    // =========================================================
    // VALIDATE VO / ALF OWNERSHIP
    //
    // Logged-in User
    //      ↓
    //     CMRC
    //      ↓
    //   VO / ALF
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
    //    Group
    //      ↓
    //    Woman
    //      ↓
    //    Loan
    // =========================================================

    private Loan getAuthorizedLoan(Long loanId) {

        Loan loan =
                loanRepository.findById(loanId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Loan not found with ID: "
                                                + loanId
                                )
                        );

        if (loan.getWoman() == null) {

            throw new RuntimeException(
                    "Woman not found for this loan"
            );
        }

        Women woman = loan.getWoman();

        if (woman.getGroup() == null) {

            throw new RuntimeException(
                    "Group not found for this loan"
            );
        }

        Group group = woman.getGroup();

        Long currentCmrcId =
                getCurrentCmrcId();

        // -----------------------------------------------------
        // GROUP -> CMRC VALIDATION
        // -----------------------------------------------------

        if (group.getCmrcId() == null ||
                !currentCmrcId.equals(
                        group.getCmrcId()
                )) {

            throw new RuntimeException(
                    "Access denied for this loan"
            );
        }

        // -----------------------------------------------------
        // GROUP -> VO / ALF VALIDATION
        // -----------------------------------------------------

        if (group.getVoAlfId() == null) {

            throw new RuntimeException(
                    "VO / ALF not found for this loan"
            );
        }

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
    // CREATE
    // =========================================================

    @Override
    public LoanDto create(LoanDto loanDto) {

        // -----------------------------------------------------
        // Validate Woman + CMRC + Group + VO/ALF
        // -----------------------------------------------------

        Women woman =
                getAuthorizedWoman(
                        loanDto.getWomanId()
                );

        Group group =
                woman.getGroup();


        // =====================================================
        // MAP DTO -> ENTITY
        // =====================================================

        Loan loan =
                modelMapper.map(
                        loanDto,
                        Loan.class
                );


        // =====================================================
        // HIERARCHY
        // =====================================================

        // Woman
        loan.setWoman(woman);

        // VO / ALF
        // NEVER TRUST FRONTEND VO/ALF ID
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

        Long currentCmrcId =
                getCurrentCmrcId();

        return loanRepository
                .findByWoman_Group_CmrcId(currentCmrcId)
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
                getAuthorizedLoan(id);

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
        // Existing Loan must belong to current CMRC
        // -----------------------------------------------------

        Loan loan =
                getAuthorizedLoan(id);


        // -----------------------------------------------------
        // New Woman must also belong to current CMRC
        // -----------------------------------------------------

        Women woman =
                getAuthorizedWoman(
                        loanDto.getWomanId()
                );

        Group group =
                woman.getGroup();


        // =====================================================
        // HIERARCHY
        // =====================================================

        loan.setWoman(woman);

        // NEVER TRUST FRONTEND VO/ALF ID
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

        // -----------------------------------------------------
        // IMPORTANT:
        // First verify Loan belongs to current CMRC.
        // -----------------------------------------------------

        Loan loan =
                getAuthorizedLoan(id);


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

        // -----------------------------------------------------
        // Woman must belong to current CMRC
        // -----------------------------------------------------

        getAuthorizedWoman(womanId);

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


        // =====================================================
        // LOAN BASIC DETAILS
        // =====================================================

        dto.setId(loan.getId());

        dto.setSanctionedAmount(
                loan.getSanctionedAmount()
        );

        dto.setProcessingFee(
                loan.getProcessingFee()
        );

        dto.setLoanAmount(
                loan.getLoanAmount()
        );

        dto.setLoanPurpose(
                loan.getLoanPurpose()
        );

        dto.setLoanGivenDate(
                loan.getLoanGivenDate()
        );

        dto.setRepaymentPeriodMonths(
                loan.getRepaymentPeriodMonths()
        );

        dto.setInterestRate(
                loan.getInterestRate()
        );

        dto.setInterestType(
                loan.getInterestType()
        );

        dto.setMonthlyEmi(
                loan.getMonthlyEmi()
        );

        dto.setLoanStatus(
                loan.getLoanStatus()
        );


        // =====================================================
        // VO / ALF ID
        // =====================================================

        dto.setVoAlfId(
                loan.getVoAlfId()
        );


        // =====================================================
        // WOMAN
        // =====================================================

        if (loan.getWoman() != null) {

            Women woman =
                    loan.getWoman();

            dto.setWomanId(
                    woman.getId()
            );

            dto.setWomanName(
                    woman.getWomanName()
            );


            // =================================================
            // GROUP
            // =================================================

            if (woman.getGroup() != null) {

                Group group =
                        woman.getGroup();

                dto.setGroupId(
                        group.getId()
                );

                dto.setGroupName(
                        group.getGroupName()
                );

                dto.setVillageName(
                        group.getVillageName()
                );


                // ---------------------------------------------
                // CMRC
                // ---------------------------------------------

                dto.setCmrcId(
                        group.getCmrcId()
                );


                // ---------------------------------------------
                // VO / ALF
                // ---------------------------------------------

                dto.setVoAlfId(
                        group.getVoAlfId()
                );
            }
        }


        // =====================================================
        // TOTAL INTEREST RECEIVED
        // =====================================================

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