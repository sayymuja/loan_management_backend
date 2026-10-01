package com.example.loan_management.service.impl;

import com.example.loan_management.auth.service.CurrentUserService;
import com.example.loan_management.dto.CmrcDto;
import com.example.loan_management.entity.Cmrc;
import com.example.loan_management.repository.CmrcRepository;
import com.example.loan_management.service.CmrcService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CmrcServiceImpl implements CmrcService {

    private final CmrcRepository cmrcRepository;
    private final ModelMapper modelMapper;
    private final CurrentUserService currentUserService;


    // =========================================================
    // CREATE
    // =========================================================

    @Override
    public CmrcDto create(CmrcDto cmrcDto) {

        /*
         * Logged-in users cannot create a new CMRC.
         * They can only manage their assigned CMRC.
         */
        throw new RuntimeException(
                "CMRC creation is not allowed for logged-in users"
        );
    }


    // =========================================================
    // ADD BALANCE
    // =========================================================

    @Override
    public CmrcDto addBalance(Double amount) {

        if (amount == null || amount <= 0) {

            throw new RuntimeException(
                    "Balance amount must be greater than 0"
            );
        }

        /*
         * Get the CMRC assigned to the logged-in user.
         */
        Cmrc cmrc = getCurrentCmrc();


        /*
         * Existing balance.
         */
        Double existingBalance =
                cmrc.getTotalFund() == null
                        ? 0.0
                        : cmrc.getTotalFund();


        /*
         * Add new balance.
         */
        Double updatedBalance =
                existingBalance + amount;


        /*
         * Update SAME CMRC row.
         */
        cmrc.setTotalFund(updatedBalance);


        /*
         * Save.
         */
        Cmrc savedCmrc =
                cmrcRepository.save(cmrc);


        /*
         * Force database synchronization.
         */
        cmrcRepository.flush();


        return modelMapper.map(
                savedCmrc,
                CmrcDto.class
        );
    }


    // =========================================================
    // GET ALL
    // =========================================================

    @Override
    public List<CmrcDto> getAll() {

        /*
         * Only return the CMRC assigned
         * to the logged-in user.
         */
        Cmrc cmrc = getCurrentCmrc();

        return List.of(
                modelMapper.map(
                        cmrc,
                        CmrcDto.class
                )
        );
    }


    // =========================================================
    // GET BY ID
    // =========================================================

    @Override
    public CmrcDto getById(Long id) {

        /*
         * Make sure the requested CMRC belongs
         * to the logged-in user.
         */
        Cmrc cmrc =
                getAuthorizedCmrc(id);

        return modelMapper.map(
                cmrc,
                CmrcDto.class
        );
    }


    // =========================================================
    // UPDATE
    // =========================================================

    @Override
    public CmrcDto update(
            Long id,
            CmrcDto cmrcDto) {

        /*
         * Get existing CMRC.
         *
         * This also checks that the logged-in user
         * is authorized to update this CMRC.
         */
        Cmrc cmrc =
                getAuthorizedCmrc(id);


        // -----------------------------------------------------
        // Update CMRC basic information
        // -----------------------------------------------------

        cmrc.setCmrcName(
                cmrcDto.getCmrcName()
        );

        cmrc.setAccountNo(
                cmrcDto.getAccountNo()
        );

        cmrc.setAccountOpeningDate(
                cmrcDto.getAccountOpeningDate()
        );

        cmrc.setDistrict(
                cmrcDto.getDistrict()
        );

        cmrc.setTaluka(
                cmrcDto.getTaluka()
        );

        cmrc.setStatus(
                cmrcDto.getStatus()
        );


        // -----------------------------------------------------
        // IMPORTANT:
        // Update totalFund in the SAME existing row.
        //
        // Example:
        // Existing balance = 20000
        // Add balance      = 20000
        // Angular sends     = 40000
        //
        // DB result:
        // total_fund = 40000
        // -----------------------------------------------------

        if (cmrcDto.getTotalFund() != null) {

            cmrc.setTotalFund(
                    cmrcDto.getTotalFund()
            );
        }


        // -----------------------------------------------------
        // Save existing CMRC
        // -----------------------------------------------------

        Cmrc updatedCmrc =
                cmrcRepository.save(cmrc);


        // -----------------------------------------------------
        // Force Hibernate to synchronize with DB
        // -----------------------------------------------------

        cmrcRepository.flush();


        // -----------------------------------------------------
        // Return updated CMRC
        // -----------------------------------------------------

        return modelMapper.map(
                updatedCmrc,
                CmrcDto.class
        );
    }


    // =========================================================
    // DELETE
    // =========================================================

    @Override
    public void delete(Long id) {

        /*
         * Only the logged-in user's CMRC can be deleted.
         */
        Cmrc cmrc =
                getAuthorizedCmrc(id);

        cmrcRepository.delete(cmrc);

        cmrcRepository.flush();
    }


    // =========================================================
    // GET CURRENT USER CMRC
    // =========================================================

    private Cmrc getCurrentCmrc() {

        Long cmrcId =
                currentUserService.getCurrentCmrcId();


        if (cmrcId == null) {

            throw new RuntimeException(
                    "No CMRC assigned to logged-in user"
            );
        }


        return cmrcRepository.findById(cmrcId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "CMRC not found for logged-in user"
                        )
                );
    }


    // =========================================================
    // AUTHORIZED CMRC
    // =========================================================

    private Cmrc getAuthorizedCmrc(Long id) {

        Long currentCmrcId =
                currentUserService.getCurrentCmrcId();


        if (currentCmrcId == null) {

            throw new RuntimeException(
                    "No CMRC assigned to logged-in user"
            );
        }


        /*
         * Prevent user from accessing another CMRC.
         */
        if (!currentCmrcId.equals(id)) {

            throw new RuntimeException(
                    "You are not authorized to access this CMRC"
            );
        }


        return cmrcRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "CMRC not found"
                        )
                );
    }
}