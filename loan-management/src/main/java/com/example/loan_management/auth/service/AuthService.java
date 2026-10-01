package com.example.loan_management.auth.service;

import com.example.loan_management.auth.dto.SignupRequest;
import com.example.loan_management.auth.entity.User;
import com.example.loan_management.auth.repository.UserRepository;
import com.example.loan_management.auth.security.JwtService;
import com.example.loan_management.entity.Cmrc;
import com.example.loan_management.repository.CmrcRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final CmrcRepository cmrcRepository;

    private final BCryptPasswordEncoder passwordEncoder =
            new BCryptPasswordEncoder();

    public AuthService(
            UserRepository userRepository,
            JwtService jwtService,
            CmrcRepository cmrcRepository) {

        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.cmrcRepository = cmrcRepository;
    }

    // =========================================================
    // SIGNUP
    // =========================================================

    public User signup(SignupRequest request) {

        // Check email
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        // Validate CMRC name
        if (request.getCmrcName() == null ||
                request.getCmrcName().trim().isEmpty()) {

            throw new RuntimeException("CMRC Name is required");
        }

        // Validate District
        if (request.getDistrict() == null ||
                request.getDistrict().trim().isEmpty()) {

            throw new RuntimeException("District is required");
        }

        // Validate Taluka
        if (request.getTaluka() == null ||
                request.getTaluka().trim().isEmpty()) {

            throw new RuntimeException("Taluka is required");
        }

        // Find existing CMRC by name
        Cmrc cmrc = cmrcRepository
                .findByCmrcNameIgnoreCase(
                        request.getCmrcName().trim()
                )
                .orElseGet(() -> {

                    // Create new CMRC
                    Cmrc newCmrc = new Cmrc();

                    newCmrc.setCmrcName(
                            request.getCmrcName().trim()
                    );

                    return cmrcRepository.save(newCmrc);
                });

        // =====================================================
        // CREATE USER
        // =====================================================

        User user = new User();

        user.setName(request.getName());

        user.setEmail(request.getEmail());

        user.setPassword(
                passwordEncoder.encode(
                        request.getPassword()
                )
        );

        // =====================================================
        // LOCATION
        // =====================================================

        user.setDistrict(
                request.getDistrict().trim()
        );

        user.setTaluka(
                request.getTaluka().trim()
        );

        // =====================================================
        // LINK CMRC WITH USER
        // =====================================================

        user.setCmrc(cmrc);

        return userRepository.save(user);
    }


    // =========================================================
    // LOGIN
    // =========================================================

    public String login(
            String email,
            String password) {

        User user =
                userRepository.findByEmail(email)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Invalid email or password"
                                )
                        );

        if (!passwordEncoder.matches(
                password,
                user.getPassword())) {

            throw new RuntimeException(
                    "Invalid email or password"
            );
        }

        return jwtService.generateToken(
                user.getEmail()
        );
    }


    // =========================================================
    // GET USER BY EMAIL
    // =========================================================

    public User getUserByEmail(String email) {

        return userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found"
                        )
                );
    }
}