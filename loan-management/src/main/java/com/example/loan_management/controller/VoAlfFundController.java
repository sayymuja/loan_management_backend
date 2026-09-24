package com.example.loan_management.controller;

import com.example.loan_management.dto.VoAlfFundDto;
import com.example.loan_management.service.VoAlfFundService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vo-alf-fund")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class VoAlfFundController {

    private final VoAlfFundService voAlfFundService;

    @PostMapping
    public VoAlfFundDto create(@RequestBody VoAlfFundDto dto) {
        return voAlfFundService.create(dto);
    }

    @GetMapping
    public List<VoAlfFundDto> getAll() {
        return voAlfFundService.getAll();
    }

    @GetMapping("/{id}")
    public VoAlfFundDto getById(@PathVariable Long id) {
        return voAlfFundService.getById(id);
    }

    @PutMapping("/{id}")
    public VoAlfFundDto update(
            @PathVariable Long id,
            @RequestBody VoAlfFundDto dto) {

        return voAlfFundService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id) {

        voAlfFundService.delete(id);

        return "VO/ALF Fund deleted successfully";
    }
}