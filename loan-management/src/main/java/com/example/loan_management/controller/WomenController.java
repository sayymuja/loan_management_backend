package com.example.loan_management.controller;

import com.example.loan_management.dto.WomenDto;
import com.example.loan_management.service.WomenService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/women")
@CrossOrigin(origins = "http://localhost:4200")
public class WomenController {

    private final WomenService womenService;

    public WomenController(WomenService womenService) {
        this.womenService = womenService;
    }


    // =====================================================
    // CREATE WOMAN
    // =====================================================

    @PostMapping
    public ResponseEntity<WomenDto> create(
            @RequestBody WomenDto womenDto) {

        WomenDto savedWomen = womenService.create(womenDto);

        return new ResponseEntity<>(
                savedWomen,
                HttpStatus.CREATED
        );
    }


    // =====================================================
    // GET ALL WOMEN
    // =====================================================

    @GetMapping
    public ResponseEntity<List<WomenDto>> getAll() {

        return ResponseEntity.ok(
                womenService.getAll()
        );
    }


    // =====================================================
    // GET WOMAN BY ID
    // =====================================================

    @GetMapping("/{id}")
    public ResponseEntity<WomenDto> getById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                womenService.getById(id)
        );
    }


    // =====================================================
    // GET WOMEN BY GROUP ID
    // =====================================================

    @GetMapping("/group/{groupId}")
    public ResponseEntity<List<WomenDto>> getByGroupId(
            @PathVariable Long groupId) {

        return ResponseEntity.ok(
                womenService.getByGroupId(groupId)
        );
    }


    // =====================================================
    // UPDATE WOMAN
    // =====================================================

    @PutMapping("/{id}")
    public ResponseEntity<WomenDto> update(
            @PathVariable Long id,
            @RequestBody WomenDto womenDto) {

        return ResponseEntity.ok(
                womenService.update(id, womenDto)
        );
    }


    // =====================================================
    // DELETE WOMAN
    // =====================================================

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id) {

        womenService.delete(id);

        return ResponseEntity.noContent().build();
    }
}