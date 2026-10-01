package com.example.loan_management.service;

import com.example.loan_management.auth.service.CurrentUserService;
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
    private final CurrentUserService currentUserService;


    // =========================================================
    // CURRENT LOGGED-IN USER CMRC
    // =========================================================

    private Cmrc getCurrentCmrc() {

        Long currentCmrcId =
                currentUserService.getCurrentCmrcId();

        return cmrcRepository.findById(currentCmrcId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "CMRC not found for logged-in user"
                        )
                );
    }


    // =========================================================
    // CHECK VO/ALF BELONGS TO CURRENT CMRC
    // =========================================================

    private VoAlf getAuthorizedVoAlf(Long id) {

        VoAlf voAlf =
                voAlfRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "VO/ALF not found"
                                )
                        );

        Long currentCmrcId =
                currentUserService.getCurrentCmrcId();

        if (voAlf.getCmrc() == null ||
                !currentCmrcId.equals(
                        voAlf.getCmrc().getId()
                )) {

            throw new RuntimeException(
                    "Access denied for this VO/ALF"
            );
        }

        return voAlf;
    }


    // =========================================================
    // CREATE
    // =========================================================

    @Override
    public VoAlfDto create(VoAlfDto voAlfDto) {

        /*
         * NEVER trust cmrcId coming from frontend.
         *
         * The logged-in user's CMRC is automatically used.
         */

        Cmrc currentCmrc = getCurrentCmrc();

        VoAlf voAlf =
                modelMapper.map(
                        voAlfDto,
                        VoAlf.class
                );

        // Force logged-in user's CMRC
        voAlf.setCmrc(currentCmrc);

        VoAlf savedVoAlf =
                voAlfRepository.save(voAlf);

        VoAlfDto response =
                modelMapper.map(
                        savedVoAlf,
                        VoAlfDto.class
                );

        response.setCmrcId(
                savedVoAlf.getCmrc().getId()
        );

        return response;
    }


    // =========================================================
    // GET ALL
    // =========================================================

    @Override
    public List<VoAlfDto> getAll() {

        Long currentCmrcId =
                currentUserService.getCurrentCmrcId();

        return voAlfRepository
                .findByCmrcId(currentCmrcId)
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


    // =========================================================
    // GET BY ID
    // =========================================================

    @Override
    public VoAlfDto getById(Long id) {

        VoAlf voAlf =
                getAuthorizedVoAlf(id);

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


    // =========================================================
    // GET BY CMRC ID
    // =========================================================

    @Override
    public List<VoAlfDto> getByCmrcId(Long cmrcId) {

        Long currentCmrcId =
                currentUserService.getCurrentCmrcId();

        /*
         * Do not allow frontend to request another CMRC.
         */

        if (!currentCmrcId.equals(cmrcId)) {

            throw new RuntimeException(
                    "Access denied for this CMRC"
            );
        }

        return voAlfRepository
                .findByCmrcId(currentCmrcId)
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


    // =========================================================
    // UPDATE
    // =========================================================

    @Override
    public VoAlfDto update(
            Long id,
            VoAlfDto voAlfDto
    ) {

        /*
         * First verify that this VO/ALF belongs
         * to logged-in user's CMRC.
         */

        VoAlf voAlf =
                getAuthorizedVoAlf(id);

        Cmrc currentCmrc =
                getCurrentCmrc();

        /*
         * IMPORTANT:
         *
         * Do NOT allow frontend to move VO/ALF
         * to another CMRC.
         */

        voAlf.setCmrc(currentCmrc);


        // =====================================================
        // UPDATE EDITABLE FIELDS
        // =====================================================

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


        // =====================================================
        // SAVE
        // =====================================================

        VoAlf updated =
                voAlfRepository.save(voAlf);


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


    // =========================================================
    // DELETE
    // =========================================================

    @Override
    public void delete(Long id) {

        /*
         * Only delete VO/ALF belonging to
         * logged-in user's CMRC.
         */

        VoAlf voAlf =
                getAuthorizedVoAlf(id);

        voAlfRepository.delete(voAlf);
    }
}