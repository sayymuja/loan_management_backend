
package com.example.loan_management.entity;

import com.example.loan_management.auth.entity.User;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "fund_approval_requests",
        indexes = {
                @Index(
                        name = "idx_fund_approval_cmrc_status",
                        columnList = "cmrc_id, status"
                ),
                @Index(
                        name = "idx_fund_approval_created",
                        columnList = "created_at"
                )
        }
)
@Data
public class FundApprovalRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // RECEIPT, PAYMENT, CASH_BANK_TRANSFER
    @Column(name = "request_type", nullable = false, length = 30)
    private String requestType;

    // PENDING, APPROVED, REJECTED
    @Column(name = "status", nullable = false, length = 20)
    private String status = "PENDING";

    // JSON containing requested transaction details
    @Column(
            name = "request_data",
            nullable = false,
            columnDefinition = "LONGTEXT"
    )
    private String requestData;

    @Column(name = "cmrc_id", nullable = false)
    private Long cmrcId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "requested_by_user_id", nullable = false)
    private User requestedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by_user_id")
    private User reviewedBy;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "admin_remarks", length = 1000)
    private String adminRemarks;

    @Column(name = "created_at", nullable = false, updatable = false)
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
