        package com.example.loan_management.auth.service;

import com.example.loan_management.auth.entity.User;
import com.example.loan_management.auth.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class CurrentUserService {

    private final UserRepository userRepository;

    // CURRENT LOGGED-IN USER
    public User getCurrentUser() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getName() == null
                || "anonymousUser".equals(authentication.getName())) {

            throw new AuthenticationCredentialsNotFoundException(
                    "User is not authenticated"
            );
        }

        String email = authentication.getName().trim();

        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() ->
                        new AuthenticationCredentialsNotFoundException(
                                "Logged-in user not found: " + email
                        )
                );
    }

    // NORMALIZE ROLE
    public String getCurrentRole() {

        User user = getCurrentUser();

        if (user.getRole() == null || user.getRole().trim().isEmpty()) {
            return "";
        }

        return user.getRole()
                .trim()
                .toUpperCase(Locale.ROOT)
                .replaceFirst("^ROLE_", "");
    }

    // CURRENT USER ID
    public Long getCurrentUserId() {
        return getCurrentUser().getId();
    }

    // CHECK WHETHER CURRENT USER IS ADMIN
    public boolean isAdmin() {
        return "ADMIN".equals(getCurrentRole());
    }

    // CHECK WHETHER CURRENT USER IS SUPER_ADMIN
    public boolean isSuperAdmin() {
        return "SUPER_ADMIN".equals(getCurrentRole());
    }

    // CHECK WHETHER CURRENT USER IS ADMIN OR SUPER_ADMIN
    public boolean isAdminOrSuperAdmin() {
        String role = getCurrentRole();

        return "ADMIN".equals(role)
                || "SUPER_ADMIN".equals(role);
    }

    // CURRENT CMRC ID
    public Long getCurrentCmrcId() {

        User user = getCurrentUser();

        if (user.getCmrc() == null) {
            throw new IllegalStateException(
                    "No CMRC assigned to logged-in user"
            );
        }

        return user.getCmrc().getId();
    }

    // CURRENT CMRC NAME
    public String getCurrentCmrcName() {

        User user = getCurrentUser();

        if (user.getCmrc() == null) {
            throw new IllegalStateException(
                    "No CMRC assigned to logged-in user"
            );
        }

        return user.getCmrc().getCmrcName();
    }
}
