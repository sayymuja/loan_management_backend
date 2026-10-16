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

        if (request.getEmail() == null
                || request.getEmail().trim().isEmpty()) {
            throw new RuntimeException("Email is required");
        }

        String normalizedEmail =
                request.getEmail().trim().toLowerCase();

        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new RuntimeException("Email already registered");
        }

        if (request.getPassword() == null
                || request.getPassword().isEmpty()) {
            throw new RuntimeException("Password is required");
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
            throw new RuntimeException("Account Opening Date is required");
        }

        // Find existing CMRC or create a new one
        Cmrc cmrc = cmrcRepository
                .findByCmrcNameIgnoreCase(
                        request.getCmrcName().trim()
                )
                .orElseGet(() -> {
                    Cmrc newCmrc = new Cmrc();
                    newCmrc.setCmrcName(
                            request.getCmrcName().trim()
                    );
                    return cmrcRepository.save(newCmrc);
                });

        // Create user
        User user = new User();

        user.setName(request.getName().trim());
        user.setEmail(normalizedEmail);

        // First registered user becomes ADMIN.
// All subsequent users become USER.
//        if (userRepository.count() == 0) {
//            user.setRole("ADMIN");
//        } else {
//            user.setRole("USER");
//        }
        user.setRole("USER");

        // Encode password using BCrypt
        user.setPassword(
                passwordEncoder.encode(request.getPassword())
        );

        user.setDistrict(request.getDistrict().trim());
        user.setTaluka(request.getTaluka().trim());

        user.setAccountNo(request.getAccountNo().trim());
        user.setAccountOpeningDate(request.getAccountOpeningDate());
        // Link user to CMRC
        user.setCmrc(cmrc);

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
                    "Invalid email or password"
            );
        }

        String normalizedEmail =
                email.trim().toLowerCase();

        // Case-insensitive email lookup
        User user = userRepository
                .findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Invalid email or password"
                        )
                );

        // Verify password
        if (user.getPassword() == null
                || !passwordEncoder.matches(
                password,
                user.getPassword()
        )) {
            throw new RuntimeException(
                    "Invalid email or password"
            );
        }

        // Return JWT after successful authentication
        return jwtService.generateToken(user.getEmail());
    }

    // =========================================================
    // GET USER BY EMAIL
    // =========================================================

    public User getUserByEmail(String email) {

        if (email == null || email.trim().isEmpty()) {
            throw new RuntimeException("User not found");
        }

        String normalizedEmail =
                email.trim().toLowerCase();

        return userRepository
                .findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(() ->
                        new RuntimeException("User not found")
                );
    }
}
