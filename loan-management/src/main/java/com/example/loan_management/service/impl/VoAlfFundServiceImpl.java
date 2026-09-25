package com.example.loan_management.service.impl;

import com.example.loan_management.dto.VoAlfFundDto;
import com.example.loan_management.entity.VoAlf;
import com.example.loan_management.entity.VoAlfFund;
import com.example.loan_management.repository.VoAlfFundRepository;
import com.example.loan_management.repository.VoAlfRepository;
import com.example.loan_management.service.VoAlfFundService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VoAlfFundServiceImpl implements VoAlfFundService {

    private final VoAlfFundRepository voAlfFundRepository;
    private final VoAlfRepository voAlfRepository;
    private final ModelMapper modelMapper;

    @Override
    public VoAlfFundDto create(VoAlfFundDto dto) {

        VoAlf voAlf = voAlfRepository.findById(dto.getVoAlfId())
                .orElseThrow(() -> new RuntimeException("VO/ALF not found"));

        VoAlfFund fund = modelMapper.map(dto, VoAlfFund.class);

        fund.setVoAlf(voAlf);

        VoAlfFund saved = voAlfFundRepository.save(fund);

        VoAlfFundDto response = modelMapper.map(saved, VoAlfFundDto.class);
        response.setVoAlfId(saved.getVoAlf().getId());

        return response;
    }

    @Override
    public List<VoAlfFundDto> getAll() {

        return voAlfFundRepository.findAll()
                .stream()
                .map(fund -> {

                    VoAlfFundDto dto =
                            modelMapper.map(fund, VoAlfFundDto.class);

                    dto.setVoAlfId(fund.getVoAlf().getId());

                    return dto;
                })
                .toList();
    }

    @Override
    public VoAlfFundDto getById(Long id) {

        VoAlfFund fund = voAlfFundRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("VO/ALF Fund not found"));

        VoAlfFundDto dto =
                modelMapper.map(fund, VoAlfFundDto.class);

        dto.setVoAlfId(fund.getVoAlf().getId());

        return dto;
    }
    @Override
    public VoAlfFundDto getByVoAlfId(Long voAlfId) {

        VoAlfFund fund = voAlfFundRepository.findByVoAlfId(voAlfId)
                .orElseThrow(() ->
                        new RuntimeException("VO/ALF Fund not found"));

        VoAlfFundDto dto =
                modelMapper.map(fund, VoAlfFundDto.class);

        dto.setVoAlfId(fund.getVoAlf().getId());

        return dto;
    }

    @Override
    public VoAlfFundDto update(Long id, VoAlfFundDto dto) {

        VoAlfFund fund = voAlfFundRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("VO/ALF Fund not found"));

        VoAlf voAlf = voAlfRepository.findById(dto.getVoAlfId())
                .orElseThrow(() ->
                        new RuntimeException("VO/ALF not found"));

        fund.setVoAlf(voAlf);

        fund.setAllocationGroupCount(dto.getAllocationGroupCount());
        fund.setAllocationWomenCount(dto.getAllocationWomenCount());
        fund.setAllocationGroupAmount(dto.getAllocationGroupAmount());
        fund.setAllocationWomenAmount(dto.getAllocationWomenAmount());

        fund.setRepaymentGroupCount(dto.getRepaymentGroupCount());
        fund.setRepaymentWomenCount(dto.getRepaymentWomenCount());
        fund.setRepaymentGroupAmount(dto.getRepaymentGroupAmount());
        fund.setRepaymentWomenAmount(dto.getRepaymentWomenAmount());

        fund.setLoanGroupCount(dto.getLoanGroupCount());
        fund.setLoanWomenCount(dto.getLoanWomenCount());
        fund.setLoanGroupAmount(dto.getLoanGroupAmount());
        fund.setLoanWomenAmount(dto.getLoanWomenAmount());

        VoAlfFund updated = voAlfFundRepository.save(fund);

        VoAlfFundDto response =
                modelMapper.map(updated, VoAlfFundDto.class);

        response.setVoAlfId(updated.getVoAlf().getId());

        return response;
    }

    @Override
    public void delete(Long id) {

        VoAlfFund fund = voAlfFundRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("VO/ALF Fund not found"));

        voAlfFundRepository.delete(fund);
    }

}