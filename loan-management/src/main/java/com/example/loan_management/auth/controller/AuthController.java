
package com.example.loan_management.auth.controller;

import com.example.loan_management.auth.dto.AuthResponse;
import com.example.loan_management.auth.dto.LoginRequest;
import com.example.loan_management.auth.dto.SignupRequest;
import com.example.loan_management.auth.entity.User;
import com.example.loan_management.auth.service.AuthService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

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
    public ResponseEntity<Map<String, Object>> signup(
            @RequestBody SignupRequest request) {

        User savedUser = authService.signup(request);

        Map<String, Object> response = new LinkedHashMap<>();

        response.put("id", savedUser.getId());
        response.put("name", savedUser.getName());
        response.put("email", savedUser.getEmail());
        response.put("role", savedUser.getRole());
        response.put("accountStatus", savedUser.getAccountStatus());

        response.put(
                "message",
                "SUPER_ADMIN".equals(savedUser.getRole())
                        ? "Initial SUPER_ADMIN account created successfully."
                        : "Registration submitted. Please wait for administrator approval."
        );

        if (savedUser.getCmrc() != null) {
            response.put("cmrcId", savedUser.getCmrc().getId());
            response.put("cmrcName", savedUser.getCmrc().getCmrcName());
        }

        // Never return password or JWT during signup.
        return ResponseEntity.ok(response);
    }

    // =========================================================
    // LOGIN
    // =========================================================

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @RequestBody LoginRequest request) {

        String token = authService.login(
                request.getEmail(),
                request.getPassword()
        );

        User user = authService.getUserByEmail(
                request.getEmail()
        );

        Long cmrcId = null;
        String cmrcName = null;

        if (user.getCmrc() != null) {
            cmrcId = user.getCmrc().getId();
            cmrcName = user.getCmrc().getCmrcName();
        }

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
                user.getRole(),
                user.getAccountStatus()
        );

        return ResponseEntity.ok(response);
    }
}
