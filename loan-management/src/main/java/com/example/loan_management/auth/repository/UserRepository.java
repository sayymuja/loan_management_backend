        package com.example.loan_management.auth.repository;

import com.example.loan_management.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // Login and email validation
    Optional<User> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    // All pending users - for authorized global administration only
    List<User> findByAccountStatusIgnoreCaseOrderByIdAsc(
            String accountStatus
    );

    // Pending users belonging to a specific CMRC
    List<User> findByAccountStatusIgnoreCaseAndCmrc_IdOrderByIdAsc(
            String accountStatus,
            Long cmrcId
    );

    // Role-based user lookup
    List<User> findByRoleIgnoreCase(String role);

    // Users belonging to a particular CMRC
    List<User> findByCmrc_Id(Long cmrcId);

    // Count users by role
    long countByRoleIgnoreCase(String role);
}
