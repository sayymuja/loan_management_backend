package com.example.loan_management.service;

import com.example.loan_management.dto.LoanImageDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface LoanImageService {

    // =========================================================
    // UPLOAD IMAGES
    // =========================================================

    List<LoanImageDto> uploadImages(
            Long loanId,
            List<MultipartFile> files
    );


    // =========================================================
    // GET IMAGES BY LOAN
    // =========================================================

    List<LoanImageDto> getByLoanId(Long loanId);


    // =========================================================
    // GET IMAGE BY ID
    // =========================================================

    LoanImageDto getById(Long imageId);


    // =========================================================
    // DELETE IMAGE
    // =========================================================

    void delete(Long imageId);


    // =========================================================
    // DELETE ALL IMAGES OF LOAN
    // Used when Loan itself is deleted
    // =========================================================

    void deleteByLoanId(Long loanId);
}