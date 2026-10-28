
package com.example.loan_management.auth.entity;

import com.example.loan_management.entity.Cmrc;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // User details
    @Column(nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    // Role is assigned by the backend, never trusted from signup input
    @Column(name = "role", nullable = false, length = 20)
    private String role = "USER";

    // Account approval status: PENDING, ACTIVE, REJECTED
    @Column(name = "account_status", nullable = false, length = 20)
    private String accountStatus = "PENDING";

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "approved_by_user_id")
    private Long approvedByUserId;

    // Location details
    @Column(name = "district")
    private String district;

    @Column(name = "taluka")
    private String taluka;

    // Account details
    @Column(name = "account_no")
    private String accountNo;

    @Column(name = "account_opening_date")
    private LocalDate accountOpeningDate;

    // CMRC relationship
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cmrc_id")
    private Cmrc cmrc;

    public User() {
    }

    public User(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = "USER";
        this.accountStatus = "PENDING";
    }
}
