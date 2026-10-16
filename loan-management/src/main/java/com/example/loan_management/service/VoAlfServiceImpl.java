
package com.example.loan_management.service;

import com.example.loan_management.auth.service.CurrentUserService;
import com.example.loan_management.dto.VoAlfDto;
import com.example.loan_management.entity.Cmrc;
import com.example.loan_management.entity.VoAlf;
import com.example.loan_management.repository.CmrcRepository;
import com.example.loan_management.repository.VoAlfRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class VoAlfServiceImpl implements VoAlfService {

    private final VoAlfRepository voAlfRepository;
    private final CmrcRepository cmrcRepository;
    private final CurrentUserService currentUserService;

    // =========================================================
    // CURRENT LOGGED-IN USER CMRC
    // =========================================================

    private Cmrc getCurrentCmrc() {

        Long currentCmrcId = currentUserService.getCurrentCmrcId();

        if (currentCmrcId == null) {
            throw new IllegalStateException(
                    "CMRC not found for logged-in user"
            );
        }

        return cmrcRepository.findById(currentCmrcId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "CMRC not found for logged-in user"
                        )
                );
    }

    // =========================================================
    // CHECK VO / ALF AUTHORIZATION
    // =========================================================

    private VoAlf getAuthorizedVoAlf(Long id) {

        VoAlf voAlf = voAlfRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "VO / ALF not found"
                        )
                );

        Long currentCmrcId = currentUserService.getCurrentCmrcId();

        if (currentCmrcId == null
                || voAlf.getCmrc() == null
                || !currentCmrcId.equals(voAlf.getCmrc().getId())) {

            throw new IllegalArgumentException(
                    "Access denied for this VO / ALF"
            );
        }

        return voAlf;
    }

    // =========================================================
    // VALIDATE REQUIRED FIELDS
    // =========================================================

    private void validateDetails(VoAlfDto dto) {

        if (dto == null) {
            throw new IllegalArgumentException(
                    "VO / ALF details are required"
            );
        }

        if (dto.getVillageName() == null
                || dto.getVillageName().isBlank()) {
            throw new IllegalArgumentException(
                    "Village is required"
            );
        }

        if (dto.getVoAlfName() == null
                || dto.getVoAlfName().isBlank()) {
            throw new IllegalArgumentException(
                    "VO / ALF name is required"
            );
        }

        if (dto.getAccountNo() == null
                || dto.getAccountNo().isBlank()) {
            throw new IllegalArgumentException(
                    "Account number is required"
            );
        }
    }

    // =========================================================
    // ENTITY TO DTO
    // =========================================================

    private VoAlfDto convertToDto(VoAlf voAlf) {

        VoAlfDto dto = new VoAlfDto();

        dto.setId(voAlf.getId());

        dto.setCmrcId(
                voAlf.getCmrc() != null
                        ? voAlf.getCmrc().getId()
                        : null
        );

        dto.setVillageName(voAlf.getVillageName());
        dto.setVoAlfName(voAlf.getVoAlfName());
        dto.setAccountNo(voAlf.getAccountNo());

        // Display-only balance.
        dto.setReceivedFund(
                voAlf.getReceivedFund() != null
                        ? voAlf.getReceivedFund()
                        : 0.0
        );

        dto.setCreatedDate(voAlf.getCreatedDate());

        return dto;
    }

    // =========================================================
    // CREATE
    // Initial balance is always zero.
    // Fund Management controls subsequent balance changes.
    // =========================================================

    @Override
    public VoAlfDto create(VoAlfDto voAlfDto) {

        validateDetails(voAlfDto);

        Cmrc currentCmrc = getCurrentCmrc();

        VoAlf voAlf = new VoAlf();

        voAlf.setCmrc(currentCmrc);
        voAlf.setVillageName(voAlfDto.getVillageName().trim());
        voAlf.setVoAlfName(voAlfDto.getVoAlfName().trim());
        voAlf.setAccountNo(voAlfDto.getAccountNo().trim());

        // Never trust a balance supplied by the frontend.
        voAlf.setReceivedFund(0.0);

        VoAlf savedVoAlf = voAlfRepository.save(voAlf);

        return convertToDto(savedVoAlf);
    }

    // =========================================================
    // GET ALL - CURRENT CMRC ONLY
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public List<VoAlfDto> getAll() {

        Long currentCmrcId = currentUserService.getCurrentCmrcId();

        if (currentCmrcId == null) {
            throw new IllegalStateException(
                    "CMRC not found for logged-in user"
            );
        }

        return voAlfRepository.findByCmrcId(currentCmrcId)
                .stream()
                .map(this::convertToDto)
                .toList();
    }

    // =========================================================
    // GET BY ID
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public VoAlfDto getById(Long id) {

        return convertToDto(getAuthorizedVoAlf(id));
    }

    // =========================================================
    // GET BY CMRC ID
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public List<VoAlfDto> getByCmrcId(Long cmrcId) {

        Long currentCmrcId = currentUserService.getCurrentCmrcId();

        if (currentCmrcId == null
                || cmrcId == null
                || !currentCmrcId.equals(cmrcId)) {

            throw new IllegalArgumentException(
                    "Access denied for this CMRC"
            );
        }

        return voAlfRepository.findByCmrcId(currentCmrcId)
                .stream()
                .map(this::convertToDto)
                .toList();
    }

    // =========================================================
    // UPDATE
    // Balance is deliberately NOT updated here.
    // Kept for compatibility with the existing service interface.
    // =========================================================

    @Override
    public VoAlfDto update(Long id, VoAlfDto voAlfDto) {

        validateDetails(voAlfDto);

        VoAlf voAlf = getAuthorizedVoAlf(id);

        voAlf.setVillageName(voAlfDto.getVillageName().trim());
        voAlf.setVoAlfName(voAlfDto.getVoAlfName().trim());
        voAlf.setAccountNo(voAlfDto.getAccountNo().trim());

        // Do not modify:
        // voAlf.setReceivedFund(...)
        // Do not accept a CMRC ID from the request.

        return convertToDto(voAlfRepository.save(voAlf));
    }

    // =========================================================
    // ADD FUND
    // Actual balance changes belong to Fund Management.
    // =========================================================

    @Override
    public VoAlfDto addFund(Long id, BigDecimal amount) {

        if (amount == null
                || amount.compareTo(BigDecimal.ZERO) <= 0) {

            throw new IllegalArgumentException(
                    "Fund amount must be greater than zero"
            );
        }

        VoAlf voAlf = getAuthorizedVoAlf(id);

        // Deliberately do not change the balance here.
        // FundManagementService handles the transaction,
        // approval workflow and actual balance update.

        return convertToDto(voAlf);
    }

    // =========================================================
    // DELETE
    // =========================================================

    @Override
    public void delete(Long id) {

        VoAlf voAlf = getAuthorizedVoAlf(id);

        // Do not force-delete linked financial records.
        // If linked records prevent deletion, handle that safely
        // after reviewing the related entities and repositories.

        voAlfRepository.delete(voAlf);
    }
}
