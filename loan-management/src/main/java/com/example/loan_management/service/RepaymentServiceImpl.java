        package com.example.loan_management.service;

import com.example.loan_management.auth.service.CurrentUserService;
import com.example.loan_management.dto.RepaymentDto;
import com.example.loan_management.dto.RepaymentSummaryDto;
import com.example.loan_management.entity.FundTransaction;
import com.example.loan_management.entity.Group;
import com.example.loan_management.entity.Loan;
import com.example.loan_management.entity.Repayment;
import com.example.loan_management.entity.VoAlf;
import com.example.loan_management.entity.Women;
import com.example.loan_management.repository.LoanRepository;
import com.example.loan_management.repository.RepaymentRepository;
import com.example.loan_management.repository.VoAlfRepository;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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
    private final FundManagementService fundManagementService;

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
            throw new IllegalArgumentException(
                    "VO / ALF ID is required."
            );
        }

        VoAlf voAlf = voAlfRepository.findById(voAlfId)
                .orElseThrow(() -> new RuntimeException(
                        "VO / ALF not found with ID: " + voAlfId
                ));

        Long currentCmrcId = getCurrentCmrcId();

        if (currentCmrcId == null
                || voAlf.getCmrc() == null
                || !currentCmrcId.equals(voAlf.getCmrc().getId())) {
            throw new RuntimeException(
                    "Access denied for this VO / ALF."
            );
        }

        return voAlf;
    }

    // =========================================================
    // AUTHORIZE LOAN
    // =========================================================

    private Loan getAuthorizedLoan(Long loanId) {

        if (loanId == null) {
            throw new IllegalArgumentException("Loan ID is required.");
        }

        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException(
                        "Loan not found with ID: " + loanId
                ));

        if (loan.getWoman() == null) {
            throw new RuntimeException(
                    "Woman not found for this loan."
            );
        }

        Women woman = loan.getWoman();

        if (woman.getGroup() == null) {
            throw new RuntimeException(
                    "Group not found for this loan."
            );
        }

        Group group = woman.getGroup();
        Long currentCmrcId = getCurrentCmrcId();

        if (currentCmrcId == null
                || group.getCmrcId() == null
                || !currentCmrcId.equals(group.getCmrcId())) {
            throw new RuntimeException(
                    "Access denied for this loan."
            );
        }

        if (group.getVoAlfId() == null) {
            throw new RuntimeException(
                    "VO / ALF not found for this loan."
            );
        }

        getAuthorizedVoAlf(group.getVoAlfId());

        if (loan.getVoAlfId() == null
                || !group.getVoAlfId().equals(loan.getVoAlfId())) {
            throw new RuntimeException(
                    "Loan VO / ALF does not match the woman's group."
            );
        }

        return loan;
    }

    // =========================================================
    // UPDATE LEGACY VO / ALF RECEIVED FUND
    // CMRC totalFund is never changed here.
    // =========================================================

    private void updateVoAlfReceivedFund(
            Loan loan,
            double amountDelta) {

        if (loan == null || loan.getVoAlfId() == null) {
            throw new RuntimeException(
                    "VO / ALF not found for this loan."
            );
        }

        VoAlf voAlf = getAuthorizedVoAlf(loan.getVoAlfId());

        double currentBalance = voAlf.getReceivedFund() != null
                ? voAlf.getReceivedFund()
                : 0.0;

        double updatedBalance = round(currentBalance + amountDelta);

        if (updatedBalance < 0) {
            throw new IllegalStateException(
                    "VO / ALF received fund cannot become negative."
            );
        }

        voAlf.setReceivedFund(updatedBalance);
        voAlfRepository.save(voAlf);
    }

    // =========================================================
    // AUTHORIZE REPAYMENT
    // =========================================================

    private Repayment getAuthorizedRepayment(Long repaymentId) {

        if (repaymentId == null) {
            throw new IllegalArgumentException(
                    "Repayment ID is required."
            );
        }

        Repayment repayment = repaymentRepository.findById(repaymentId)
                .orElseThrow(() -> new RuntimeException(
                        "Repayment not found with ID: " + repaymentId
                ));

        if (repayment.getLoan() == null) {
            throw new RuntimeException(
                    "Loan not found for this repayment."
            );
        }

        getAuthorizedLoan(repayment.getLoan().getId());

        return repayment;
    }

    // =========================================================
    // CREATE
    // =========================================================

    @Override
    @Transactional
    public RepaymentDto create(RepaymentDto repaymentDto) {

        if (repaymentDto == null || repaymentDto.getLoanId() == null) {
            throw new IllegalArgumentException(
                    "Loan ID is required."
            );
        }

        Loan loan = getAuthorizedLoan(repaymentDto.getLoanId());

        Repayment repayment = modelMapper.map(
                repaymentDto,
                Repayment.class
        );

        repayment.setLoan(loan);

        calculateRepayment(repayment, loan);

        Repayment saved = repaymentRepository.save(repayment);

        RepaymentDto response = modelMapper.map(
                saved,
                RepaymentDto.class
        );

        response.setLoanId(saved.getLoan().getId());

        return response;
    }

    // =========================================================
    // GET ALL
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public List<RepaymentDto> getAll() {

        Long currentCmrcId = getCurrentCmrcId();

        return repaymentRepository.findAll()
                .stream()
                .filter(repayment ->
                        repayment.getLoan() != null
                                && repayment.getLoan().getWoman() != null
                                && repayment.getLoan().getWoman().getGroup() != null
                                && currentCmrcId != null
                                && currentCmrcId.equals(
                                repayment.getLoan()
                                        .getWoman()
                                        .getGroup()
                                        .getCmrcId()
                        )
                )
                .map(repayment -> {
                    RepaymentDto dto = modelMapper.map(
                            repayment,
                            RepaymentDto.class
                    );

                    dto.setLoanId(repayment.getLoan().getId());

                    return dto;
                })
                .collect(Collectors.toList());
    }

    // =========================================================
    // GET BY ID
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public RepaymentDto getById(Long id) {

        Repayment repayment = getAuthorizedRepayment(id);

        RepaymentDto dto = modelMapper.map(
                repayment,
                RepaymentDto.class
        );

        dto.setLoanId(repayment.getLoan().getId());

        return dto;
    }

    // =========================================================
    // UPDATE
    // =========================================================

    @Override
    @Transactional
    public RepaymentDto update(Long id, RepaymentDto repaymentDto) {

        Repayment repayment = getAuthorizedRepayment(id);

        if (repaymentDto == null || repaymentDto.getLoanId() == null) {
            throw new IllegalArgumentException(
                    "Loan ID is required."
            );
        }

        // Do not allow generic update to bypass payment processing.
        if (repayment.getPaidAmount() != null
                && repayment.getPaidAmount() > 0) {
            throw new IllegalStateException(
                    "A repayment with a payment cannot be edited through "
                            + "the general update API."
            );
        }

        Loan loan = getAuthorizedLoan(repaymentDto.getLoanId());

        repayment.setLoan(loan);
        repayment.setRepaymentDate(repaymentDto.getRepaymentDate());
        repayment.setPaidAmount(repaymentDto.getPaidAmount());
        repayment.setRegularRepayment(
                repaymentDto.getRegularRepayment()
        );
        repayment.setPenaltyAmount(
                repaymentDto.getPenaltyAmount()
        );
        repayment.setRemark(repaymentDto.getRemark());

        calculateRepayment(repayment, loan);

        Repayment updated = repaymentRepository.save(repayment);

        RepaymentDto response = modelMapper.map(
                updated,
                RepaymentDto.class
        );

        response.setLoanId(updated.getLoan().getId());

        return response;
    }

    // =========================================================
    // DELETE
    // =========================================================

    @Override
    @Transactional
    public void delete(Long id) {

        Repayment repayment = getAuthorizedRepayment(id);

        double paidAmount = repayment.getPaidAmount() != null
                ? repayment.getPaidAmount()
                : 0.0;

        double penaltyAmount = repayment.getPenaltyAmount() != null
                ? repayment.getPenaltyAmount()
                : 0.0;

        if (paidAmount > 0 || penaltyAmount > 0) {
            throw new IllegalStateException(
                    "A repayment with a payment or penalty cannot be deleted. "
                            + "Use an approved correction workflow."
            );
        }

        Long loanId = repayment.getLoan().getId();

        repaymentRepository.delete(repayment);

        updateLoanStatus(loanId);
    }

    // =========================================================
    // REPAYMENT SUMMARY
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public RepaymentSummaryDto getSummary(Long loanId) {

        Loan loan = getAuthorizedLoan(loanId);

        List<Repayment> repayments =
                repaymentRepository.findByLoanId(loanId);

        double totalPrincipal = repayments.stream()
                .mapToDouble(r -> valueOrZero(r.getPrincipalAmount()))
                .sum();

        double totalInterest = repayments.stream()
                .mapToDouble(r -> valueOrZero(r.getInterestAmount()))
                .sum();

        double totalRepayment = repayments.stream()
                .mapToDouble(r -> valueOrZero(r.getTotalAmount()))
                .sum();

        double totalPenalty = repayments.stream()
                .mapToDouble(r -> valueOrZero(r.getPenaltyAmount()))
                .sum();

        double loanAmount = valueOrZero(loan.getLoanAmount());

        double outstandingAmount = Math.max(
                loanAmount - totalPrincipal,
                0.0
        );

        RepaymentSummaryDto summary = new RepaymentSummaryDto();

        summary.setLoanId(loanId);
        summary.setLoanAmount(loan.getLoanAmount());
        summary.setTotalPrincipalPaid(round(totalPrincipal));
        summary.setTotalInterestPaid(round(totalInterest));
        summary.setTotalRepayment(round(totalRepayment));
        summary.setOutstandingAmount(round(outstandingAmount));
        summary.setTotalPenalty(round(totalPenalty));

        return summary;
    }

    // =========================================================
    // GET BY LOAN ID
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public List<RepaymentDto> getByLoanId(Long loanId) {

        getAuthorizedLoan(loanId);

        return repaymentRepository.findByLoanId(loanId)
                .stream()
                .map(repayment -> {
                    RepaymentDto dto = modelMapper.map(
                            repayment,
                            RepaymentDto.class
                    );

                    dto.setLoanId(repayment.getLoan().getId());

                    return dto;
                })
                .collect(Collectors.toList());
    }

    // =========================================================
    // GENERATE SCHEDULE
    // Never delete or regenerate existing repayments.
    // =========================================================

    @Override
    @Transactional
    public List<RepaymentDto> generateSchedule(Long loanId) {

        Loan loan = getAuthorizedLoan(loanId);

        List<Repayment> existing =
                repaymentRepository.findByLoanId(loanId);

        if (existing != null && !existing.isEmpty()) {
            return existing.stream()
                    .sorted((a, b) -> {
                        Integer noA = a.getInstallmentNo();
                        Integer noB = b.getInstallmentNo();

                        if (noA == null && noB == null) {
                            return 0;
                        }
                        if (noA == null) {
                            return 1;
                        }
                        if (noB == null) {
                            return -1;
                        }

                        return noA.compareTo(noB);
                    })
                    .map(repayment -> {
                        RepaymentDto dto = modelMapper.map(
                                repayment,
                                RepaymentDto.class
                        );

                        dto.setLoanId(repayment.getLoan().getId());

                        return dto;
                    })
                    .collect(Collectors.toList());
        }

        if (loan.getLoanAmount() == null
                || loan.getLoanAmount() <= 0) {
            throw new IllegalArgumentException(
                    "Loan amount is invalid."
            );
        }

        if (loan.getInterestRate() == null
                || loan.getInterestRate() < 0) {
            throw new IllegalArgumentException(
                    "Interest rate is invalid."
            );
        }

        if (loan.getMonthlyEmi() == null
                || loan.getMonthlyEmi() <= 0) {
            throw new IllegalArgumentException(
                    "Monthly EMI is invalid."
            );
        }

        if (loan.getRepaymentPeriodMonths() == null
                || loan.getRepaymentPeriodMonths() <= 0) {
            throw new IllegalArgumentException(
                    "Repayment period is invalid."
            );
        }

        if (loan.getLoanGivenDate() == null) {
            throw new IllegalArgumentException(
                    "Loan given date is required."
            );
        }

        double outstandingPrincipal = loan.getLoanAmount();
        double monthlyRate = loan.getInterestRate() / 12.0 / 100.0;
        double emi = loan.getMonthlyEmi();

        int totalInstallments = loan.getRepaymentPeriodMonths();

        LocalDate firstInstallmentDate =
                loan.getLoanGivenDate().plusMonths(1);

        List<Repayment> schedule = new ArrayList<>();

        for (int i = 1; i <= totalInstallments; i++) {

            Repayment repayment = new Repayment();

            repayment.setLoan(loan);
            repayment.setInstallmentNo(i);
            repayment.setInstallmentDate(
                    firstInstallmentDate.plusMonths(i - 1)
            );

            double interestAmount =
                    outstandingPrincipal * monthlyRate;

            double principalAmount = Math.max(
                    0.0,
                    emi - interestAmount
            );

            principalAmount = Math.min(
                    principalAmount,
                    outstandingPrincipal
            );

            if (i == totalInstallments) {
                principalAmount = outstandingPrincipal;
            }

            double scheduledAmount =
                    principalAmount + interestAmount;

            repayment.setPrincipalAmount(round(principalAmount));
            repayment.setInterestAmount(round(interestAmount));
            repayment.setScheduledAmount(round(scheduledAmount));
            repayment.setPaidAmount(0.0);
            repayment.setPenaltyAmount(0.0);
            repayment.setTotalAmount(0.0);
            repayment.setRegularRepayment(false);
            repayment.setPaymentStatus("PENDING");
            repayment.setRepaymentDate(null);
            repayment.setRemark(null);

            schedule.add(repayment);

            outstandingPrincipal = Math.max(
                    0.0,
                    outstandingPrincipal - principalAmount
            );
        }

        List<Repayment> saved = repaymentRepository.saveAll(schedule);

        return saved.stream()
                .map(repayment -> {
                    RepaymentDto dto = modelMapper.map(
                            repayment,
                            RepaymentDto.class
                    );

                    dto.setLoanId(repayment.getLoan().getId());

                    return dto;
                })
                .collect(Collectors.toList());
    }

    // =========================================================
    // CALCULATE REPAYMENT
    // =========================================================

    private void calculateRepayment(
            Repayment repayment,
            Loan loan) {

        double paidAmount = valueOrZero(repayment.getPaidAmount());
        double loanAmount = valueOrZero(loan.getLoanAmount());
        double annualRate = valueOrZero(loan.getInterestRate());

        List<Repayment> previous =
                repaymentRepository.findByLoanId(loan.getId());

        double paidPrincipal = previous.stream()
                .filter(r ->
                        r.getId() == null
                                || !r.getId().equals(repayment.getId())
                )
                .filter(r ->
                        "PAID".equalsIgnoreCase(r.getPaymentStatus())
                                || "PARTIAL".equalsIgnoreCase(
                                r.getPaymentStatus()
                        )
                )
                .mapToDouble(r ->
                        Math.max(valueOrZero(r.getPrincipalAmount()), 0.0)
                )
                .sum();

        double outstandingPrincipal = Math.max(
                loanAmount - paidPrincipal,
                0.0
        );

        double monthlyInterest =
                outstandingPrincipal * annualRate / 12.0 / 100.0;

        double interest = Math.max(
                0.0,
                Math.min(paidAmount, monthlyInterest)
        );

        double principalPaid = Math.max(
                0.0,
                paidAmount - interest
        );

        repayment.setInterestAmount(round(interest));
        repayment.setPrincipalAmount(round(principalPaid));
    }

    // =========================================================
    // PAY EMI - LEGACY SIGNATURE
    // The event ID is mandatory to prevent duplicate payments.
    // =========================================================

    @Override
    @Transactional
    public RepaymentDto payEmi(
            Long repaymentId,
            Double paidAmount,
            Double penaltyAmount,
            Boolean regularRepayment) {

        throw new IllegalStateException(
                "Payment event ID is required for EMI payment."
        );
    }

    // =========================================================
    // PAY EMI - SINGLE BALANCE + IDEMPOTENCY
    // paymentEventId is the fifth parameter.
    // =========================================================

    @Override
    @Transactional
    public RepaymentDto payEmi(
            Long repaymentId,
            Double paidAmount,
            Double penaltyAmount,
            Boolean regularRepayment,
            String paymentEventId) {

        if (paymentEventId == null || paymentEventId.isBlank()) {
            throw new IllegalArgumentException(
                    "Payment event ID is required."
            );
        }

        String eventId = paymentEventId.trim();

        // Leave room for the ":PENALTY" suffix.
        if (eventId.length() > 82) {
            throw new IllegalArgumentException(
                    "Payment event ID must not exceed 82 characters."
            );
        }

        double payment = round(valueOrZero(paidAmount));
        double requestedPenalty = round(valueOrZero(penaltyAmount));

        if (payment <= 0) {
            throw new IllegalArgumentException(
                    "Payment amount must be greater than zero."
            );
        }

        if (requestedPenalty < 0) {
            throw new IllegalArgumentException(
                    "Penalty amount cannot be negative."
            );
        }

        Repayment repayment = getAuthorizedRepayment(repaymentId);
        Loan loan = repayment.getLoan();

        double existingPenalty = round(
                valueOrZero(repayment.getPenaltyAmount())
        );

        // Check idempotency before applying any balance changes.
        FundTransaction existing =
                fundManagementService.getTransactionByPaymentEventId(
                        eventId
                );

        if (existing != null) {

            boolean matches =
                    existing.getTransactionType()
                            == FundTransaction.TransactionType.RECEIPT
                            && existing.getCategory()
                            == FundTransaction.FundCategory.LOAN_REPAYMENT
                            && existing.getReferenceId() != null
                            && existing.getReferenceId().equals(repaymentId)
                            && "REPAYMENT".equals(
                            existing.getReferenceType()
                    )
                            && existing.getAmount() != null
                            && existing.getAmount().compareTo(
                            BigDecimal.valueOf(payment)
                    ) == 0;

            if (!matches) {
                throw new IllegalStateException(
                        "Payment event ID is already used for a different transaction."
                );
            }

            if (Math.abs(existingPenalty - requestedPenalty) > 0.001) {
                throw new IllegalStateException(
                        "This payment event was already processed "
                                + "with a different penalty."
                );
            }

            RepaymentDto duplicateResponse = modelMapper.map(
                    repayment,
                    RepaymentDto.class
            );

            duplicateResponse.setLoanId(loan.getId());

            return duplicateResponse;
        }

        double scheduled = valueOrZero(repayment.getScheduledAmount());
        double alreadyPaid = valueOrZero(repayment.getPaidAmount());

        if (scheduled <= 0) {
            throw new IllegalStateException(
                    "Scheduled EMI amount is invalid."
            );
        }

        if ("PAID".equalsIgnoreCase(repayment.getPaymentStatus())) {
            throw new IllegalStateException(
                    "This EMI is already fully paid."
            );
        }

        double remaining = round(
                Math.max(0.0, scheduled - alreadyPaid)
        );

        if (payment > remaining + 0.001) {
            throw new IllegalArgumentException(
                    "Payment exceeds remaining EMI amount: " + remaining
            );
        }

        if (requestedPenalty + 0.001 < existingPenalty) {
            throw new IllegalArgumentException(
                    "Penalty cannot be reduced without an approved correction."
            );
        }

        double penaltyDelta = round(
                Math.max(0.0, requestedPenalty - existingPenalty)
        );

        double totalPaid = round(alreadyPaid + payment);

        if (totalPaid > scheduled && totalPaid - scheduled <= 0.001) {
            totalPaid = scheduled;
        }

        if (totalPaid > scheduled) {
            throw new IllegalArgumentException(
                    "Total paid amount cannot exceed scheduled EMI."
            );
        }

        if (loan.getVoAlfId() == null) {
            throw new IllegalStateException(
                    "VO / ALF not found for this loan."
            );
        }

        // Record the EMI receipt in the fund ledger.
        fundManagementService.recordInternalTransaction(
                loan.getVoAlfId(),
                FundTransaction.TransactionType.RECEIPT,
                FundTransaction.FundCategory.LOAN_REPAYMENT,
                BigDecimal.valueOf(payment),
                LocalDate.now(),
                "EMI repayment for repayment ID " + repaymentId,
                "REPAYMENT",
                repaymentId,
                eventId
        );

        // Record only the incremental penalty.
        if (penaltyDelta > 0) {
            fundManagementService.recordInternalTransaction(
                    loan.getVoAlfId(),
                    FundTransaction.TransactionType.RECEIPT,
                    FundTransaction.FundCategory.PENALTY_RECEIVED,
                    BigDecimal.valueOf(penaltyDelta),
                    LocalDate.now(),
                    "Penalty for repayment ID " + repaymentId,
                    "REPAYMENT_PENALTY",
                    repaymentId,
                    eventId + ":PENALTY"
            );
        }

        repayment.setPaidAmount(totalPaid);
        repayment.setPenaltyAmount(requestedPenalty);
        repayment.setRegularRepayment(
                Boolean.TRUE.equals(regularRepayment)
        );
        repayment.setRepaymentDate(LocalDate.now());

        repayment.setPaymentStatus(
                totalPaid + 0.001 >= scheduled
                        ? "PAID"
                        : "PARTIAL"
        );

        calculateRepayment(repayment, loan);

        repayment.setTotalAmount(
                round(totalPaid + requestedPenalty)
        );

        Repayment saved = repaymentRepository.save(repayment);

        // Update the legacy VO/ALF receivedFund field only.
        // CMRC totalFund is never changed by this service.
        updateVoAlfReceivedFund(saved.getLoan(), payment);

        if (penaltyDelta > 0) {
            updateVoAlfReceivedFund(
                    saved.getLoan(),
                    penaltyDelta
            );
        }

        updateLoanStatus(saved.getLoan().getId());

        RepaymentDto response = modelMapper.map(
                saved,
                RepaymentDto.class
        );

        response.setLoanId(saved.getLoan().getId());

        return response;
    }

    // =========================================================
    // EDIT PAID EMI
    // Disabled until an approved correction/refund workflow exists.
    // =========================================================

    @Override
    @Transactional
    public RepaymentDto editPaidEmi(
            Long repaymentId,
            Double paidAmount,
            Double penaltyAmount) {

        getAuthorizedRepayment(repaymentId);

        throw new IllegalStateException(
                "Paid EMI editing is disabled. "
                        + "An approved correction/refund transaction "
                        + "is required before editing."
        );
    }

    // =========================================================
    // UPDATE LOAN STATUS
    // =========================================================

    private void updateLoanStatus(Long loanId) {

        Loan loan = getAuthorizedLoan(loanId);

        List<Repayment> repayments =
                repaymentRepository.findByLoanId(loanId);

        Integer totalInstallments =
                loan.getRepaymentPeriodMonths();

        if (totalInstallments == null || totalInstallments <= 0) {
            loan.setLoanStatus("ACTIVE");
            loanRepository.save(loan);
            return;
        }

        long paidInstallments = repayments.stream()
                .filter(r ->
                        "PAID".equalsIgnoreCase(r.getPaymentStatus())
                )
                .count();

        loan.setLoanStatus(
                paidInstallments >= totalInstallments
                        ? "CLOSED"
                        : "ACTIVE"
        );

        loanRepository.save(loan);
    }

    // =========================================================
    // HELPERS
    // =========================================================

    private double valueOrZero(Double value) {
        return value != null ? value : 0.0;
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
