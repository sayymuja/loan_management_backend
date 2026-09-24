package com.example.loan_management.service.impl;

import com.example.loan_management.dto.InterestDto;
import com.example.loan_management.entity.Cmrc;
import com.example.loan_management.entity.Interest;
import com.example.loan_management.repository.CmrcRepository;
import com.example.loan_management.repository.InterestRepository;
import com.example.loan_management.service.InterestService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InterestServiceImpl implements InterestService {

    private final InterestRepository interestRepository;
    private final CmrcRepository cmrcRepository;
    private final ModelMapper modelMapper;

    @Override
    public InterestDto create(InterestDto dto) {

        Cmrc cmrc = cmrcRepository.findById(dto.getCmrcId())
                .orElseThrow(() -> new RuntimeException("CMRC not found"));

        Interest interest = modelMapper.map(dto, Interest.class);
        interest.setCmrc(cmrc);

        Interest saved = interestRepository.save(interest);

        InterestDto response =
                modelMapper.map(saved, InterestDto.class);

        response.setCmrcId(saved.getCmrc().getId());

        return response;
    }

    @Override
    public List<InterestDto> getAll() {

        return interestRepository.findAll()
                .stream()
                .map(interest -> {

                    InterestDto dto =
                            modelMapper.map(interest, InterestDto.class);

                    dto.setCmrcId(interest.getCmrc().getId());

                    return dto;
                })
                .toList();
    }

    @Override
    public InterestDto getById(Long id) {

        Interest interest = interestRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Interest record not found"));

        InterestDto dto =
                modelMapper.map(interest, InterestDto.class);

        dto.setCmrcId(interest.getCmrc().getId());

        return dto;
    }

    @Override
    public List<InterestDto> getByCmrcId(Long cmrcId) {

        return interestRepository.findByCmrcId(cmrcId)
                .stream()
                .map(interest -> {

                    InterestDto dto =
                            modelMapper.map(interest, InterestDto.class);

                    dto.setCmrcId(interest.getCmrc().getId());

                    return dto;
                })
                .toList();
    }

    @Override
    public List<InterestDto> getByFinancialYear(
            String financialYear) {

        return interestRepository
                .findByFinancialYear(financialYear)
                .stream()
                .map(interest ->
                        modelMapper.map(
                                interest,
                                InterestDto.class
                        ))
                .toList();
    }

    @Override
    public InterestDto update(Long id, InterestDto dto) {

        Interest interest = interestRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Interest record not found"));

        Cmrc cmrc = cmrcRepository.findById(dto.getCmrcId())
                .orElseThrow(() ->
                        new RuntimeException("CMRC not found"));

        interest.setCmrc(cmrc);
        interest.setVillageName(dto.getVillageName());
        interest.setVoAlfReceivedAmount(
                dto.getVoAlfReceivedAmount()
        );
        interest.setFinancialYear(dto.getFinancialYear());
        interest.setTotalNilWomen(dto.getTotalNilWomen());
        interest.setTotalInterestAmount(dto.getTotalInterestAmount());

        Interest updated =
                interestRepository.save(interest);

        InterestDto response =
                modelMapper.map(updated, InterestDto.class);

        response.setCmrcId(updated.getCmrc().getId());

        return response;
    }

    @Override
    public void delete(Long id) {

        Interest interest = interestRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Interest record not found"));

        interestRepository.delete(interest);
    }
}