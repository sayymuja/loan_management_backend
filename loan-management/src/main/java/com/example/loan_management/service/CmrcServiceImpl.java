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

    // =========================================================
    // CREATE CMRC
    // =========================================================

    @Override
    public CmrcDto create(CmrcDto cmrcDto) {

        Cmrc cmrc = modelMapper.map(cmrcDto, Cmrc.class);

        Cmrc savedCmrc = cmrcRepository.save(cmrc);

        return modelMapper.map(savedCmrc, CmrcDto.class);
    }

    // =========================================================
    // GET ALL CMRC
    // =========================================================

    @Override
    public List<CmrcDto> getAll() {

        return cmrcRepository.findAll()
                .stream()
                .map(cmrc ->
                        modelMapper.map(cmrc, CmrcDto.class)
                )
                .toList();
    }

    // =========================================================
    // GET CMRC BY ID
    // =========================================================

    @Override
    public CmrcDto getById(Long id) {

        Cmrc cmrc = cmrcRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "CMRC not found with id: " + id
                        )
                );

        return modelMapper.map(cmrc, CmrcDto.class);
    }

    // =========================================================
    // UPDATE CMRC
    // =========================================================

    @Override
    public CmrcDto update(Long id, CmrcDto cmrcDto) {

        Cmrc cmrc = cmrcRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "CMRC not found with id: " + id
                        )
                );

        // ==============================
        // BASIC INFORMATION
        // ==============================

        cmrc.setSerialNo(
                cmrcDto.getSerialNo()
        );

        cmrc.setCmrcName(
                cmrcDto.getCmrcName()
        );

        cmrc.setAccountNo(
                cmrcDto.getAccountNo()
        );

        cmrc.setAccountOpeningDate(
                cmrcDto.getAccountOpeningDate()
        );

        // ==============================
        // LOCATION
        // ==============================

        cmrc.setDistrict(
                cmrcDto.getDistrict()
        );

        cmrc.setTaluka(
                cmrcDto.getTaluka()
        );

        // ==============================
        // STATUS
        // ==============================

        cmrc.setStatus(
                cmrcDto.getStatus()
        );

        // ==============================
        // CMRC AMOUNT
        // ==============================

        cmrc.setTotalFund(
                cmrcDto.getTotalFund()
        );

        cmrc.setServiceFeeReceived(
                cmrcDto.getServiceFeeReceived()
        );

        cmrc.setRecordsPrinted(
                cmrcDto.getRecordsPrinted()
        );

        cmrc.setPrintedRecordsAmount(
                cmrcDto.getPrintedRecordsAmount()
        );

        cmrc.setRecordsDistributedVillages(
                cmrcDto.getRecordsDistributedVillages()
        );

        cmrc.setExpectedRecordAmount(
                cmrcDto.getExpectedRecordAmount()
        );

        cmrc.setActualRecordAmountReceived(
                cmrcDto.getActualRecordAmountReceived()
        );

        // ==============================
        // TEZSHREE FUND RECEIVED
        // ==============================

        cmrc.setTezshreeFundReceivedUltraPoor(
                cmrcDto.getTezshreeFundReceivedUltraPoor()
        );

        cmrc.setTezshreeFundReceivedDebtTrappedWomen(
                cmrcDto.getTezshreeFundReceivedDebtTrappedWomen()
        );

        cmrc.setTezshreeFundReceivedTotal(
                cmrcDto.getTezshreeFundReceivedTotal()
        );

        // ==============================
        // FUND DISTRIBUTION
        // ==============================

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

        // ==============================
        // SAVE
        // ==============================

        Cmrc updatedCmrc =
                cmrcRepository.save(cmrc);

        return modelMapper.map(
                updatedCmrc,
                CmrcDto.class
        );
    }

    // =========================================================
    // DELETE CMRC
    // =========================================================

    @Override
    public void delete(Long id) {

        Cmrc cmrc = cmrcRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "CMRC not found with id: " + id
                        )
                );

        cmrcRepository.delete(cmrc);
    }
}
