package com.example.loan_management.auth.service;

import com.example.loan_management.auth.dto.UserApprovalDto;
import com.example.loan_management.auth.entity.User;
import com.example.loan_management.auth.repository.UserRepository;
import com.example.loan_management.entity.Cmrc;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Transactional
public class UserApprovalServiceImpl implements UserApprovalService {

    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;

    // =========================================================
    // ADMIN: GET PENDING USERS FROM OWN CMRC
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public List<UserApprovalDto> getPendingUsers() {

        requireAdmin();

        Long adminCmrcId = currentUserService.getCurrentCmrcId();

        return userRepository
                .findByAccountStatusIgnoreCaseAndCmrc_IdOrderByIdAsc(
                        "PENDING",
                        adminCmrcId
                )
                .stream()
                .map(this::toDto)
                .toList();
    }

    // =========================================================
    // ADMIN: APPROVE USER
    // =========================================================

    @Override
    public UserApprovalDto approveUser(Long userId) {

        requireAdmin();

        Long adminCmrcId = currentUserService.getCurrentCmrcId();

        User user = getPendingUser(userId);

        verifySameCmrc(user, adminCmrcId);

        // Public signup users must remain USER.
        if (!"USER".equalsIgnoreCase(user.getRole())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Only registered USER accounts can be approved here."
            );
        }

        user.setAccountStatus("ACTIVE");
        user.setApprovedAt(java.time.LocalDateTime.now());
        user.setApprovedByUserId(
                currentUserService.getCurrentUserId()
        );

        return toDto(userRepository.save(user));
    }

    // =========================================================
    // ADMIN: REJECT USER
    // =========================================================

    @Override
    public UserApprovalDto rejectUser(Long userId) {

        requireAdmin();

        Long adminCmrcId = currentUserService.getCurrentCmrcId();

        User user = getPendingUser(userId);

        verifySameCmrc(user, adminCmrcId);

        if (!"USER".equalsIgnoreCase(user.getRole())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Only registered USER accounts can be rejected here."
            );
        }

        user.setAccountStatus("REJECTED");
        user.setApprovedAt(null);
        user.setApprovedByUserId(
                currentUserService.getCurrentUserId()
        );

        return toDto(userRepository.save(user));
    }

    // =========================================================
    // SECURITY HELPERS
    // =========================================================

    private void requireAdmin() {

        String role = currentUserService.getCurrentRole();

        if (!"ADMIN".equals(role)) {
            throw new AccessDeniedException(
                    "Only ADMIN can approve or reject user registrations."
            );
        }
    }

    private User getPendingUser(Long userId) {

        if (userId == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "User ID is required."
            );
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found."
                ));

        if (!"PENDING".equalsIgnoreCase(user.getAccountStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Only pending users can be approved or rejected."
            );
        }

        return user;
    }

    private void verifySameCmrc(User user, Long adminCmrcId) {

        if (user.getCmrc() == null
                || user.getCmrc().getId() == null
                || !user.getCmrc().getId().equals(adminCmrcId)) {

            throw new AccessDeniedException(
                    "You can only manage users belonging to your own CMRC."
            );
        }
    }

    // =========================================================
    // ENTITY TO DTO
    // =========================================================

    private UserApprovalDto toDto(User user) {

        Cmrc cmrc = user.getCmrc();

        return new UserApprovalDto(
                user.getId(),
                user.getName(),
                user.getEmail(),
                cmrc != null ? cmrc.getId() : null,
                cmrc != null ? cmrc.getCmrcName() : null,
                user.getDistrict(),
                user.getTaluka(),
                user.getAccountNo(),
                user.getAccountOpeningDate(),
                user.getRole(),
                user.getAccountStatus()
        );
    }
}