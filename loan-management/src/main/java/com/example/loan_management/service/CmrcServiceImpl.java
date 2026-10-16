        package com.example.loan_management.service;

import com.example.loan_management.auth.service.CurrentUserService;
import com.example.loan_management.dto.CmrcDto;
import com.example.loan_management.entity.Cmrc;
import com.example.loan_management.repository.CmrcRepository;

import lombok.RequiredArgsConstructor;

import org.modelmapper.ModelMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CmrcServiceImpl implements CmrcService {

    private final CmrcRepository cmrcRepository;
    private final ModelMapper modelMapper;
    private final CurrentUserService currentUserService;

    // =====================================================
    // ROLE CHECK
    // =====================================================

    private boolean isSuperAdmin() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        return authentication != null
                && authentication.getAuthorities().stream()
                .anyMatch(authority ->
                        "ROLE_SUPER_ADMIN".equals(authority.getAuthority()));
    }

    private void rejectSuperAdminWrite() {

        if (isSuperAdmin()) {
            throw new RuntimeException(
                    "SUPER_ADMIN has read-only access to CMRC records"
            );
        }
    }

    // =====================================================
    // CREATE CMRC
    // =====================================================

    @Override
    public CmrcDto create(CmrcDto cmrcDto) {

        throw new RuntimeException(
                "CMRC creation is not allowed for logged-in users"
        );
    }

    // =====================================================
    // ADD CMRC TOTAL FUND
    // Explicit balance operation only
    // =====================================================

    @Override
    public CmrcDto addBalance(Double amount) {

        rejectSuperAdminWrite();

        if (amount == null
                || !Double.isFinite(amount)
                || amount <= 0) {

            throw new RuntimeException(
                    "Balance amount must be a valid number greater than 0"
            );
        }

        Cmrc cmrc = getCurrentCmrc();

        Double existingBalance =
                cmrc.getTotalFund() == null
                        ? 0.0
                        : cmrc.getTotalFund();

        cmrc.setTotalFund(existingBalance + amount);

        Cmrc savedCmrc = cmrcRepository.save(cmrc);

        return convertToDto(savedCmrc);
    }

    // =====================================================
    // GET ALL CMRC
    // =====================================================

    @Override
    @Transactional(readOnly = true)
    public List<CmrcDto> getAll() {

        List<Cmrc> cmrcList;

        if (isSuperAdmin()) {
            cmrcList = cmrcRepository.findAll();
        } else {
            cmrcList = List.of(getCurrentCmrc());
        }

        return cmrcList.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // =====================================================
    // GET CMRC BY ID
    // =====================================================

    @Override
    @Transactional(readOnly = true)
    public CmrcDto getById(Long id) {

        Cmrc cmrc;

        if (isSuperAdmin()) {

            cmrc = cmrcRepository.findById(id)
                    .orElseThrow(() ->
                            new RuntimeException("CMRC not found"));

        } else {

            cmrc = getAuthorizedCmrc(id);
        }

        return convertToDto(cmrc);
    }

    // =====================================================
    // UPDATE CMRC DETAILS
    // IMPORTANT: totalFund is not modified here
    // =====================================================


    @Override
    public CmrcDto update(Long id, CmrcDto cmrcDto) {

        rejectSuperAdminWrite();

        Cmrc cmrc = getAuthorizedCmrc(id);

        // Update only Balance Amount and Status.
        if (cmrcDto.getTotalFund() == null
                || !Double.isFinite(cmrcDto.getTotalFund())
                || cmrcDto.getTotalFund() < 0) {

            throw new RuntimeException(
                    "Balance amount must be a valid number greater than or equal to 0"
            );
        }

        String status = cmrcDto.getStatus();

        if (status == null
                || (!"ACTIVE".equalsIgnoreCase(status)
                && !"INACTIVE".equalsIgnoreCase(status))) {

            throw new RuntimeException(
                    "Status must be ACTIVE or INACTIVE"
            );
        }

        // Set the new balance directly; do not add it to the old balance.
        cmrc.setTotalFund(cmrcDto.getTotalFund());
        cmrc.setStatus(status.toUpperCase());

        // Preserve CMRC name, account number, opening date,
        // district and taluka without modifying them.
        Cmrc updatedCmrc = cmrcRepository.save(cmrc);

        return convertToDto(updatedCmrc);
    }


    // =====================================================
    // DELETE CMRC
    // =====================================================

    @Override
    public void delete(Long id) {

        rejectSuperAdminWrite();

        Cmrc cmrc = getAuthorizedCmrc(id);

        cmrcRepository.delete(cmrc);
    }

    // =====================================================
    // ENTITY TO DTO
    // =====================================================

    private CmrcDto convertToDto(Cmrc cmrc) {

        CmrcDto dto = modelMapper.map(cmrc, CmrcDto.class);

        // Single balance field
        dto.setTotalFund(
                cmrc.getTotalFund() == null
                        ? 0.0
                        : cmrc.getTotalFund()
        );

        return dto;
    }

    // =====================================================
    // GET CURRENT USER CMRC
    // =====================================================

    private Cmrc getCurrentCmrc() {

        Long cmrcId = currentUserService.getCurrentCmrcId();

        if (cmrcId == null) {
            throw new RuntimeException(
                    "No CMRC assigned to logged-in user"
            );
        }

        return cmrcRepository.findById(cmrcId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "CMRC not found for logged-in user"
                        ));
    }

    // =====================================================
    // AUTHORIZE CMRC
    // =====================================================

    private Cmrc getAuthorizedCmrc(Long id) {

        Long currentCmrcId = currentUserService.getCurrentCmrcId();

        if (currentCmrcId == null) {
            throw new RuntimeException(
                    "No CMRC assigned to logged-in user"
            );
        }

        if (!currentCmrcId.equals(id)) {
            throw new RuntimeException(
                    "You are not authorized to access this CMRC"
            );
        }

        return cmrcRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("CMRC not found"));
    }
}
