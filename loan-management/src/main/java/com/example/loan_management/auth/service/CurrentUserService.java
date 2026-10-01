package com.example.loan_management.auth.service;

import com.example.loan_management.auth.entity.User;
import com.example.loan_management.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CurrentUserService {

    private final UserRepository userRepository;


    // =========================================================
    // CURRENT LOGGED-IN USER
    // =========================================================

    public User getCurrentUser() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null ||
                !authentication.isAuthenticated()) {

            throw new RuntimeException("User is not authenticated");
        }

        String email = authentication.getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("Logged-in user not found"));
    }


    // =========================================================
    // CURRENT USER ID
    // =========================================================

    public Long getCurrentUserId() {

        return getCurrentUser().getId();
    }


    // =========================================================
    // CURRENT CMRC
    // =========================================================

    public Long getCurrentCmrcId() {

        User user = getCurrentUser();

        if (user.getCmrc() == null) {
            throw new RuntimeException(
                    "No CMRC assigned to logged-in user"
            );
        }

        return user.getCmrc().getId();
    }


    // =========================================================
    // CURRENT CMRC NAME
    // =========================================================

    public String getCurrentCmrcName() {

        User user = getCurrentUser();

        if (user.getCmrc() == null) {
            throw new RuntimeException(
                    "No CMRC assigned to logged-in user"
            );
        }

        return user.getCmrc().getCmrcName();
    }
}