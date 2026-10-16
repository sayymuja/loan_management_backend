        package com.example.loan_management.service;

import com.example.loan_management.auth.service.CurrentUserService;
import com.example.loan_management.dto.LoanDto;
import com.example.loan_management.entity.FundTransaction;
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

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class LoanServiceImpl implements LoanService {

    private final LoanRepository loanRepository;
    private final WomenRepository womenRepository;
    private final VoAlfRepository voAlfRepository;
    private final RepaymentRepository repaymentRepository;
    private final ClScheduleRepository clScheduleRepository;
    private final FundManagementService fundManagementService;
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
    // =========================================================

    private Women getAuthorizedWoman(Long womanId) {

        if (womanId == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Woman ID is required."
            );
        }

        Women woman = womenRepository.findById(womanId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Woman not found with ID: " + womanId
                ));

        if (woman.getGroup() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Group not found for selected woman."
            );
        }

        Group group = woman.getGroup();
        Long currentCmrcId = getCurrentCmrcId();

        if (currentCmrcId == null
                || group.getCmrcId() == null
                || !currentCmrcId.equals(group.getCmrcId())) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Access denied. Woman does not belong to your CMRC."
            );
        }

        if (group.getVoAlfId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "VO / ALF ID not found for selected group."
            );
        }

        getAuthorizedVoAlf(group.getVoAlfId());

        return woman;
    }

    // =========================================================
    // VALIDATE VO / ALF OWNERSHIP
    // =========================================================

    private VoAlf getAuthorizedVoAlf(Long voAlfId) {

        if (voAlfId == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "VO / ALF ID is required."
            );
        }

        VoAlf voAlf = voAlfRepository.findById(voAlfId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "VO / ALF not found with ID: " + voAlfId
                ));

        Long currentCmrcId = getCurrentCmrcId();

        if (currentCmrcId == null
                || voAlf.getCmrc() == null
                || !currentCmrcId.equals(voAlf.getCmrc().getId())) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Access denied for this VO / ALF."
            );
        }

        return voAlf;
    }

    // =========================================================
    // VALIDATE LOAN OWNERSHIP
    // =========================================================

    private Loan getAuthorizedLoan(Long loanId) {

        if (loanId == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Loan ID is required."
            );
        }

        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Loan not found with ID: " + loanId
                ));

        if (loan.getWoman() == null
                || loan.getWoman().getGroup() == null) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Woman or group not found for this loan."
            );
        }

        Group group = loan.getWoman().getGroup();
        Long currentCmrcId = getCurrentCmrcId();

        if (currentCmrcId == null
                || group.getCmrcId() == null
                || !currentCmrcId.equals(group.getCmrcId())) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Access denied for this loan."
            );
        }

        if (group.getVoAlfId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "VO / ALF not found for this loan."
            );
        }

        getAuthorizedVoAlf(group.getVoAlfId());

        if (loan.getVoAlfId() == null
                || !group.getVoAlfId().equals(loan.getVoAlfId())) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Loan VO / ALF does not match the woman's group."
            );
        }

        return loan;
    }

    // =========================================================
    // CREATE LOAN + SINGLE-BALANCE DISBURSEMENT
    // =========================================================

    @Override
    public LoanDto create(LoanDto loanDto) {

        if (loanDto == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Loan details are required."
            );
        }

        // Validate borrower and derive hierarchy from database.
        Women woman = getAuthorizedWoman(loanDto.getWomanId());
        Group group = woman.getGroup();

        // Validate loan inputs.
        validateLoanDetails(loanDto);

        double disbursedAmount = calculateDisbursedAmount(
                loanDto.getSanctionedAmount(),
                loanDto.getProcessingFee()
        );

        if (disbursedAmount <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Disbursed loan amount must be greater than zero."
            );
        }

        // Build entity using trusted database relationships.
        Loan loan = modelMapper.map(loanDto, Loan.class);

        loan.setWoman(woman);
        loan.setVoAlfId(group.getVoAlfId());

        loan.setSanctionedAmount(loanDto.getSanctionedAmount());
        loan.setProcessingFee(loanDto.getProcessingFee());
        loan.setLoanAmount(disbursedAmount);

        loan.setLoanPurpose(loanDto.getLoanPurpose());
        loan.setLoanGivenDate(loanDto.getLoanGivenDate());

        loan.setRepaymentPeriodMonths(
                loanDto.getRepaymentPeriodMonths()
        );

        loan.setInterestRate(loanDto.getInterestRate());
        loan.setInterestType(loanDto.getInterestType());

        loan.setMonthlyEmi(
                calculateEmi(
                        disbursedAmount,
                        loanDto.getInterestRate(),
                        loanDto.getRepaymentPeriodMonths(),
                        loanDto.getInterestType()
                )
        );

        loan.setLoanStatus(
                loanDto.getLoanStatus() != null
                        && !loanDto.getLoanStatus().isBlank()
                        ? loanDto.getLoanStatus()
                        : "ACTIVE"
        );

        // Save loan first.
        Loan savedLoan = loanRepository.save(loan);

        // Record disbursement as a PAYMENT against the single balance.
        // No Cash/Bank payment mode is used.
        // Fund service validates sufficient funds.
        // CMRC totalFund is not changed here.
        fundManagementService.recordInternalTransaction(
                savedLoan.getVoAlfId(),
                FundTransaction.TransactionType.PAYMENT,
                FundTransaction.FundCategory.LOAN_DISBURSEMENT,
                BigDecimal.valueOf(savedLoan.getLoanAmount()),
                savedLoan.getLoanGivenDate(),
                "Loan disbursement for loan ID " + savedLoan.getId(),
                "LOAN",
                savedLoan.getId(),
                "LOAN_DISBURSEMENT_" + savedLoan.getId()
        );

        return mapLoanToDto(savedLoan);
    }

    // =========================================================
    // VALIDATE LOAN DETAILS
    // =========================================================

    private void validateLoanDetails(LoanDto loanDto) {

        if (loanDto.getSanctionedAmount() == null
                || loanDto.getSanctionedAmount() <= 0) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Sanctioned amount must be greater than zero."
            );
        }

        if (loanDto.getProcessingFee() == null
                || loanDto.getProcessingFee() < 0) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Processing fee cannot be negative."
            );
        }

        if (loanDto.getProcessingFee()
                >= loanDto.getSanctionedAmount()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Processing fee must be less than sanctioned amount."
            );
        }

        if (loanDto.getLoanGivenDate() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Loan given date is required."
            );
        }

        if (loanDto.getRepaymentPeriodMonths() == null
                || loanDto.getRepaymentPeriodMonths() <= 0) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Repayment period must be greater than zero."
            );
        }

        if (loanDto.getInterestRate() == null
                || loanDto.getInterestRate() < 0) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Interest rate cannot be negative."
            );
        }

        String interestType = loanDto.getInterestType();

        if (interestType == null
                || (!"FLAT".equalsIgnoreCase(interestType)
                && !"REDUCING".equalsIgnoreCase(interestType))) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Interest type must be FLAT or REDUCING."
            );
        }
    }

    // =========================================================
    // GET ALL LOANS
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public List<LoanDto> getAll() {

        Long currentCmrcId = getCurrentCmrcId();

        if (currentCmrcId == null) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "CMRC is not associated with the current user."
            );
        }

        return loanRepository
                .findByWoman_Group_CmrcId(currentCmrcId)
                .stream()
                .map(this::mapLoanToDto)
                .toList();
    }

    // =========================================================
    // GET LOAN BY ID
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public LoanDto getById(Long id) {

        Loan loan = getAuthorizedLoan(id);

        return mapLoanToDto(loan);
    }

    // =========================================================
    // UPDATE LOAN
    // Does not create a second disbursement transaction.
    // =========================================================

    @Override
    public LoanDto update(Long id, LoanDto loanDto) {

        Loan loan = getAuthorizedLoan(id);

        if (loanDto == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Loan details are required."
            );
        }

        Women woman = getAuthorizedWoman(loanDto.getWomanId());

        validateLoanDetails(loanDto);

        double disbursedAmount = calculateDisbursedAmount(
                loanDto.getSanctionedAmount(),
                loanDto.getProcessingFee()
        );

        if (disbursedAmount <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Disbursed loan amount must be greater than zero."
            );
        }

        // Preserve trusted hierarchy.
        loan.setWoman(woman);
        loan.setVoAlfId(woman.getGroup().getVoAlfId());

        loan.setSanctionedAmount(loanDto.getSanctionedAmount());
        loan.setProcessingFee(loanDto.getProcessingFee());
        loan.setLoanAmount(disbursedAmount);

        loan.setLoanPurpose(loanDto.getLoanPurpose());
        loan.setLoanGivenDate(loanDto.getLoanGivenDate());

        loan.setRepaymentPeriodMonths(
                loanDto.getRepaymentPeriodMonths()
        );

        loan.setInterestRate(loanDto.getInterestRate());
        loan.setInterestType(loanDto.getInterestType());

        loan.setMonthlyEmi(
                calculateEmi(
                        disbursedAmount,
                        loanDto.getInterestRate(),
                        loanDto.getRepaymentPeriodMonths(),
                        loanDto.getInterestType()
                )
        );

        if (loanDto.getLoanStatus() != null
                && !loanDto.getLoanStatus().isBlank()) {
            loan.setLoanStatus(loanDto.getLoanStatus());
        }

        // Do not create another disbursement entry here.
        // Do not change the CMRC totalFund.
        // Financial changes to an already-disbursed loan should
        // use a separately approved adjustment workflow.

        Loan updatedLoan = loanRepository.save(loan);

        return mapLoanToDto(updatedLoan);
    }

    // =========================================================
    // DELETE LOAN
    // =========================================================

    @Override
    public void delete(Long id) {

        Loan loan = getAuthorizedLoan(id);

        // Do not silently delete a disbursed loan's ledger entry.
        // A disbursed loan requires a proper reversal/refund workflow.
        FundTransaction disbursement =
                fundManagementService.getTransactionByPaymentEventId(
                        "LOAN_DISBURSEMENT_" + id
                );

        if (disbursement != null) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This loan has a disbursement ledger entry. "
                            + "Process an approved reversal before deleting it."
            );
        }

        clScheduleRepository.deleteAll(
                clScheduleRepository.findByLoanId(id)
        );

        repaymentRepository.deleteByLoanId(id);

        loanImageService.deleteByLoanId(id);

        loanRepository.delete(loan);
    }

    // =========================================================
    // GET LOANS BY WOMAN
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public List<LoanDto> getByWomanId(Long womanId) {

        getAuthorizedWoman(womanId);

        return loanRepository
                .findByWomanId(womanId)
                .stream()
                .map(this::mapLoanToDto)
                .toList();
    }

    // =========================================================
    // ENTITY TO DTO
    // =========================================================

    private LoanDto mapLoanToDto(Loan loan) {

        LoanDto dto = new LoanDto();

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

        // No paymentMode field.

        dto.setVoAlfId(loan.getVoAlfId());

        if (loan.getWoman() != null) {

            Women woman = loan.getWoman();

            dto.setWomanId(woman.getId());
            dto.setWomanName(woman.getWomanName());

            if (woman.getGroup() != null) {

                Group group = woman.getGroup();

                dto.setGroupId(group.getId());
                dto.setGroupName(group.getGroupName());
                dto.setVillageName(group.getVillageName());
                dto.setCmrcId(group.getCmrcId());
                dto.setVoAlfId(group.getVoAlfId());

                if (group.getVoAlfId() != null) {
                    voAlfRepository.findById(group.getVoAlfId())
                            .ifPresent(voAlf -> {
                                dto.setVoAlfName(
                                        voAlf.getVoAlfName()
                                );

                                if (voAlf.getCmrc() != null) {
                                    dto.setCmrcId(
                                            voAlf.getCmrc().getId()
                                    );

                                    dto.setCmrcName(
                                            voAlf.getCmrc().getCmrcName()
                                    );
                                }
                            });
                }
            }
        }

        dto.setTotalInterestReceived(
                calculateTotalInterestReceived(loan.getId())
        );

        return dto;
    }

    // =========================================================
    // CALCULATE DISBURSED AMOUNT
    // =========================================================

    private Double calculateDisbursedAmount(
            Double sanctionedAmount,
            Double processingFee) {

        double sanctioned = sanctionedAmount != null
                ? sanctionedAmount
                : 0.0;

        double fee = processingFee != null
                ? processingFee
                : 0.0;

        return Math.max(sanctioned - fee, 0.0);
    }

    // =========================================================
    // TOTAL INTEREST RECEIVED
    // =========================================================

    private BigDecimal calculateTotalInterestReceived(Long loanId) {

        if (loanId == null) {
            return BigDecimal.ZERO;
        }

        List<Repayment> repayments =
                repaymentRepository.findByLoanId(loanId);

        return repayments.stream()
                .map(repayment ->
                        repayment.getInterestAmount() != null
                                ? BigDecimal.valueOf(
                                repayment.getInterestAmount()
                        )
                                : BigDecimal.ZERO
                )
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // =========================================================
    // CALCULATE EMI
    // =========================================================

    private Double calculateEmi(
            Double principal,
            Double annualRate,
            Integer months,
            String interestType) {

        if (principal == null
                || annualRate == null
                || months == null
                || months <= 0) {
            return 0.0;
        }

        // FLAT INTEREST
        if ("FLAT".equalsIgnoreCase(interestType)) {

            double totalInterest =
                    principal * annualRate / 100.0 * months / 12.0;

            return (principal + totalInterest) / months;
        }

        // REDUCING BALANCE
        double monthlyRate = annualRate / 12.0 / 100.0;

        if (monthlyRate == 0.0) {
            return principal / months;
        }

        double factor = Math.pow(1.0 + monthlyRate, months);

        return principal * monthlyRate * factor / (factor - 1.0);
    }
}
