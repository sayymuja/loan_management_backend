        package com.example.loan_management.auth.controller;

import com.example.loan_management.auth.entity.User;
import com.example.loan_management.auth.repository.UserRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/users")
@CrossOrigin(origins = "http://localhost:4200")
public class UserManagementController {

    private final UserRepository userRepository;

    public UserManagementController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // ADMIN: View all users without exposing passwords
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Map<String, Object>>> getAllUsers() {

        List<Map<String, Object>> users = userRepository.findAll()
                .stream()
                .map(user -> Map.<String, Object>of(
                        "id", user.getId(),
                        "name", user.getName(),
                        "email", user.getEmail(),
                        "role", user.getRole(),
                        "cmrcId", user.getCmrc() != null
                                ? user.getCmrc().getId() : 0L
                ))
                .toList();

        return ResponseEntity.ok(users);
    }

    // ADMIN: Change a user's role
    @PutMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateUserRole(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {

        String role = request.get("role");

        if (role == null ||
                !List.of("ADMIN", "USER", "VIEWER").contains(role)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message",
                            "Role must be ADMIN, USER, or VIEWER"));
        }

        User user = userRepository.findById(id).orElse(null);

        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        user.setRole(role);
        userRepository.save(user);

        return ResponseEntity.ok(
                Map.of("message", "User role updated successfully")
        );
    }
}
