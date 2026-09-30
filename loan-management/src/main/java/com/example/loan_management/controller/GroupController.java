package com.example.loan_management.controller;

import com.example.loan_management.dto.GroupDto;
import com.example.loan_management.service.GroupService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/group")
@CrossOrigin(origins = "http://localhost:4200")
public class GroupController {

    private final GroupService groupService;

    public GroupController(GroupService groupService) {
        this.groupService = groupService;
    }

    // =====================================================
    // CREATE GROUP
    // =====================================================
    @PostMapping
    public ResponseEntity<GroupDto> create(
            @RequestBody GroupDto groupDto) {

        GroupDto savedGroup =
                groupService.create(groupDto);

        return new ResponseEntity<>(
                savedGroup,
                HttpStatus.CREATED
        );
    }

    // =====================================================
    // GET ALL GROUPS
    // =====================================================
    @GetMapping
    public ResponseEntity<List<GroupDto>> getAll() {

        return ResponseEntity.ok(
                groupService.getAll()
        );
    }

    // =====================================================
    // GET GROUP BY ID
    // =====================================================
    @GetMapping("/{id}")
    public ResponseEntity<GroupDto> getById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                groupService.getById(id)
        );
    }

    // =====================================================
    // GET GROUPS BY CMRC ID
    // =====================================================
    @GetMapping("/cmrc/{cmrcId}")
    public ResponseEntity<List<GroupDto>> getByCmrcId(
            @PathVariable Long cmrcId) {

        return ResponseEntity.ok(
                groupService.getByCmrcId(cmrcId)
        );
    }

    // =====================================================
    // GET GROUPS BY VO / ALF ID
    // =====================================================
    @GetMapping("/vo-alf/{voAlfId}")
    public ResponseEntity<List<GroupDto>> getByVoAlfId(
            @PathVariable Long voAlfId) {

        return ResponseEntity.ok(
                groupService.getByVoAlfId(voAlfId)
        );
    }

    // =====================================================
    // UPDATE GROUP
    // =====================================================
    @PutMapping("/{id}")
    public ResponseEntity<GroupDto> update(
            @PathVariable Long id,
            @RequestBody GroupDto groupDto) {

        return ResponseEntity.ok(
                groupService.update(id, groupDto)
        );
    }

    // =====================================================
    // DELETE GROUP
    // =====================================================
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id) {

        groupService.delete(id);

        return ResponseEntity.noContent().build();
    }
}