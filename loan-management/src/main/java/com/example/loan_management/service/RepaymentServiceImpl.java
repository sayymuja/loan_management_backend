package com.example.loan_management.service;
import java.time.LocalDate;
import com.example.loan_management.dto.RepaymentDto;
import com.example.loan_management.dto.RepaymentSummaryDto;
import com.example.loan_management.entity.Loan;
import com.example.loan_management.entity.Repayment;
import com.example.loan_management.repository.LoanRepository;
import com.example.loan_management.repository.RepaymentRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RepaymentServiceImpl implements RepaymentService {

    private final RepaymentRepository repaymentRepository;
    private final LoanRepository loanRepository;
    private final ModelMapper modelMapper;

    @Override
    public RepaymentDto create(RepaymentDto repaymentDto) {

        Loan loan = loanRepository.findById(repaymentDto.getLoanId())
                .orElseThrow(() -> new RuntimeException("Loan not found"));

        Repayment repayment = modelMapper.map(repaymentDto, Repayment.class);

        repayment.setLoan(loan);
        calculateRepayment(repayment, loan);

        Repayment savedRepayment = repaymentRepository.save(repayment);

        RepaymentDto response =
                modelMapper.map(savedRepayment, RepaymentDto.class);

        response.setLoanId(savedRepayment.getLoan().getId());

        return response;
    }

    @Override
    public List<RepaymentDto> getAll() {

        return repaymentRepository.findAll()
                .stream()
                .map(repayment -> {

                    RepaymentDto dto =
                            modelMapper.map(repayment, RepaymentDto.class);

                    dto.setLoanId(repayment.getLoan().getId());

                    return dto;
                })
                .toList();
    }

    @Override
    public RepaymentDto getById(Long id) {

        Repayment repayment = repaymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Repayment not found"));

        RepaymentDto dto =
                modelMapper.map(repayment, RepaymentDto.class);

        dto.setLoanId(repayment.getLoan().getId());

        return dto;
    }

    @Override
    public RepaymentDto update(Long id, RepaymentDto repaymentDto) {

        Repayment repayment = repaymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Repayment not found"));

        Loan loan = loanRepository.findById(repaymentDto.getLoanId())
                .orElseThrow(() -> new RuntimeException("Loan not found"));

        repayment.setLoan(loan);
        repayment.setRepaymentDate(repaymentDto.getRepaymentDate());
        repayment.setPaidAmount(repaymentDto.getPaidAmount());

        calculateRepayment(repayment, loan);
        repayment.setRegularRepayment(repaymentDto.getRegularRepayment());
        repayment.setPenaltyAmount(repaymentDto.getPenaltyAmount());
        repayment.setRemark(repaymentDto.getRemark());

        Repayment updatedRepayment =
                repaymentRepository.save(repayment);

        RepaymentDto response =
                modelMapper.map(updatedRepayment, RepaymentDto.class);

        response.setLoanId(updatedRepayment.getLoan().getId());

        return response;
    }

    @Override
    public void delete(Long id) {

        Repayment repayment = repaymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Repayment not found"));

        Long loanId = repayment.getLoan().getId();

        repaymentRepository.delete(repayment);

        updateLoanStatus(loanId);
    }
    @Override
    public RepaymentSummaryDto getSummary(Long loanId) {

        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found"));

        List<Repayment> repayments =
                repaymentRepository.findByLoanId(loanId);

        double totalPrincipal = repayments.stream()
                .mapToDouble(r -> r.getPrincipalAmount() != null
                        ? r.getPrincipalAmount() : 0)
                .sum();

        double totalInterest = repayments.stream()
                .mapToDouble(r -> r.getInterestAmount() != null
                        ? r.getInterestAmount() : 0)
                .sum();

        double totalRepayment = repayments.stream()
                .mapToDouble(r -> r.getTotalAmount() != null
                        ? r.getTotalAmount() : 0)
                .sum();

        double totalPenalty = repayments.stream()
                .mapToDouble(r -> r.getPenaltyAmount() != null
                        ? r.getPenaltyAmount() : 0)
                .sum();

        double outstandingAmount =
                loan.getLoanAmount() - totalPrincipal;

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
    @Override
    public List<RepaymentDto> getByLoanId(Long loanId) {
        return repaymentRepository.findByLoanId(loanId)
                .stream()
                .map(repayment -> modelMapper.map(repayment, RepaymentDto.class))
                .toList();
    }
    @Override
    public List<RepaymentDto> generateSchedule(Long loanId) {

        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found"));

        // Check existing schedule
        List<Repayment> existing =
                repaymentRepository.findByLoanId(loanId);

        if (!existing.isEmpty()) {
            return existing.stream()
                    .map(repayment -> {
                        RepaymentDto dto =
                                modelMapper.map(repayment, RepaymentDto.class);
                        dto.setLoanId(repayment.getLoan().getId());
                        return dto;
                    })
                    .toList();
        }

        Integer months = loan.getRepaymentPeriodMonths();

        if (months == null || months <= 0) {
            throw new RuntimeException("Invalid repayment period");
        }

        Double emi = loan.getMonthlyEmi();

        if (emi == null || emi <= 0) {
            throw new RuntimeException("Monthly EMI not available");
        }

        LocalDate startDate = loan.getLoanGivenDate();

        if (startDate == null) {
            throw new RuntimeException("Loan given date not available");
        }

        List<RepaymentDto> result = new java.util.ArrayList<>();

        for (int i = 1; i <= months; i++) {

            Repayment repayment = new Repayment();

            repayment.setLoan(loan);
            repayment.setInstallmentNo(i);

            repayment.setInstallmentDate(
                    startDate.plusMonths(i)
            );

            repayment.setScheduledAmount(emi);

            repayment.setPaidAmount(0.0);
            repayment.setPrincipalAmount(0.0);
            repayment.setInterestAmount(0.0);
            repayment.setTotalAmount(0.0);

            repayment.setPaymentStatus("PENDING");
            repayment.setRegularRepayment("No");
            repayment.setPenaltyAmount(0.0);
            repayment.setRemark("");

            Repayment saved =
                    repaymentRepository.save(repayment);

            RepaymentDto dto =
                    modelMapper.map(saved, RepaymentDto.class);

            dto.setLoanId(loanId);

            result.add(dto);
        }

        return result;
    }
    private void calculateRepayment(
            Repayment repayment,
            Loan loan) {

        double paidAmount = repayment.getPaidAmount() != null
                ? repayment.getPaidAmount()
                : 0.0;

        double loanAmount = loan.getLoanAmount() != null
                ? loan.getLoanAmount()
                : 0.0;

        double annualRate = loan.getInterestRate() != null
                ? loan.getInterestRate()
                : 0.0;

        List<Repayment> previousRepayments =
                repaymentRepository.findByLoanId(loan.getId());

        double paidPrincipal = previousRepayments.stream()
                .filter(r -> r.getId() == null
                        || !r.getId().equals(repayment.getId()))
                .mapToDouble(r -> r.getPrincipalAmount() != null
                        ? Math.max(r.getPrincipalAmount(), 0.0)
                        : 0.0)
                .sum();

        double outstandingPrincipal =
                Math.max(loanAmount - paidPrincipal, 0.0);

        double monthlyInterest =
                outstandingPrincipal * annualRate / 12 / 100;

        /*
         * Interest cannot be negative
         */
        double interest =
                Math.max(0.0, Math.min(paidAmount, monthlyInterest));

        /*
         * Principal cannot be negative
         */
        double principalPaid =
                Math.max(0.0, paidAmount - interest);

        repayment.setInterestAmount(interest);
        repayment.setPrincipalAmount(principalPaid);
    }
    @Override
    public RepaymentDto payEmi(
            Long repaymentId,
            Double paidAmount,
            Double penaltyAmount) {

        Repayment repayment = repaymentRepository.findById(repaymentId)
                .orElseThrow(() -> new RuntimeException("Repayment not found"));

        // Previous paid amount
        double previousPaidAmount =
                repayment.getPaidAmount() != null
                        ? repayment.getPaidAmount()
                        : 0.0;

        // New payment
        double newPaidAmount =
                paidAmount != null
                        ? paidAmount
                        : 0.0;

        // Add previous + new payment
        double totalPaidAmount =
                previousPaidAmount + newPaidAmount;

        // Scheduled EMI
        double scheduledAmount =
                repayment.getScheduledAmount() != null
                        ? repayment.getScheduledAmount()
                        : 0.0;

        // Penalty
        double newPenaltyAmount =
                penaltyAmount != null
                        ? penaltyAmount
                        : 0.0;

        // Update paid amount
        repayment.setPaidAmount(totalPaidAmount);

        // Update penalty
        repayment.setPenaltyAmount(newPenaltyAmount);

        // Payment status
        if (totalPaidAmount >= scheduledAmount) {
            repayment.setPaymentStatus("PAID");
        } else if (totalPaidAmount > 0) {
            repayment.setPaymentStatus("PARTIAL");
        } else {
            repayment.setPaymentStatus("PENDING");
        }

        repayment.setRepaymentDate(LocalDate.now());

        // Calculate principal + interest
        calculateRepayment(
                repayment,
                repayment.getLoan()
        );

        // Total collection = paid EMI + penalty
        repayment.setTotalAmount(
                totalPaidAmount + newPenaltyAmount
        );

        Repayment saved =
                repaymentRepository.save(repayment);

        updateLoanStatus(saved.getLoan().getId());

        RepaymentDto response =
                modelMapper.map(saved, RepaymentDto.class);

        response.setLoanId(saved.getLoan().getId());

        return response;
    }
    @Override
    public RepaymentDto editPaidEmi(
            Long repaymentId,
            Double paidAmount,
            Double penaltyAmount) {

        Repayment repayment = repaymentRepository.findById(repaymentId)
                .orElseThrow(() -> new RuntimeException("Repayment not found"));

        // Edited Paid Amount
        double updatedPaidAmount =
                paidAmount != null
                        ? paidAmount
                        : 0.0;

        // Edited Penalty Amount
        double updatedPenaltyAmount =
                penaltyAmount != null
                        ? penaltyAmount
                        : 0.0;

        // Scheduled EMI
        double scheduledAmount =
                repayment.getScheduledAmount() != null
                        ? repayment.getScheduledAmount()
                        : 0.0;

        // Replace existing values
        repayment.setPaidAmount(updatedPaidAmount);
        repayment.setPenaltyAmount(updatedPenaltyAmount);

        // Recalculate payment status
        if (updatedPaidAmount >= scheduledAmount) {
            repayment.setPaymentStatus("PAID");
        } else if (updatedPaidAmount > 0) {
            repayment.setPaymentStatus("PARTIAL");
        } else {
            repayment.setPaymentStatus("PENDING");
        }

        repayment.setRepaymentDate(LocalDate.now());

        // Recalculate principal + interest
        calculateRepayment(
                repayment,
                repayment.getLoan()
        );

        // Paid amount + penalty
        repayment.setTotalAmount(
                updatedPaidAmount + updatedPenaltyAmount
        );

        Repayment saved =
                repaymentRepository.save(repayment);

        updateLoanStatus(saved.getLoan().getId());

        RepaymentDto response =
                modelMapper.map(saved, RepaymentDto.class);

        response.setLoanId(saved.getLoan().getId());

        return response;
    }
    private void updateLoanStatus(Long loanId) {

        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found"));

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
                .filter(r -> "PAID".equalsIgnoreCase(r.getPaymentStatus()))
                .count();

        if (paidInstallments >= totalInstallments) {
            loan.setLoanStatus("CLOSED");
        } else {
            loan.setLoanStatus("ACTIVE");
        }

        loanRepository.save(loan);
    }

}