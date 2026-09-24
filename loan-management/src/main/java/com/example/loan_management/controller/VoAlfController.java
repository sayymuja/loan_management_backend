package com.example.loan_management.controller;

import com.example.loan_management.dto.VoAlfDto;
import com.example.loan_management.service.VoAlfService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vo-alf")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class VoAlfController {

    private final VoAlfService voAlfService;

    @PostMapping
    public VoAlfDto create(@RequestBody VoAlfDto voAlfDto) {
        return voAlfService.create(voAlfDto);
    }

    @GetMapping
    public List<VoAlfDto> getAll() {
        return voAlfService.getAll();
    }

    @GetMapping("/{id}")
    public VoAlfDto getById(@PathVariable Long id) {
        return voAlfService.getById(id);
    }

    @PutMapping("/{id}")
    public VoAlfDto update(
            @PathVariable Long id,
            @RequestBody VoAlfDto voAlfDto) {

        return voAlfService.update(id, voAlfDto);
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id) {
        voAlfService.delete(id);

        return "VO/ALF deleted successfully";
    }
    @GetMapping("/cmrc/{cmrcId}")
    public List<VoAlfDto> getByCmrcId(@PathVariable Long cmrcId) {
        return voAlfService.getByCmrcId(cmrcId);
    }
}