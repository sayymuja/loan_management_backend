package com.example.loan_management.service.impl;

import com.example.loan_management.auth.service.CurrentUserService;
import com.example.loan_management.dto.WomenDto;
import com.example.loan_management.entity.Group;
import com.example.loan_management.entity.Women;
import com.example.loan_management.entity.VoAlf;
import com.example.loan_management.repository.GroupRepository;
import com.example.loan_management.repository.VoAlfRepository;
import com.example.loan_management.repository.WomenRepository;
import com.example.loan_management.service.WomenService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class WomenServiceImpl implements WomenService {

    private final WomenRepository womenRepository;
    private final GroupRepository groupRepository;
    private final VoAlfRepository voAlfRepository;
    private final CurrentUserService currentUserService;

    public WomenServiceImpl(
            WomenRepository womenRepository,
            GroupRepository groupRepository,
            VoAlfRepository voAlfRepository,
            CurrentUserService currentUserService) {

        this.womenRepository = womenRepository;
        this.groupRepository = groupRepository;
        this.voAlfRepository = voAlfRepository;
        this.currentUserService = currentUserService;
    }


    // =====================================================
    // CURRENT CMRC
    // =====================================================

    private Long getCurrentCmrcId() {

        return currentUserService.getCurrentCmrcId();
    }


    // =====================================================
    // GET AUTHORIZED GROUP
    //
    // Logged-in User
    //      ↓
    //     CMRC
    //      ↓
    //   VO / ALF
    //      ↓
    //    Group
    // =====================================================

    private Group getAuthorizedGroup(Long groupId) {

        if (groupId == null) {

            throw new RuntimeException(
                    "Group ID is required"
            );
        }

        Group group =
                groupRepository.findById(groupId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Group not found with ID: "
                                                + groupId
                                )
                        );

        Long currentCmrcId =
                getCurrentCmrcId();


        // -------------------------------------------------
        // GROUP -> CMRC
        // -------------------------------------------------

        if (group.getCmrcId() == null ||
                !currentCmrcId.equals(
                        group.getCmrcId()
                )) {

            throw new RuntimeException(
                    "Access denied for this Group"
            );
        }


        // -------------------------------------------------
        // GROUP -> VO / ALF
        // -------------------------------------------------

        if (group.getVoAlfId() == null) {

            throw new RuntimeException(
                    "VO / ALF ID not found for this Group"
            );
        }


        VoAlf voAlf =
                voAlfRepository.findById(
                        group.getVoAlfId()
                ).orElseThrow(() ->
                        new RuntimeException(
                                "VO / ALF not found with ID: "
                                        + group.getVoAlfId()
                        )
                );


        // -------------------------------------------------
        // VO / ALF -> CMRC
        // -------------------------------------------------

        if (voAlf.getCmrc() == null ||
                !currentCmrcId.equals(
                        voAlf.getCmrc().getId()
                )) {

            throw new RuntimeException(
                    "Access denied for this VO / ALF"
            );
        }

        return group;
    }


    // =====================================================
    // GET AUTHORIZED WOMAN
    //
    // Logged-in User
    //      ↓
    //     CMRC
    //      ↓
    //   VO / ALF
    //      ↓
    //    Group
    //      ↓
    //    Woman
    // =====================================================

    private Women getAuthorizedWoman(Long womanId) {

        if (womanId == null) {

            throw new RuntimeException(
                    "Woman ID is required"
            );
        }

        Women women =
                womenRepository.findById(womanId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Woman not found with ID: "
                                                + womanId
                                )
                        );


        if (women.getGroup() == null) {

            throw new RuntimeException(
                    "Group not found for this woman"
            );
        }


        // -------------------------------------------------
        // Verify:
        // Woman -> Group -> CMRC -> VO / ALF
        // -------------------------------------------------

        getAuthorizedGroup(
                women.getGroup().getId()
        );

        return women;
    }


    // =====================================================
    // CREATE WOMAN
    // =====================================================

    @Override
    public WomenDto create(WomenDto womenDto) {

        if (womenDto.getGroupId() == null) {

            throw new RuntimeException(
                    "Group ID is required"
            );
        }


        // -------------------------------------------------
        // Frontend Group ID must belong to current CMRC
        // -------------------------------------------------

        Group group =
                getAuthorizedGroup(
                        womenDto.getGroupId()
                );


        Women women =
                new Women();

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

        Long currentCmrcId =
                getCurrentCmrcId();


        // -------------------------------------------------
        // IMPORTANT:
        // Database se directly current CMRC ki women
        // fetch hongi.
        // -------------------------------------------------

        return womenRepository
                .findByGroup_CmrcId(currentCmrcId)
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
                getAuthorizedWoman(id);

        return convertToDto(women);
    }


    // =====================================================
    // GET WOMEN BY GROUP ID
    // =====================================================

    @Override
    public List<WomenDto> getByGroupId(
            Long groupId) {

        // -------------------------------------------------
        // First verify Group belongs to current CMRC
        // -------------------------------------------------

        getAuthorizedGroup(groupId);


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

        // -------------------------------------------------
        // Existing woman must belong to current CMRC
        // -------------------------------------------------

        Women women =
                getAuthorizedWoman(id);


        // -------------------------------------------------
        // New Group
        // -------------------------------------------------

        if (womenDto.getGroupId() == null) {

            throw new RuntimeException(
                    "Group ID is required"
            );
        }


        // -------------------------------------------------
        // New Group must also belong to current CMRC
        // -------------------------------------------------

        Group group =
                getAuthorizedGroup(
                        womenDto.getGroupId()
                );


        women.setGroup(group);


        // -------------------------------------------------
        // Woman Details
        // -------------------------------------------------

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

        // -------------------------------------------------
        // Verify ownership before delete
        // -------------------------------------------------

        Women women =
                getAuthorizedWoman(id);

        womenRepository.delete(women);
    }


    // =====================================================
    // ENTITY → DTO
    // =====================================================

    private WomenDto convertToDto(
            Women women) {

        WomenDto dto =
                new WomenDto();


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


            dto.setGroupId(
                    group.getId()
            );

            dto.setCmrcId(
                    group.getCmrcId()
            );

            dto.setVoAlfId(
                    group.getVoAlfId()
            );

            dto.setGroupName(
                    group.getGroupName()
            );

            dto.setVillageName(
                    group.getVillageName()
            );
        }


        return dto;
    }
}