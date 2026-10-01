package com.example.loan_management.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "loan_images")
@Data
public class LoanImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    // =========================================================
    // LOAN
    // =========================================================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "loan_id",
            nullable = false
    )
    private Loan loan;


    // =========================================================
    // FILE DETAILS
    // =========================================================

    @Column(
            name = "file_name",
            nullable = false
    )
    private String fileName;


    @Column(
            name = "file_path",
            nullable = false,
            length = 500
    )
    private String filePath;


    @Column(
            name = "content_type",
            length = 100
    )
    private String contentType;


    @Column(
            name = "file_size"
    )
    private Long fileSize;


    // =========================================================
    // UPLOAD DATE
    // =========================================================

    @Column(
            name = "uploaded_at",
            nullable = false
    )
    private LocalDateTime uploadedAt;
}