
package com.example.loan_management.auth.controller;

import com.example.loan_management.auth.dto.RoleUpdateRequest;
import com.example.loan_management.auth.dto.UserManagementDto;
import com.example.loan_management.auth.entity.User;
import com.example.loan_management.auth.repository.UserRepository;
import com.example.loan_management.auth.service.CurrentUserService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/api/super-admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class SuperAdminController {

    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;

    // List all users without exposing passwords
    @GetMapping("/users")
    @Transactional(readOnly = true)
    public List<UserManagementDto> getAllUsers() {

        return userRepository.findAll()
                .stream()
                .map(this::toDto)
                .toList();
    }

    // Assign ADMIN or SUPER_ADMIN role
    @PutMapping("/users/{id}/role")
    @Transactional
    public UserManagementDto updateUserRole(
            @PathVariable Long id,
            @RequestBody RoleUpdateRequest request) {

        if (request == null || request.getRole() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Role is required"
            );
        }

        String role = request.getRole()
                .trim()
                .toUpperCase(Locale.ROOT)
                .replace("ROLE_", "");

        if (!role.equals("ADMIN")
                && !role.equals("SUPER_ADMIN")) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Only ADMIN and SUPER_ADMIN roles can be assigned"
            );
        }

        Long currentUserId = currentUserService.getCurrentUserId();

        if (currentUserId.equals(id)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "You cannot change your own role"
            );
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found"
                ));

        user.setRole(role);

        User savedUser = userRepository.save(user);

        return toDto(savedUser);
    }

    private UserManagementDto toDto(User user) {

        Long cmrcId = null;
        String cmrcName = null;

        if (user.getCmrc() != null) {
            cmrcId = user.getCmrc().getId();
            cmrcName = user.getCmrc().getCmrcName();
        }

        return new UserManagementDto(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                cmrcId,
                cmrcName
        );
    }
}
