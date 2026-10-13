        package com.example.loan_management.entity;

import com.example.loan_management.auth.entity.User;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "loan_approval_requests")
@Data
public class LoanApprovalRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // CREATE, UPDATE or DELETE
    @Column(name = "request_type", nullable = false, length = 20)
    private String requestType;

    // PENDING, APPROVED or REJECTED
    @Column(name = "status", nullable = false, length = 20)
    private String status = "PENDING";

    // Existing loan ID for UPDATE/DELETE; null for CREATE
    @Column(name = "loan_id")
    private Long loanId;

    // JSON containing the requested loan details
    @Column(name = "request_data", nullable = false, columnDefinition = "LONGTEXT")
    private String requestData;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "requested_by_user_id", nullable = false)
    private User requestedBy;

    @Column(name = "cmrc_id", nullable = false)
    private Long cmrcId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by_user_id")
    private User reviewedBy;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "admin_remarks", length = 1000)
    private String adminRemarks;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }

        if (status == null || status.isBlank()) {
            status = "PENDING";
        }
    }
}