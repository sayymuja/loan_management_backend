        package com.example.loan_management.auth.service;

import com.example.loan_management.auth.dto.SignupRequest;
import com.example.loan_management.auth.entity.User;
import com.example.loan_management.auth.repository.UserRepository;
import com.example.loan_management.auth.security.JwtService;
import com.example.loan_management.entity.Cmrc;
import com.example.loan_management.repository.CmrcRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final CmrcRepository cmrcRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(
            UserRepository userRepository,
            JwtService jwtService,
            CmrcRepository cmrcRepository,
            PasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.cmrcRepository = cmrcRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // =========================================================
    // SIGNUP
    // =========================================================

    @Transactional
    public User signup(SignupRequest request) {

        if (request == null) {
            throw new RuntimeException(
                    "Registration details are required");
        }

        if (request.getEmail() == null
                || request.getEmail().trim().isEmpty()) {
            throw new RuntimeException("Email is required");
        }

        String normalizedEmail =
                request.getEmail().trim().toLowerCase(Locale.ROOT);

        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new RuntimeException("Email already registered");
        }

        if (request.getPassword() == null
                || request.getPassword().isBlank()) {
            throw new RuntimeException("Password is required");
        }

        if (request.getPassword().length() < 8) {
            throw new RuntimeException(
                    "Password must be at least 8 characters long");
        }

        if (request.getName() == null
                || request.getName().trim().isEmpty()) {
            throw new RuntimeException("Name is required");
        }

        if (request.getCmrcName() == null
                || request.getCmrcName().trim().isEmpty()) {
            throw new RuntimeException("CMRC Name is required");
        }

        if (request.getDistrict() == null
                || request.getDistrict().trim().isEmpty()) {
            throw new RuntimeException("District is required");
        }

        if (request.getTaluka() == null
                || request.getTaluka().trim().isEmpty()) {
            throw new RuntimeException("Taluka is required");
        }

        if (request.getAccountNo() == null
                || request.getAccountNo().trim().isEmpty()) {
            throw new RuntimeException("Account Number is required");
        }

        if (request.getAccountOpeningDate() == null) {
            throw new RuntimeException(
                    "Account Opening Date is required");
        }

        /*
         * First registered user:
         * SUPER_ADMIN + ACTIVE.
         *
         * All subsequent users:
         * USER + PENDING.
         *
         * Important: count-based first-user initialization
         * requires database-level protection against concurrent
         * signup requests in production.
         */
        boolean firstUser = userRepository.count() == 0;

        Cmrc cmrc;

        if (firstUser) {
            // Only the initial setup may create the first CMRC.
            cmrc = cmrcRepository
                    .findByCmrcNameIgnoreCase(
                            request.getCmrcName().trim())
                    .orElseGet(() -> {
                        Cmrc newCmrc = new Cmrc();
                        newCmrc.setCmrcName(
                                request.getCmrcName().trim());
                        return cmrcRepository.save(newCmrc);
                    });
        } else {
            /*
             * Do not create a new CMRC for an unapproved signup.
             * Link an existing CMRC when its name matches.
             * If none exists, leave the relation null so that
             * the approval workflow can assign the correct CMRC.
             */
            cmrc = cmrcRepository
                    .findByCmrcNameIgnoreCase(
                            request.getCmrcName().trim())
                    .orElse(null);
        }

        User user = new User();

        user.setName(request.getName().trim());
        user.setEmail(normalizedEmail);
        user.setPassword(
                passwordEncoder.encode(request.getPassword()));

        user.setDistrict(request.getDistrict().trim());
        user.setTaluka(request.getTaluka().trim());
        user.setAccountNo(request.getAccountNo().trim());
        user.setAccountOpeningDate(
                request.getAccountOpeningDate());

        user.setCmrc(cmrc);

        // Role and account status are always controlled by backend.
        if (firstUser) {
            user.setRole("SUPER_ADMIN");
            user.setAccountStatus("ACTIVE");
        } else {
            user.setRole("USER");
            user.setAccountStatus("PENDING");
        }

        return userRepository.save(user);
    }

    // =========================================================
    // LOGIN
    // =========================================================

    public String login(String email, String password) {

        if (email == null
                || email.trim().isEmpty()
                || password == null
                || password.isEmpty()) {
            throw new RuntimeException(
                    "Invalid email or password");
        }

        String normalizedEmail =
                email.trim().toLowerCase(Locale.ROOT);

        User user = userRepository
                .findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Invalid email or password"));

        if (user.getPassword() == null
                || !passwordEncoder.matches(
                password, user.getPassword())) {
            throw new RuntimeException(
                    "Invalid email or password");
        }

        String status = user.getAccountStatus();

        if (!"ACTIVE".equalsIgnoreCase(status)) {

            if ("REJECTED".equalsIgnoreCase(status)) {
                throw new RuntimeException(
                        "Your account registration was rejected. "
                                + "Please contact the administrator.");
            }

            throw new RuntimeException(
                    "Your account is pending approval. "
                            + "Please contact the administrator.");
        }

        return jwtService.generateToken(user.getEmail());
    }

    // =========================================================
    // GET USER BY EMAIL
    // =========================================================

    @Transactional(readOnly = true)
    public User getUserByEmail(String email) {

        if (email == null || email.trim().isEmpty()) {
            throw new RuntimeException("User not found");
        }

        String normalizedEmail =
                email.trim().toLowerCase(Locale.ROOT);

        return userRepository
                .findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));
    }
}
