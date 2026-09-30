package com.example.loan_management.controller;

import com.example.loan_management.dto.VillageDto;
import com.example.loan_management.service.VillageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/village")
@CrossOrigin(origins = "http://localhost:4200")
public class VillageController {

    private final VillageService villageService;

    public VillageController(VillageService villageService) {
        this.villageService = villageService;
    }

    // =====================================================
    // Create Village
    // POST /api/village
    // =====================================================
    @PostMapping
    public ResponseEntity<VillageDto> create(
            @RequestBody VillageDto villageDto) {

        VillageDto savedVillage = villageService.create(villageDto);

        return new ResponseEntity<>(
                savedVillage,
                HttpStatus.CREATED
        );
    }

    // =====================================================
    // Get All Villages
    // GET /api/village
    // =====================================================
    @GetMapping
    public ResponseEntity<List<VillageDto>> getAll() {

        return ResponseEntity.ok(
                villageService.getAll()
        );
    }

    // =====================================================
    // Get Village By ID
    // GET /api/village/{id}
    // =====================================================
    @GetMapping("/{id}")
    public ResponseEntity<VillageDto> getById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                villageService.getById(id)
        );
    }

    // =====================================================
    // Get Villages By VO / ALF ID
    // GET /api/village/vo-alf/{voAlfId}
    // =====================================================
    @GetMapping("/vo-alf/{voAlfId}")
    public ResponseEntity<List<VillageDto>> getByVoAlfId(
            @PathVariable Long voAlfId) {

        return ResponseEntity.ok(
                villageService.getByVoAlfId(voAlfId)
        );
    }

    // =====================================================
    // Update Village
    // PUT /api/village/{id}
    // =====================================================
    @PutMapping("/{id}")
    public ResponseEntity<VillageDto> update(
            @PathVariable Long id,
            @RequestBody VillageDto villageDto) {

        return ResponseEntity.ok(
                villageService.update(id, villageDto)
        );
    }

    // =====================================================
    // Delete Village
    // DELETE /api/village/{id}
    // =====================================================
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id) {

        villageService.delete(id);

        return ResponseEntity.noContent().build();
    }
}