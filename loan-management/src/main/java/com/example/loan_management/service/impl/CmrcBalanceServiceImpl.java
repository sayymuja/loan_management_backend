package com.example.loan_management.service.impl;

import com.example.loan_management.dto.CmrcBalanceDto;
import com.example.loan_management.entity.Cmrc;
import com.example.loan_management.entity.CmrcBalance;
import com.example.loan_management.repository.CmrcBalanceRepository;
import com.example.loan_management.repository.CmrcRepository;
import com.example.loan_management.service.CmrcBalanceService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CmrcBalanceServiceImpl implements CmrcBalanceService {

    private final CmrcBalanceRepository cmrcBalanceRepository;
    private final CmrcRepository cmrcRepository;
    private final ModelMapper modelMapper;

    @Override
    public CmrcBalanceDto create(CmrcBalanceDto dto) {

        Cmrc cmrc = cmrcRepository.findById(dto.getCmrcId())
                .orElseThrow(() ->
                        new RuntimeException("CMRC not found"));

        CmrcBalance balance =
                modelMapper.map(dto, CmrcBalance.class);

        balance.setCmrc(cmrc);

        CmrcBalance saved =
                cmrcBalanceRepository.save(balance);

        CmrcBalanceDto response =
                modelMapper.map(saved, CmrcBalanceDto.class);

        response.setCmrcId(saved.getCmrc().getId());

        return response;
    }

    @Override
    public List<CmrcBalanceDto> getAll() {

        return cmrcBalanceRepository.findAll()
                .stream()
                .map(balance -> {

                    CmrcBalanceDto dto =
                            modelMapper.map(
                                    balance,
                                    CmrcBalanceDto.class
                            );

                    dto.setCmrcId(
                            balance.getCmrc().getId()
                    );

                    return dto;
                })
                .toList();
    }

    @Override
    public CmrcBalanceDto getById(Long id) {

        CmrcBalance balance =
                cmrcBalanceRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "CMRC balance not found"
                                ));

        CmrcBalanceDto dto =
                modelMapper.map(
                        balance,
                        CmrcBalanceDto.class
                );

        dto.setCmrcId(balance.getCmrc().getId());

        return dto;
    }

    @Override
    public List<CmrcBalanceDto> getByCmrcId(Long cmrcId) {

        return cmrcBalanceRepository
                .findByCmrcId(cmrcId)
                .stream()
                .map(balance -> {

                    CmrcBalanceDto dto =
                            modelMapper.map(
                                    balance,
                                    CmrcBalanceDto.class
                            );

                    dto.setCmrcId(
                            balance.getCmrc().getId()
                    );

                    return dto;
                })
                .toList();
    }

    @Override
    public CmrcBalanceDto update(
            Long id,
            CmrcBalanceDto dto) {

        CmrcBalance balance =
                cmrcBalanceRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "CMRC balance not found"
                                ));

        Cmrc cmrc =
                cmrcRepository.findById(dto.getCmrcId())
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "CMRC not found"
                                ));

        balance.setCmrc(cmrc);
        balance.setBalanceDate(dto.getBalanceDate());
        balance.setBalanceAmount(dto.getBalanceAmount());

        CmrcBalance updated =
                cmrcBalanceRepository.save(balance);

        CmrcBalanceDto response =
                modelMapper.map(
                        updated,
                        CmrcBalanceDto.class
                );

        response.setCmrcId(
                updated.getCmrc().getId()
        );

        return response;
    }

    @Override
    public void delete(Long id) {

        CmrcBalance balance =
                cmrcBalanceRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "CMRC balance not found"
                                ));

        cmrcBalanceRepository.delete(balance);
    }
}