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
            throw new RuntimeException("VO / ALF ID is required");
        }

        VoAlf voAlf = voAlfRepository.findById(voAlfId)
                .orElseThrow(() -> new RuntimeException(
                        "VO / ALF not found with ID: " + voAlfId
                ));

        Long currentCmrcId = getCurrentCmrcId();

        if (voAlf.getCmrc() == null
                || !currentCmrcId.equals(voAlf.getCmrc().getId())) {
            throw new RuntimeException(
                    "Access denied for this VO / ALF"
            );
        }

        return voAlf;
    }

    // =========================================================
    // AUTHORIZE LOAN
    // =========================================================

    private Loan getAuthorizedLoan(Long loanId) {

        if (loanId == null) {
            throw new RuntimeException("Loan ID is required");
        }

        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException(
                        "Loan not found with ID: " + loanId
                ));

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
        Long currentCmrcId = getCurrentCmrcId();

        if (group.getCmrcId() == null
                || !currentCmrcId.equals(group.getCmrcId())) {
            throw new RuntimeException(
                    "Access denied for this loan"
            );
        }

        if (group.getVoAlfId() == null) {
            throw new RuntimeException(
                    "VO / ALF not found for this loan"
            );
        }

        getAuthorizedVoAlf(group.getVoAlfId());

        if (loan.getVoAlfId() == null
                || !group.getVoAlfId().equals(loan.getVoAlfId())) {
            throw new RuntimeException(
                    "Loan VO / ALF does not match woman's group"
            );
        }

        return loan;
    }

    // =========================================================
    // UPDATE VO / ALF RECEIVED FUND ONLY
    // Positive amountDelta = add funds
    // Negative amountDelta = reverse funds
    // CMRC totalFund is never changed here.
    // =========================================================

    private void updateVoAlfReceivedFund(
            Loan loan,
            double amountDelta) {

        if (loan == null || loan.getVoAlfId() == null) {
            throw new RuntimeException(
                    "VO / ALF not found for this loan"
            );
        }

        VoAlf voAlf = getAuthorizedVoAlf(loan.getVoAlfId());

        double currentReceivedFund =
                voAlf.getReceivedFund() != null
                        ? voAlf.getReceivedFund()
                        : 0.0;

        double updatedReceivedFund =
                round(currentReceivedFund + amountDelta);

        if (updatedReceivedFund < 0) {
            throw new RuntimeException(
                    "VO / ALF received fund cannot become negative"
            );
        }

        voAlf.setReceivedFund(updatedReceivedFund);
        voAlfRepository.save(voAlf);
    }

    // =========================================================
    // AUTHORIZE REPAYMENT
    // =========================================================

    private Repayment getAuthorizedRepayment(Long repaymentId) {

        if (repaymentId == null) {
            throw new RuntimeException("Repayment ID is required");
        }

        Repayment repayment = repaymentRepository.findById(repaymentId)
                .orElseThrow(() -> new RuntimeException(
                        "Repayment not found with id: " + repaymentId
                ));

        if (repayment.getLoan() == null) {
            throw new RuntimeException(
                    "Loan not found for this repayment"
            );
        }

        getAuthorizedLoan(repayment.getLoan().getId());

        return repayment;
    }

    // =========================================================
    // CREATE
    // =========================================================

    @Override
    public RepaymentDto create(RepaymentDto repaymentDto) {

        Loan loan = getAuthorizedLoan(repaymentDto.getLoanId());

        Repayment repayment = modelMapper.map(
                repaymentDto,
                Repayment.class
        );

        repayment.setLoan(loan);

        calculateRepayment(repayment, loan);

        Repayment savedRepayment =
                repaymentRepository.save(repayment);

        RepaymentDto response = modelMapper.map(
                savedRepayment,
                RepaymentDto.class
        );

        response.setLoanId(savedRepayment.getLoan().getId());

        return response;
    }

    // =========================================================
    // GET ALL
    // =========================================================

    @Override
    public List<RepaymentDto> getAll() {

        Long currentCmrcId = getCurrentCmrcId();

        return repaymentRepository.findAll()
                .stream()
                .filter(repayment ->
                        repayment.getLoan() != null
                                && repayment.getLoan().getWoman() != null
                                && repayment.getLoan().getWoman().getGroup() != null
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
                .toList();
    }

    // =========================================================
    // GET BY ID
    // =========================================================

    @Override
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
    public RepaymentDto update(
            Long id,
            RepaymentDto repaymentDto) {

        Repayment repayment = getAuthorizedRepayment(id);

        Loan loan = getAuthorizedLoan(repaymentDto.getLoanId());

        repayment.setLoan(loan);
        repayment.setRepaymentDate(repaymentDto.getRepaymentDate());
        repayment.setPaidAmount(repaymentDto.getPaidAmount());

        calculateRepayment(repayment, loan);

        repayment.setRegularRepayment(
                repaymentDto.getRegularRepayment()
        );

        repayment.setPenaltyAmount(
                repaymentDto.getPenaltyAmount()
        );

        repayment.setRemark(repaymentDto.getRemark());

        Repayment updatedRepayment =
                repaymentRepository.save(repayment);

        RepaymentDto response = modelMapper.map(
                updatedRepayment,
                RepaymentDto.class
        );

        response.setLoanId(updatedRepayment.getLoan().getId());

        return response;
    }

    // =========================================================
    // DELETE
    // =========================================================

    @Override
    public void delete(Long id) {

        Repayment repayment = getAuthorizedRepayment(id);

        Long loanId = repayment.getLoan().getId();

        repaymentRepository.delete(repayment);

        updateLoanStatus(loanId);
    }

    // =========================================================
    // REPAYMENT SUMMARY
    // =========================================================

    @Override
    public RepaymentSummaryDto getSummary(Long loanId) {

        Loan loan = getAuthorizedLoan(loanId);

        List<Repayment> repayments =
                repaymentRepository.findByLoanId(loanId);

        double totalPrincipal = repayments.stream()
                .mapToDouble(r ->
                        r.getPrincipalAmount() != null
                                ? r.getPrincipalAmount()
                                : 0.0
                )
                .sum();

        double totalInterest = repayments.stream()
                .mapToDouble(r ->
                        r.getInterestAmount() != null
                                ? r.getInterestAmount()
                                : 0.0
                )
                .sum();

        double totalRepayment = repayments.stream()
                .mapToDouble(r ->
                        r.getTotalAmount() != null
                                ? r.getTotalAmount()
                                : 0.0
                )
                .sum();

        double totalPenalty = repayments.stream()
                .mapToDouble(r ->
                        r.getPenaltyAmount() != null
                                ? r.getPenaltyAmount()
                                : 0.0
                )
                .sum();

        double outstandingAmount = Math.max(
                (loan.getLoanAmount() != null
                        ? loan.getLoanAmount()
                        : 0.0) - totalPrincipal,
                0.0
        );

        RepaymentSummaryDto summary = new RepaymentSummaryDto();

        summary.setLoanId(loanId);
        summary.setLoanAmount(loan.getLoanAmount());
        summary.setTotalPrincipalPaid(totalPrincipal);
        summary.setTotalInterestPaid(totalInterest);
        summary.setTotalRepayment(totalRepayment);
        summary.setOutstandingAmount(outstandingAmount);
        summary.setTotalPenalty(totalPenalty);

        return summary;
    }

    // =========================================================
    // GET BY LOAN ID
    // =========================================================

    @Override
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
                .toList();
    }

    // =========================================================
    // GENERATE REPAYMENT SCHEDULE
    // Existing records are never deleted or regenerated.
    // =========================================================

    @Override
    @Transactional
    public List<RepaymentDto> generateSchedule(Long loanId) {

        Loan loan = getAuthorizedLoan(loanId);

        List<Repayment> existingRepayments =
                repaymentRepository.findByLoanId(loanId);

        if (existingRepayments != null
                && !existingRepayments.isEmpty()) {

            return existingRepayments.stream()
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

                        if (repayment.getLoan() != null) {
                            dto.setLoanId(
                                    repayment.getLoan().getId()
                            );
                        }

                        return dto;
                    })
                    .collect(Collectors.toList());
        }

        if (loan.getLoanAmount() == null
                || loan.getLoanAmount() <= 0) {
            throw new RuntimeException("Loan amount is invalid");
        }

        if (loan.getInterestRate() == null
                || loan.getInterestRate() < 0) {
            throw new RuntimeException("Interest rate is invalid");
        }

        if (loan.getMonthlyEmi() == null
                || loan.getMonthlyEmi() <= 0) {
            throw new RuntimeException("Monthly EMI is invalid");
        }

        if (loan.getRepaymentPeriodMonths() == null
                || loan.getRepaymentPeriodMonths() <= 0) {
            throw new RuntimeException(
                    "Repayment period is invalid"
            );
        }

        if (loan.getLoanGivenDate() == null) {
            throw new RuntimeException(
                    "Loan given date is required"
            );
        }

        double outstandingPrincipal = loan.getLoanAmount();

        double monthlyRate =
                loan.getInterestRate() / 12.0 / 100.0;

        double emi = loan.getMonthlyEmi();

        int totalInstallments =
                loan.getRepaymentPeriodMonths();

        LocalDate firstInstallmentDate =
                loan.getLoanGivenDate().plusMonths(1);

        List<Repayment> repaymentList = new ArrayList<>();

        for (int i = 1; i <= totalInstallments; i++) {

            Repayment repayment = new Repayment();

            repayment.setLoan(loan);
            repayment.setInstallmentNo(i);

            repayment.setInstallmentDate(
                    firstInstallmentDate.plusMonths(i - 1)
            );

            double interestAmount =
                    outstandingPrincipal * monthlyRate;

            double principalAmount =
                    Math.max(0.0, emi - interestAmount);

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

            repaymentList.add(repayment);

            outstandingPrincipal = Math.max(
                    0.0,
                    outstandingPrincipal - principalAmount
            );
        }

        List<Repayment> savedList =
                repaymentRepository.saveAll(repaymentList);

        return savedList.stream()
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

        List<Repayment> previousRepayments =
                repaymentRepository.findByLoanId(loan.getId());

        double paidPrincipal = previousRepayments.stream()
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
                        r.getPrincipalAmount() != null
                                ? Math.max(r.getPrincipalAmount(), 0.0)
                                : 0.0
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
    // PAY EMI
    // Updates VO / ALF receivedFund only.
    // CMRC totalFund remains unchanged.
    // =========================================================

    @Override
    @Transactional
    public RepaymentDto payEmi(
            Long repaymentId,
            Double paidAmount,
            Double penaltyAmount,
            Boolean regularRepayment) {

        Repayment repayment =
                getAuthorizedRepayment(repaymentId);

        double payment =
                paidAmount != null ? paidAmount : 0.0;

        double penalty =
                penaltyAmount != null ? penaltyAmount : 0.0;

        double scheduled =
                repayment.getScheduledAmount() != null
                        ? repayment.getScheduledAmount()
                        : 0.0;

        double alreadyPaid =
                repayment.getPaidAmount() != null
                        ? repayment.getPaidAmount()
                        : 0.0;

        double existingPenalty =
                repayment.getPenaltyAmount() != null
                        ? repayment.getPenaltyAmount()
                        : 0.0;

        if (payment <= 0) {
            throw new RuntimeException(
                    "Payment amount must be greater than zero"
            );
        }

        if (penalty < 0) {
            throw new RuntimeException(
                    "Penalty amount cannot be negative"
            );
        }

        if (scheduled <= 0) {
            throw new RuntimeException(
                    "Scheduled EMI amount is invalid"
            );
        }

        if ("PAID".equalsIgnoreCase(
                repayment.getPaymentStatus())) {
            throw new RuntimeException(
                    "This EMI is already fully paid. Use Edit EMI instead."
            );
        }

        double remaining = round(
                Math.max(0.0, scheduled - alreadyPaid)
        );

        if (payment > remaining + 0.001) {
            throw new RuntimeException(
                    "Payment exceeds remaining EMI amount: " + remaining
            );
        }

        double totalPaid = round(alreadyPaid + payment);

        if (totalPaid > scheduled
                && totalPaid - scheduled <= 0.001) {
            totalPaid = scheduled;
        }

        if (totalPaid > scheduled) {
            throw new RuntimeException(
                    "Total paid amount cannot exceed scheduled EMI"
            );
        }

        repayment.setPaidAmount(totalPaid);

        repayment.setPenaltyAmount(
                penaltyAmount != null
                        ? penalty
                        : existingPenalty
        );

        repayment.setRegularRepayment(
                regularRepayment != null
                        ? regularRepayment
                        : false
        );

        if (totalPaid <= 0) {
            repayment.setPaymentStatus("PENDING");
        } else if (totalPaid + 0.001 >= scheduled) {
            repayment.setPaymentStatus("PAID");
        } else {
            repayment.setPaymentStatus("PARTIAL");
        }

        repayment.setRepaymentDate(LocalDate.now());

        calculateRepayment(repayment, repayment.getLoan());

        double finalPenalty =
                repayment.getPenaltyAmount() != null
                        ? repayment.getPenaltyAmount()
                        : 0.0;

        repayment.setTotalAmount(
                round(totalPaid + finalPenalty)
        );

        Repayment saved = repaymentRepository.save(repayment);

        // Only VO / ALF receivedFund is increased.
        // CMRC totalFund is not updated.
        updateVoAlfReceivedFund(saved.getLoan(), payment);

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
    // Adjusts VO / ALF receivedFund by the difference only.
    // CMRC totalFund remains unchanged.
    // =========================================================

    @Override
    @Transactional
    public RepaymentDto editPaidEmi(
            Long repaymentId,
            Double paidAmount,
            Double penaltyAmount) {

        Repayment repayment =
                getAuthorizedRepayment(repaymentId);

        double paid =
                paidAmount != null ? paidAmount : 0.0;

        double penalty =
                penaltyAmount != null ? penaltyAmount : 0.0;

        double scheduled =
                repayment.getScheduledAmount() != null
                        ? repayment.getScheduledAmount()
                        : 0.0;

        double previousPaid =
                repayment.getPaidAmount() != null
                        ? repayment.getPaidAmount()
                        : 0.0;

        if (paid < 0) {
            throw new RuntimeException(
                    "Paid amount cannot be negative"
            );
        }

        if (penalty < 0) {
            throw new RuntimeException(
                    "Penalty amount cannot be negative"
            );
        }

        if (paid > scheduled + 0.001) {
            throw new RuntimeException(
                    "Paid amount cannot exceed scheduled EMI"
            );
        }

        if (paid > scheduled) {
            paid = scheduled;
        }

        repayment.setPaidAmount(paid);
        repayment.setPenaltyAmount(penalty);

        if (paid <= 0) {
            repayment.setPaymentStatus("PENDING");
        } else if (paid + 0.001 >= scheduled) {
            repayment.setPaymentStatus("PAID");
        } else {
            repayment.setPaymentStatus("PARTIAL");
        }

        if (paid > 0) {
            repayment.setRepaymentDate(LocalDate.now());
        } else {
            repayment.setRepaymentDate(null);
        }

        calculateRepayment(
                repayment,
                repayment.getLoan()
        );

        repayment.setTotalAmount(round(paid + penalty));

        Repayment saved = repaymentRepository.save(repayment);

        // Change VO / ALF receivedFund by the difference only.
        double amountDifference = round(paid - previousPaid);

        if (amountDifference != 0) {
            updateVoAlfReceivedFund(
                    saved.getLoan(),
                    amountDifference
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
    // UPDATE LOAN STATUS
    // =========================================================

    private void updateLoanStatus(Long loanId) {

        Loan loan = getAuthorizedLoan(loanId);

        List<Repayment> repayments =
                repaymentRepository.findByLoanId(loanId);

        Integer totalInstallments =
                loan.getRepaymentPeriodMonths();

        if (totalInstallments == null
                || totalInstallments <= 0) {

            loan.setLoanStatus("ACTIVE");
            loanRepository.save(loan);
            return;
        }

        long paidInstallments = repayments.stream()
                .filter(r ->
                        "PAID".equalsIgnoreCase(
                                r.getPaymentStatus()
                        )
                )
                .count();

        if (paidInstallments >= totalInstallments) {
            loan.setLoanStatus("CLOSED");
        } else {
            loan.setLoanStatus("ACTIVE");
        }

        loanRepository.save(loan);
    }

    // =========================================================
    // ROUND TO TWO DECIMAL PLACES
    // =========================================================

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
