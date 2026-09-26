package com.example.loan_management.service;

import com.example.loan_management.dto.LoanDto;
import com.example.loan_management.entity.Loan;
import com.example.loan_management.entity.Repayment;
import com.example.loan_management.entity.VoAlf;
import com.example.loan_management.repository.LoanRepository;
import com.example.loan_management.repository.RepaymentRepository;
import com.example.loan_management.repository.VoAlfRepository;
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
    private final ModelMapper modelMapper;

    @Override
    public LoanDto create(LoanDto loanDto) {

        VoAlf voAlf = voAlfRepository.findById(loanDto.getVoAlfId())
                .orElseThrow(() -> new RuntimeException("VO/ALF not found"));

        Loan loan = modelMapper.map(loanDto, Loan.class);

        loan.setVoAlf(voAlf);

        // New loan is ACTIVE
        loan.setLoanStatus("ACTIVE");

        Loan savedLoan = loanRepository.save(loan);

        LoanDto response = modelMapper.map(savedLoan, LoanDto.class);

        response.setVoAlfId(savedLoan.getVoAlf().getId());

        response.setLoanStatus(savedLoan.getLoanStatus());

        response.setTotalInterestReceived(
                calculateTotalInterestReceived(savedLoan.getId())
        );

        return response;
    }

    @Override
    public List<LoanDto> getAll() {

        return loanRepository.findAll()
                .stream()
                .map(this::mapLoanToDto)
                .toList();
    }

    @Override
    public LoanDto getById(Long id) {

        Loan loan = loanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Loan not found"));

        return mapLoanToDto(loan);
    }

    @Override
    public LoanDto update(Long id, LoanDto loanDto) {

        Loan loan = loanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Loan not found"));

        VoAlf voAlf = voAlfRepository.findById(loanDto.getVoAlfId())
                .orElseThrow(() -> new RuntimeException("VO/ALF not found"));

        loan.setVoAlf(voAlf);

        loan.setGroupName(loanDto.getGroupName());
        loan.setWomanName(loanDto.getWomanName());
        loan.setLoanAmount(loanDto.getLoanAmount());
        loan.setLoanPurpose(loanDto.getLoanPurpose());
        loan.setLoanGivenDate(loanDto.getLoanGivenDate());
        loan.setRepaymentPeriodMonths(
                loanDto.getRepaymentPeriodMonths()
        );
        loan.setInterestRate(loanDto.getInterestRate());
        loan.setInterestType(loanDto.getInterestType());

        loan.setMonthlyEmi(
                calculateEmi(
                        loan.getLoanAmount(),
                        loan.getInterestRate(),
                        loan.getRepaymentPeriodMonths(),
                        loan.getInterestType()
                )
        );

        Loan updatedLoan = loanRepository.save(loan);

        return mapLoanToDto(updatedLoan);
    }

    @Override
    public void delete(Long id) {

        Loan loan = loanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Loan not found"));

        loanRepository.delete(loan);
    }

    @Override
    public List<LoanDto> getByVoAlfId(Long voAlfId) {

        return loanRepository.findByVoAlfId(voAlfId)
                .stream()
                .map(this::mapLoanToDto)
                .toList();
    }

    /**
     * Convert Loan entity to LoanDto
     */
    private LoanDto mapLoanToDto(Loan loan) {

        LoanDto dto = modelMapper.map(loan, LoanDto.class);

        if (loan.getVoAlf() != null) {
            dto.setVoAlfId(loan.getVoAlf().getId());
        }

        // Loan status
        dto.setLoanStatus(
                loan.getLoanStatus() != null
                        ? loan.getLoanStatus()
                        : "ACTIVE"
        );

        // Total interest received
        dto.setTotalInterestReceived(
                calculateTotalInterestReceived(loan.getId())
        );

        return dto;
    }

    /**
     * Calculate total interest received
     * from all repayments of the loan.
     */
    private BigDecimal calculateTotalInterestReceived(Long loanId) {

        if (loanId == null) {
            return BigDecimal.ZERO;
        }

        List<Repayment> repayments =
                repaymentRepository.findByLoanId(loanId);

        return repayments.stream()
                .map(repayment ->
                        repayment.getInterestAmount() != null
                                ? BigDecimal.valueOf(repayment.getInterestAmount())
                                : BigDecimal.ZERO
                )
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Calculate EMI
     */
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

        if ("FLAT".equalsIgnoreCase(interestType)) {

            double totalInterest =
                    principal * annualRate / 100 * months / 12;

            return (principal + totalInterest) / months;
        }

        // REDUCING BALANCE
        double monthlyRate =
                annualRate / 12 / 100;

        if (monthlyRate == 0) {
            return principal / months;
        }

        return principal * monthlyRate *
                Math.pow(1 + monthlyRate, months)
                /
                (Math.pow(1 + monthlyRate, months) - 1);
    }
}