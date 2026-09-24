package com.example.loan_management.service.impl;

import com.example.loan_management.dto.VoAlfBankBalanceDto;
import com.example.loan_management.entity.VoAlf;
import com.example.loan_management.entity.VoAlfBankBalance;
import com.example.loan_management.repository.VoAlfBankBalanceRepository;
import com.example.loan_management.repository.VoAlfRepository;
import com.example.loan_management.service.VoAlfBankBalanceService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VoAlfBankBalanceServiceImpl
        implements VoAlfBankBalanceService {

    private final VoAlfBankBalanceRepository bankBalanceRepository;
    private final VoAlfRepository voAlfRepository;
    private final ModelMapper modelMapper;

    @Override
    public VoAlfBankBalanceDto create(VoAlfBankBalanceDto dto) {

        VoAlf voAlf = voAlfRepository.findById(dto.getVoAlfId())
                .orElseThrow(() ->
                        new RuntimeException("VO/ALF not found"));

        VoAlfBankBalance balance =
                modelMapper.map(dto, VoAlfBankBalance.class);

        balance.setVoAlf(voAlf);

        VoAlfBankBalance saved =
                bankBalanceRepository.save(balance);

        VoAlfBankBalanceDto response =
                modelMapper.map(saved, VoAlfBankBalanceDto.class);

        response.setVoAlfId(saved.getVoAlf().getId());

        return response;
    }

    @Override
    public List<VoAlfBankBalanceDto> getAll() {

        return bankBalanceRepository.findAll()
                .stream()
                .map(balance -> {

                    VoAlfBankBalanceDto dto =
                            modelMapper.map(
                                    balance,
                                    VoAlfBankBalanceDto.class
                            );

                    dto.setVoAlfId(
                            balance.getVoAlf().getId()
                    );

                    return dto;

                })
                .toList();
    }

    @Override
    public VoAlfBankBalanceDto getById(Long id) {

        VoAlfBankBalance balance =
                bankBalanceRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Bank balance not found"
                                ));

        VoAlfBankBalanceDto dto =
                modelMapper.map(
                        balance,
                        VoAlfBankBalanceDto.class
                );

        dto.setVoAlfId(
                balance.getVoAlf().getId()
        );

        return dto;
    }

    @Override
    public List<VoAlfBankBalanceDto> getByVoAlfId(Long voAlfId) {

        return bankBalanceRepository
                .findByVoAlfId(voAlfId)
                .stream()
                .map(balance -> {

                    VoAlfBankBalanceDto dto =
                            modelMapper.map(
                                    balance,
                                    VoAlfBankBalanceDto.class
                            );

                    dto.setVoAlfId(
                            balance.getVoAlf().getId()
                    );

                    return dto;

                })
                .toList();
    }

    @Override
    public VoAlfBankBalanceDto update(
            Long id,
            VoAlfBankBalanceDto dto) {

        VoAlfBankBalance balance =
                bankBalanceRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Bank balance not found"
                                ));

        VoAlf voAlf =
                voAlfRepository.findById(dto.getVoAlfId())
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "VO/ALF not found"
                                ));

        balance.setVoAlf(voAlf);
        balance.setBalanceMonth(dto.getBalanceMonth());
        balance.setBalanceAmount(dto.getBalanceAmount());

        VoAlfBankBalance updated =
                bankBalanceRepository.save(balance);

        VoAlfBankBalanceDto response =
                modelMapper.map(
                        updated,
                        VoAlfBankBalanceDto.class
                );

        response.setVoAlfId(
                updated.getVoAlf().getId()
        );

        return response;
    }

    @Override
    public void delete(Long id) {

        VoAlfBankBalance balance =
                bankBalanceRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Bank balance not found"
                                ));

        bankBalanceRepository.delete(balance);
    }
    @Override
    public List<VoAlfBankBalanceDto> createBulk(
            List<VoAlfBankBalanceDto> dtoList) {

        return dtoList.stream()
                .map(this::create)
                .toList();
    }
}