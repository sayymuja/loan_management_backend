package com.example.loan_management.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "fund_account",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_fund_account_vo_alf",
                        columnNames = "vo_alf_id"
                )
        }
)
@Data
public class FundAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vo_alf_id", nullable = false)
    private VoAlf voAlf;

    @Column(
            name = "opening_balance",
            nullable = false,
            precision = 15,
            scale = 2
    )
    private BigDecimal openingBalance = BigDecimal.ZERO;

    @Column(
            name = "balance",
            nullable = false,
            precision = 15,
            scale = 2
    )
    private BigDecimal balance = BigDecimal.ZERO;

    @Version
    private Long version;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (openingBalance == null) {
            openingBalance = BigDecimal.ZERO;
        }

        if (balance == null) {
            balance = openingBalance;
        }

        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}