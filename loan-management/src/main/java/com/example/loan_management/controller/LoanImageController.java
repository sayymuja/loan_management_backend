package com.example.loan_management.controller;

import com.example.loan_management.dto.LoanImageDto;
import com.example.loan_management.entity.LoanImage;
import com.example.loan_management.repository.LoanImageRepository;
import com.example.loan_management.service.LoanImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/api/loan-images")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class LoanImageController {

    private final LoanImageService loanImageService;
    private final LoanImageRepository loanImageRepository;


    // =========================================================
    // UPLOAD MULTIPLE IMAGES
    // =========================================================

    @PostMapping(
            value = "/upload/{loanId}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<List<LoanImageDto>> uploadImages(
            @PathVariable Long loanId,
            @RequestParam("files") List<org.springframework.web.multipart.MultipartFile> files
    ) {

        List<LoanImageDto> images =
                loanImageService.uploadImages(
                        loanId,
                        files
                );

        return ResponseEntity.ok(images);
    }


    // =========================================================
    // GET ALL IMAGES BY LOAN
    // =========================================================

    @GetMapping("/loan/{loanId}")
    public ResponseEntity<List<LoanImageDto>> getByLoanId(
            @PathVariable Long loanId
    ) {

        return ResponseEntity.ok(
                loanImageService.getByLoanId(loanId)
        );
    }


    // =========================================================
    // VIEW IMAGE
    // =========================================================

    @GetMapping("/view/{imageId}")
    public ResponseEntity<Resource> viewImage(
            @PathVariable Long imageId
    ) {

        LoanImage image =
                loanImageRepository.findById(imageId)
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Loan image not found with ID: "
                                                + imageId
                                )
                        );

        try {

            Path path =
                    Paths.get(
                                    image.getFilePath()
                            )
                            .toAbsolutePath()
                            .normalize();

            Resource resource =
                    new UrlResource(
                            path.toUri()
                    );

            if (!resource.exists()
                    || !resource.isReadable()) {

                throw new RuntimeException(
                        "Image file not found"
                );
            }

            MediaType mediaType =
                    MediaType.APPLICATION_OCTET_STREAM;

            if (image.getContentType() != null) {

                try {

                    mediaType =
                            MediaType.parseMediaType(
                                    image.getContentType()
                            );

                } catch (Exception ignored) {
                    // Keep default content type
                }
            }

            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .header(
                            HttpHeaders.CONTENT_DISPOSITION,
                            ContentDisposition
                                    .inline()
                                    .filename(
                                            image.getFileName()
                                    )
                                    .build()
                                    .toString()
                    )
                    .body(resource);

        } catch (Exception e) {

            throw new RuntimeException(
                    "Unable to view image",
                    e
            );
        }
    }


    // =========================================================
    // DOWNLOAD IMAGE
    // =========================================================

    @GetMapping("/download/{imageId}")
    public ResponseEntity<Resource> downloadImage(
            @PathVariable Long imageId
    ) {

        LoanImage image =
                loanImageRepository.findById(imageId)
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Loan image not found with ID: "
                                                + imageId
                                )
                        );

        try {

            Path path =
                    Paths.get(
                                    image.getFilePath()
                            )
                            .toAbsolutePath()
                            .normalize();

            Resource resource =
                    new UrlResource(
                            path.toUri()
                    );

            if (!resource.exists()
                    || !resource.isReadable()) {

                throw new RuntimeException(
                        "Image file not found"
                );
            }

            MediaType mediaType =
                    MediaType.APPLICATION_OCTET_STREAM;

            if (image.getContentType() != null) {

                try {

                    mediaType =
                            MediaType.parseMediaType(
                                    image.getContentType()
                            );

                } catch (Exception ignored) {
                    // Keep default content type
                }
            }

            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .header(
                            HttpHeaders.CONTENT_DISPOSITION,
                            ContentDisposition
                                    .attachment()
                                    .filename(
                                            image.getFileName()
                                    )
                                    .build()
                                    .toString()
                    )
                    .body(resource);

        } catch (Exception e) {

            throw new RuntimeException(
                    "Unable to download image",
                    e
            );
        }
    }


    // =========================================================
    // DELETE IMAGE
    // =========================================================

    @DeleteMapping("/{imageId}")
    public ResponseEntity<String> deleteImage(
            @PathVariable Long imageId
    ) {

        loanImageService.delete(imageId);

        return ResponseEntity.ok(
                "Loan image deleted successfully"
        );
    }
}