package com.example.loan_management.service;

import com.example.loan_management.dto.LoanImageDto;
import com.example.loan_management.entity.Loan;
import com.example.loan_management.entity.LoanImage;
import com.example.loan_management.repository.LoanImageRepository;
import com.example.loan_management.repository.LoanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LoanImageServiceImpl implements LoanImageService {

    private final LoanImageRepository loanImageRepository;
    private final LoanRepository loanRepository;

    // =========================================================
    // UPLOAD DIRECTORY
    // =========================================================

    @Value("${app.loan-image.upload-dir:uploads/loan-images}")
    private String uploadDir;


    // =========================================================
    // FILE SETTINGS
    // =========================================================

    private static final long MAX_FILE_SIZE =
            5 * 1024 * 1024; // 5 MB

    private static final List<String> ALLOWED_CONTENT_TYPES =
            List.of(
                    "image/jpeg",
                    "image/png",
                    "image/webp"
            );


    // =========================================================
    // UPLOAD IMAGES
    // =========================================================

    @Override
    public List<LoanImageDto> uploadImages(
            Long loanId,
            List<MultipartFile> files
    ) {

        if (loanId == null) {
            throw new RuntimeException(
                    "Loan ID is required"
            );
        }

        if (files == null || files.isEmpty()) {
            throw new RuntimeException(
                    "At least one image is required"
            );
        }

        // =====================================================
        // FIND LOAN
        // =====================================================

        Loan loan =
                loanRepository.findById(loanId)
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Loan not found with ID: "
                                                + loanId
                                )
                        );


        // =====================================================
        // CREATE LOAN IMAGE DIRECTORY
        // =====================================================

        Path loanDirectory =
                Paths.get(
                        uploadDir,
                        String.valueOf(loanId)
                );

        try {

            Files.createDirectories(
                    loanDirectory
            );

        } catch (IOException e) {

            throw new RuntimeException(
                    "Unable to create image upload directory",
                    e
            );
        }


        // =====================================================
        // RESULT
        // =====================================================

        List<LoanImageDto> uploadedImages =
                new ArrayList<>();


        // =====================================================
        // PROCESS EACH FILE
        // =====================================================

        for (MultipartFile file : files) {

            if (file == null || file.isEmpty()) {
                continue;
            }


            // =================================================
            // VALIDATE CONTENT TYPE
            // =================================================

            String contentType =
                    file.getContentType();

            if (
                    contentType == null
                            || !ALLOWED_CONTENT_TYPES.contains(
                            contentType.toLowerCase()
                    )
            ) {

                throw new RuntimeException(
                        "Only JPG, PNG and WEBP images are allowed"
                );
            }


            // =================================================
            // VALIDATE FILE SIZE
            // =================================================

            if (file.getSize() > MAX_FILE_SIZE) {

                throw new RuntimeException(
                        "Image size must not exceed 5 MB"
                );
            }


            // =================================================
            // ORIGINAL FILE NAME
            // =================================================

            String originalFileName =
                    Paths.get(
                                    file.getOriginalFilename() != null
                                            ? file.getOriginalFilename()
                                            : "image"
                            )
                            .getFileName()
                            .toString();


            // =================================================
            // FILE EXTENSION
            // =================================================

            String extension = "";

            int dotIndex =
                    originalFileName.lastIndexOf(".");

            if (dotIndex >= 0) {
                extension =
                        originalFileName.substring(
                                dotIndex
                        );
            }


            // =================================================
            // UNIQUE FILE NAME
            // =================================================

            String uniqueFileName =
                    UUID.randomUUID()
                            + extension;


            // =================================================
            // FILE PATH
            // =================================================

            Path targetPath =
                    loanDirectory.resolve(
                            uniqueFileName
                    );


            // =================================================
            // SAVE PHYSICAL FILE
            // =================================================

            try {

                Files.copy(
                        file.getInputStream(),
                        targetPath
                );

            } catch (IOException e) {

                throw new RuntimeException(
                        "Unable to save image: "
                                + originalFileName,
                        e
                );
            }


            // =================================================
            // SAVE DATABASE RECORD
            // =================================================

            LoanImage loanImage =
                    new LoanImage();

            loanImage.setLoan(loan);

            loanImage.setFileName(
                    originalFileName
            );

            loanImage.setFilePath(
                    targetPath.toString()
            );

            loanImage.setContentType(
                    contentType
            );

            loanImage.setFileSize(
                    file.getSize()
            );

            loanImage.setUploadedAt(
                    LocalDateTime.now()
            );


            LoanImage savedImage =
                    loanImageRepository.save(
                            loanImage
                    );


            // =================================================
            // DTO
            // =================================================

            uploadedImages.add(
                    mapToDto(savedImage)
            );
        }


        return uploadedImages;
    }


    // =========================================================
    // GET IMAGES BY LOAN
    // =========================================================

    @Override
    public List<LoanImageDto> getByLoanId(
            Long loanId
    ) {

        return loanImageRepository
                .findByLoanId(loanId)
                .stream()
                .map(this::mapToDto)
                .toList();
    }


    // =========================================================
    // GET IMAGE BY ID
    // =========================================================

    @Override
    public LoanImageDto getById(
            Long imageId
    ) {

        LoanImage image =
                loanImageRepository.findById(
                        imageId
                ).orElseThrow(
                        () -> new RuntimeException(
                                "Loan image not found with ID: "
                                        + imageId
                        )
                );

        return mapToDto(image);
    }


    // =========================================================
    // DELETE IMAGE
    // =========================================================

    @Override
    public void delete(
            Long imageId
    ) {

        LoanImage image =
                loanImageRepository.findById(
                        imageId
                ).orElseThrow(
                        () -> new RuntimeException(
                                "Loan image not found with ID: "
                                        + imageId
                        )
                );


        // =====================================================
        // DELETE PHYSICAL FILE
        // =====================================================

        deletePhysicalFile(image);


        // =====================================================
        // DELETE DATABASE RECORD
        // =====================================================

        loanImageRepository.delete(
                image
        );
    }


    // =========================================================
    // DELETE ALL IMAGES OF LOAN
    // =========================================================

    @Override
    public void deleteByLoanId(
            Long loanId
    ) {

        List<LoanImage> images =
                loanImageRepository.findByLoanId(
                        loanId
                );


        // =====================================================
        // DELETE PHYSICAL FILES
        // =====================================================

        for (LoanImage image : images) {

            deletePhysicalFile(image);
        }


        // =====================================================
        // DELETE DATABASE RECORDS
        // =====================================================

        loanImageRepository.deleteAll(
                images
        );
    }


    // =========================================================
    // DELETE PHYSICAL FILE
    // =========================================================

    private void deletePhysicalFile(
            LoanImage image
    ) {

        if (
                image == null
                        || image.getFilePath() == null
        ) {
            return;
        }

        try {

            Path path =
                    Paths.get(
                            image.getFilePath()
                    );

            Files.deleteIfExists(path);

        } catch (IOException e) {

            // Physical file delete failure
            // should not stop DB cleanup
            System.err.println(
                    "Unable to delete image file: "
                            + image.getFilePath()
            );
        }
    }


    // =========================================================
    // MAP ENTITY -> DTO
    // =========================================================

    private LoanImageDto mapToDto(
            LoanImage image
    ) {

        LoanImageDto dto =
                new LoanImageDto();

        dto.setId(
                image.getId()
        );

        if (image.getLoan() != null) {

            dto.setLoanId(
                    image.getLoan().getId()
            );
        }

        dto.setFileName(
                image.getFileName()
        );

        dto.setContentType(
                image.getContentType()
        );

        dto.setFileSize(
                image.getFileSize()
        );

        dto.setViewUrl(
                "/api/loan-images/view/"
                        + image.getId()
        );

        dto.setDownloadUrl(
                "/api/loan-images/download/"
                        + image.getId()
        );

        dto.setUploadedAt(
                image.getUploadedAt()
        );

        return dto;
    }
}