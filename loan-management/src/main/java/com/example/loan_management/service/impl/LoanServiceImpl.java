package com.example.loan_management.service.impl;

import com.example.loan_management.dto.LoanDto;
import com.example.loan_management.entity.Loan;
import com.example.loan_management.entity.VoAlf;
import com.example.loan_management.repository.LoanRepository;
import com.example.loan_management.repository.VoAlfRepository;
import com.example.loan_management.service.LoanService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LoanServiceImpl implements LoanService {

    private final LoanRepository loanRepository;
    private final VoAlfRepository voAlfRepository;
    private final ModelMapper modelMapper;

    @Override
    public LoanDto create(LoanDto loanDto) {

        VoAlf voAlf = voAlfRepository.findById(loanDto.getVoAlfId())
                .orElseThrow(() -> new RuntimeException("VO/ALF not found"));

        Loan loan = modelMapper.map(loanDto, Loan.class);

        loan.setVoAlf(voAlf);

        Loan savedLoan = loanRepository.save(loan);

        LoanDto response = modelMapper.map(savedLoan, LoanDto.class);

        response.setVoAlfId(savedLoan.getVoAlf().getId());

        return response;
    }

    @Override
    public List<LoanDto> getAll() {

        return loanRepository.findAll()
                .stream()
                .map(loan -> {

                    LoanDto dto = modelMapper.map(loan, LoanDto.class);

                    dto.setVoAlfId(loan.getVoAlf().getId());

                    return dto;
                })
                .toList();
    }

    @Override
    public LoanDto getById(Long id) {

        Loan loan = loanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Loan not found"));

        LoanDto dto = modelMapper.map(loan, LoanDto.class);

        dto.setVoAlfId(loan.getVoAlf().getId());

        return dto;
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
        loan.setRepaymentPeriodMonths(loanDto.getRepaymentPeriodMonths());
        loan.setInterestRate(loanDto.getInterestRate());

        Loan updatedLoan = loanRepository.save(loan);

        LoanDto response = modelMapper.map(updatedLoan, LoanDto.class);

        response.setVoAlfId(updatedLoan.getVoAlf().getId());

        return response;
    }

    @Override
    public void delete(Long id) {

        Loan loan = loanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Loan not found"));

        loanRepository.delete(loan);
    }
}