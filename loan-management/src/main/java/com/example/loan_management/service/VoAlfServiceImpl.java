package com.example.loan_management.service;

import com.example.loan_management.dto.VoAlfDto;
import com.example.loan_management.entity.Cmrc;
import com.example.loan_management.entity.VoAlf;
import com.example.loan_management.repository.CmrcRepository;
import com.example.loan_management.repository.VoAlfRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VoAlfServiceImpl implements VoAlfService {

    private final VoAlfRepository voAlfRepository;
    private final CmrcRepository cmrcRepository;
    private final ModelMapper modelMapper;


    // ==========================================
    // CREATE
    // ==========================================

    @Override
    public VoAlfDto create(VoAlfDto voAlfDto) {

        Cmrc cmrc = cmrcRepository.findById(voAlfDto.getCmrcId())
                .orElseThrow(() -> new RuntimeException("CMRC not found"));

        VoAlf voAlf = modelMapper.map(voAlfDto, VoAlf.class);

        // Set CMRC relationship
        voAlf.setCmrc(cmrc);

        // createdDate automatically set by @PrePersist
        VoAlf savedVoAlf = voAlfRepository.save(voAlf);

        // Convert Entity -> DTO
        VoAlfDto response =
                modelMapper.map(savedVoAlf, VoAlfDto.class);

        // Set CMRC ID
        response.setCmrcId(
                savedVoAlf.getCmrc().getId()
        );

        return response;
    }


    // ==========================================
    // GET ALL
    // ==========================================

    @Override
    public List<VoAlfDto> getAll() {

        return voAlfRepository.findAll()
                .stream()
                .map(voAlf -> {

                    VoAlfDto dto =
                            modelMapper.map(
                                    voAlf,
                                    VoAlfDto.class
                            );

                    dto.setCmrcId(
                            voAlf.getCmrc().getId()
                    );

                    return dto;

                })
                .toList();
    }


    // ==========================================
    // GET BY ID
    // ==========================================

    @Override
    public VoAlfDto getById(Long id) {

        VoAlf voAlf =
                voAlfRepository.findById(id)
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "VO/ALF not found"
                                )
                        );

        VoAlfDto dto =
                modelMapper.map(
                        voAlf,
                        VoAlfDto.class
                );

        dto.setCmrcId(
                voAlf.getCmrc().getId()
        );

        return dto;
    }


    // ==========================================
    // GET BY CMRC ID
    // ==========================================

    @Override
    public List<VoAlfDto> getByCmrcId(Long cmrcId) {

        List<VoAlf> voAlfList =
                voAlfRepository.findByCmrcId(cmrcId);

        return voAlfList
                .stream()
                .map(voAlf -> {

                    VoAlfDto dto =
                            modelMapper.map(
                                    voAlf,
                                    VoAlfDto.class
                            );

                    dto.setCmrcId(
                            voAlf.getCmrc().getId()
                    );

                    return dto;

                })
                .toList();
    }


    // ==========================================
    // UPDATE
    // ==========================================

    @Override
    public VoAlfDto update(
            Long id,
            VoAlfDto voAlfDto
    ) {

        VoAlf voAlf =
                voAlfRepository.findById(id)
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "VO/ALF not found"
                                )
                        );

        Cmrc cmrc =
                cmrcRepository.findById(
                                voAlfDto.getCmrcId()
                        )
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "CMRC not found"
                                )
                        );


        // ==========================================
        // Update editable fields
        // ==========================================

        voAlf.setCmrc(cmrc);

        voAlf.setVillageName(
                voAlfDto.getVillageName()
        );

        voAlf.setVoAlfName(
                voAlfDto.getVoAlfName()
        );

        voAlf.setAccountNo(
                voAlfDto.getAccountNo()
        );

        voAlf.setReceivedFund(
                voAlfDto.getReceivedFund()
        );


        // ==========================================
        // IMPORTANT
        // ==========================================
        // serialNo removed
        // createdDate NOT updated
        //
        // createdDate should remain the
        // original creation date.
        // ==========================================


        VoAlf updated =
                voAlfRepository.save(voAlf);


        // Entity -> DTO

        VoAlfDto response =
                modelMapper.map(
                        updated,
                        VoAlfDto.class
                );

        response.setCmrcId(
                updated.getCmrc().getId()
        );

        return response;
    }


    // ==========================================
    // DELETE
    // ==========================================

    @Override
    public void delete(Long id) {

        VoAlf voAlf =
                voAlfRepository.findById(id)
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "VO/ALF not found"
                                )
                        );

        voAlfRepository.delete(voAlf);
    }
}