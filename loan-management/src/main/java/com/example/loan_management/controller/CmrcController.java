package com.example.loan_management.controller;

import com.example.loan_management.dto.CmrcDto;
import com.example.loan_management.service.CmrcService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/cmrc")
@CrossOrigin(origins = "http://localhost:4200")
public class CmrcController {

    private final CmrcService cmrcService;

    public CmrcController(CmrcService cmrcService) {
        this.cmrcService = cmrcService;
    }


    // =====================================================
    // CREATE CMRC
    // =====================================================

    @PostMapping
    public CmrcDto create(
            @RequestBody CmrcDto cmrc) {

        return cmrcService.create(cmrc);
    }


    // =====================================================
    // ADD BALANCE
    // =====================================================

    @PostMapping("/add-balance")
    public CmrcDto addBalance(
            @RequestBody Double amount) {

        return cmrcService.addBalance(amount);
    }


    // =====================================================
    // GET ALL CMRC
    // =====================================================

    @GetMapping
    public List<CmrcDto> getAll() {

        return cmrcService.getAll();
    }


    // =====================================================
    // GET CMRC BY ID
    // =====================================================

    @GetMapping("/{id}")
    public CmrcDto getById(
            @PathVariable Long id) {

        return cmrcService.getById(id);
    }


    // =====================================================
    // UPDATE CMRC
    // =====================================================

    @PutMapping("/{id}")
    public CmrcDto update(
            @PathVariable Long id,
            @RequestBody CmrcDto cmrc) {

        return cmrcService.update(
                id,
                cmrc
        );
    }


    // =====================================================
    // DELETE CMRC
    // =====================================================

    @DeleteMapping("/{id}")
    public String delete(
            @PathVariable Long id) {

        cmrcService.delete(id);

        return "CMRC deleted successfully";
    }
}