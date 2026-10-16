        package com.example.loan_management.auth.controller;

import com.example.loan_management.auth.dto.AuthResponse;
import com.example.loan_management.auth.dto.LoginRequest;
import com.example.loan_management.auth.dto.SignupRequest;
import com.example.loan_management.auth.entity.User;
import com.example.loan_management.auth.service.AuthService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:4200")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // =========================================================
    // SIGNUP
    // =========================================================

    @PostMapping("/signup")
    public ResponseEntity<User> signup(
            @RequestBody SignupRequest request) {

        User savedUser = authService.signup(request);

        // Never return password
        savedUser.setPassword(null);

        return ResponseEntity.ok(savedUser);
    }

    // =========================================================
    // LOGIN
    // =========================================================

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @RequestBody LoginRequest request) {

        // Generate JWT token
        String token = authService.login(
                request.getEmail(),
                request.getPassword()
        );

        // Fetch user
        User user = authService.getUserByEmail(
                request.getEmail()
        );

        // Get CMRC details
        Long cmrcId = null;
        String cmrcName = null;

        if (user.getCmrc() != null) {
            cmrcId = user.getCmrc().getId();
            cmrcName = user.getCmrc().getCmrcName();
        }

        // Build login response
        AuthResponse response = new AuthResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                cmrcId,
                cmrcName,
                user.getDistrict(),
                user.getTaluka(),
                user.getAccountNo(),
                user.getAccountOpeningDate(),
                token,
                user.getRole()
        );

        return ResponseEntity.ok(response);
    }
}
