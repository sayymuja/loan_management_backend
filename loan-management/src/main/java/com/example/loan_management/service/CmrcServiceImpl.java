package com.example.loan_management.service;

import com.example.loan_management.dto.CmrcDto;
import com.example.loan_management.entity.Cmrc;
import com.example.loan_management.repository.CmrcRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CmrcServiceImpl implements CmrcService {

    private final CmrcRepository cmrcRepository;
    private final ModelMapper modelMapper;

    @Override
    public CmrcDto create(CmrcDto cmrcDto) {

        Cmrc cmrc = modelMapper.map(cmrcDto, Cmrc.class);

        Cmrc savedCmrc = cmrcRepository.save(cmrc);

        return modelMapper.map(savedCmrc, CmrcDto.class);
    }

    @Override
    public List<CmrcDto> getAll() {

        return cmrcRepository.findAll()
                .stream()
                .map(cmrc -> modelMapper.map(cmrc, CmrcDto.class))
                .toList();
    }

    @Override
    public CmrcDto getById(Long id) {

        Cmrc cmrc = cmrcRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("CMRC not found with id: " + id));

        return modelMapper.map(cmrc, CmrcDto.class);
    }

    @Override
    public CmrcDto update(Long id, CmrcDto cmrcDto) {

        Cmrc cmrc = cmrcRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("CMRC not found with id: " + id));

        cmrc.setSerialNo(cmrcDto.getSerialNo());
        cmrc.setCmrcName(cmrcDto.getCmrcName());
        cmrc.setAccountNo(cmrcDto.getAccountNo());
        cmrc.setAccountOpeningDate(cmrcDto.getAccountOpeningDate());
        cmrc.setTotalFund(cmrcDto.getTotalFund());
        cmrc.setServiceFeeReceived(cmrcDto.getServiceFeeReceived());
        cmrc.setRecordsPrinted(cmrcDto.getRecordsPrinted());
        cmrc.setPrintedRecordsAmount(cmrcDto.getPrintedRecordsAmount());
        cmrc.setRecordsDistributedVillages(
                cmrcDto.getRecordsDistributedVillages()
        );
        cmrc.setExpectedRecordAmount(
                cmrcDto.getExpectedRecordAmount()
        );
        cmrc.setActualRecordAmountReceived(
                cmrcDto.getActualRecordAmountReceived()
        );
        cmrc.setTezshreeFundReceivedUltraPoor(
                cmrcDto.getTezshreeFundReceivedUltraPoor()
        );
        cmrc.setTezshreeFundReceivedDebtTrappedWomen(
                cmrcDto.getTezshreeFundReceivedDebtTrappedWomen()
        );
        cmrc.setTezshreeFundReceivedTotal(
                cmrcDto.getTezshreeFundReceivedTotal()
        );
        cmrc.setFundDistributedVillageCount(
                cmrcDto.getFundDistributedVillageCount()
        );
        cmrc.setDistributedUltraPoorWomenCount(
                cmrcDto.getDistributedUltraPoorWomenCount()
        );
        cmrc.setDistributedUltraPoorFund(
                cmrcDto.getDistributedUltraPoorFund()
        );
        cmrc.setDistributedDebtTrappedWomenCount(
                cmrcDto.getDistributedDebtTrappedWomenCount()
        );
        cmrc.setDistributedDebtTrappedFund(
                cmrcDto.getDistributedDebtTrappedFund()
        );
        cmrc.setDistributedTotalFund(
                cmrcDto.getDistributedTotalFund()
        );

        Cmrc updatedCmrc = cmrcRepository.save(cmrc);

        return modelMapper.map(updatedCmrc, CmrcDto.class);
    }

    @Override
    public void delete(Long id) {

        Cmrc cmrc = cmrcRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("CMRC not found with id: " + id));

        cmrcRepository.delete(cmrc);
    }
}