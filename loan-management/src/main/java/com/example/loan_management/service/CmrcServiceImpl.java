
package com.example.loan_management.service.impl;

import com.example.loan_management.auth.service.CurrentUserService;
import com.example.loan_management.dto.CmrcDto;
import com.example.loan_management.entity.Cmrc;
import com.example.loan_management.repository.CmrcRepository;
import com.example.loan_management.service.CmrcService;

import lombok.RequiredArgsConstructor;

import org.modelmapper.ModelMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CmrcServiceImpl implements CmrcService {

    private final CmrcRepository cmrcRepository;
    private final ModelMapper modelMapper;
    private final CurrentUserService currentUserService;

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

    @Override
    public CmrcDto create(CmrcDto cmrcDto) {
        throw new RuntimeException(
                "CMRC creation is not allowed for logged-in users"
        );
    }

    @Override
    public CmrcDto addBalance(Double amount) {

        rejectSuperAdminWrite();

        if (amount == null || amount <= 0) {
            throw new RuntimeException(
                    "Balance amount must be greater than 0"
            );
        }

        Cmrc cmrc = getCurrentCmrc();

        Double existingBalance =
                cmrc.getTotalFund() == null
                        ? 0.0
                        : cmrc.getTotalFund();

        cmrc.setTotalFund(existingBalance + amount);

        Cmrc savedCmrc = cmrcRepository.save(cmrc);
        cmrcRepository.flush();

        return modelMapper.map(savedCmrc, CmrcDto.class);
    }

    @Override
    public List<CmrcDto> getAll() {

        List<Cmrc> cmrcList;

        if (isSuperAdmin()) {
            // SUPER_ADMIN can view every CMRC.
            cmrcList = cmrcRepository.findAll();
        } else {
            // ADMIN and USER can view only their assigned CMRC.
            cmrcList = List.of(getCurrentCmrc());
        }

        return cmrcList.stream()
                .map(cmrc -> modelMapper.map(cmrc, CmrcDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public CmrcDto getById(Long id) {

        Cmrc cmrc;

        if (isSuperAdmin()) {
            // Read-only access to any CMRC.
            cmrc = cmrcRepository.findById(id)
                    .orElseThrow(() ->
                            new RuntimeException("CMRC not found"));
        } else {
            cmrc = getAuthorizedCmrc(id);
        }

        return modelMapper.map(cmrc, CmrcDto.class);
    }

    @Override
    public CmrcDto update(Long id, CmrcDto cmrcDto) {

        rejectSuperAdminWrite();

        Cmrc cmrc = getAuthorizedCmrc(id);

        cmrc.setCmrcName(cmrcDto.getCmrcName());
        cmrc.setAccountNo(cmrcDto.getAccountNo());
        cmrc.setAccountOpeningDate(cmrcDto.getAccountOpeningDate());
        cmrc.setDistrict(cmrcDto.getDistrict());
        cmrc.setTaluka(cmrcDto.getTaluka());
        cmrc.setStatus(cmrcDto.getStatus());

        if (cmrcDto.getTotalFund() != null) {
            cmrc.setTotalFund(cmrcDto.getTotalFund());
        }

        Cmrc updatedCmrc = cmrcRepository.save(cmrc);
        cmrcRepository.flush();

        return modelMapper.map(updatedCmrc, CmrcDto.class);
    }

    @Override
    public void delete(Long id) {

        rejectSuperAdminWrite();

        Cmrc cmrc = getAuthorizedCmrc(id);

        cmrcRepository.delete(cmrc);
        cmrcRepository.flush();
    }

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
