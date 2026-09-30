package com.example.loan_management.service.impl;

import com.example.loan_management.dto.WomenDto;
import com.example.loan_management.entity.Group;
import com.example.loan_management.entity.Women;
import com.example.loan_management.repository.GroupRepository;
import com.example.loan_management.repository.WomenRepository;
import com.example.loan_management.service.WomenService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class WomenServiceImpl implements WomenService {

    private final WomenRepository womenRepository;
    private final GroupRepository groupRepository;

    public WomenServiceImpl(
            WomenRepository womenRepository,
            GroupRepository groupRepository) {

        this.womenRepository = womenRepository;
        this.groupRepository = groupRepository;
    }


    // =====================================================
    // CREATE WOMAN
    // =====================================================

    @Override
    public WomenDto create(WomenDto womenDto) {

        if (womenDto.getGroupId() == null) {
            throw new RuntimeException("Group ID is required");
        }

        Group group = groupRepository.findById(
                womenDto.getGroupId()
        ).orElseThrow(() ->
                new RuntimeException(
                        "Group not found with ID: "
                                + womenDto.getGroupId()
                )
        );

        Women women = new Women();

        women.setGroup(group);

        women.setWomanName(
                womenDto.getWomanName()
        );

        women.setHusbandName(
                womenDto.getHusbandName()
        );

        women.setMobileNo(
                womenDto.getMobileNo()
        );

        women.setAddress(
                womenDto.getAddress()
        );

        women.setStatus(
                womenDto.getStatus()
        );

        Women savedWomen =
                womenRepository.save(women);

        return convertToDto(savedWomen);
    }


    // =====================================================
    // GET ALL WOMEN
    // =====================================================

    @Override
    public List<WomenDto> getAll() {

        return womenRepository.findAll()
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }


    // =====================================================
    // GET WOMAN BY ID
    // =====================================================

    @Override
    public WomenDto getById(Long id) {

        Women women =
                womenRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Woman not found with ID: "
                                                + id
                                )
                        );

        return convertToDto(women);
    }


    // =====================================================
    // GET WOMEN BY GROUP ID
    // =====================================================

    @Override
    public List<WomenDto> getByGroupId(
            Long groupId) {

        return womenRepository
                .findByGroupId(groupId)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }


    // =====================================================
    // UPDATE WOMAN
    // =====================================================

    @Override
    public WomenDto update(
            Long id,
            WomenDto womenDto) {

        Women women =
                womenRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Woman not found with ID: "
                                                + id
                                )
                        );


        // =================================================
        // UPDATE GROUP
        // =================================================

        if (womenDto.getGroupId() == null) {
            throw new RuntimeException(
                    "Group ID is required"
            );
        }

        Group group =
                groupRepository.findById(
                        womenDto.getGroupId()
                ).orElseThrow(() ->
                        new RuntimeException(
                                "Group not found with ID: "
                                        + womenDto.getGroupId()
                        )
                );

        women.setGroup(group);


        // =================================================
        // UPDATE WOMAN DETAILS
        // =================================================

        women.setWomanName(
                womenDto.getWomanName()
        );

        women.setHusbandName(
                womenDto.getHusbandName()
        );

        women.setMobileNo(
                womenDto.getMobileNo()
        );

        women.setAddress(
                womenDto.getAddress()
        );

        women.setStatus(
                womenDto.getStatus()
        );


        Women updatedWomen =
                womenRepository.save(women);

        return convertToDto(updatedWomen);
    }


    // =====================================================
    // DELETE WOMAN
    // =====================================================

    @Override
    public void delete(Long id) {

        if (!womenRepository.existsById(id)) {

            throw new RuntimeException(
                    "Woman not found with ID: " + id
            );
        }

        womenRepository.deleteById(id);
    }


    // =====================================================
    // ENTITY → DTO
    // =====================================================

    private WomenDto convertToDto(
            Women women) {

        WomenDto dto = new WomenDto();


        // =================================================
        // WOMAN
        // =================================================

        dto.setId(
                women.getId()
        );

        dto.setWomanName(
                women.getWomanName()
        );

        dto.setHusbandName(
                women.getHusbandName()
        );

        dto.setMobileNo(
                women.getMobileNo()
        );

        dto.setAddress(
                women.getAddress()
        );

        dto.setStatus(
                women.getStatus()
        );


        // =================================================
        // GROUP / HIERARCHY
        // =================================================

        if (women.getGroup() != null) {

            Group group =
                    women.getGroup();


            // Group ID
            dto.setGroupId(
                    group.getId()
            );


            // CMRC ID
            dto.setCmrcId(
                    group.getCmrcId()
            );


            // VO / ALF ID
            dto.setVoAlfId(
                    group.getVoAlfId()
            );


            // Group Name
            dto.setGroupName(
                    group.getGroupName()
            );


            // Village
            dto.setVillageName(
                    group.getVillageName()
            );
        }


        return dto;
    }
}