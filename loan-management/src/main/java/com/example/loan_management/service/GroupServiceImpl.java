package com.example.loan_management.service.impl;

import com.example.loan_management.auth.service.CurrentUserService;
import com.example.loan_management.dto.GroupDto;
import com.example.loan_management.entity.Group;
import com.example.loan_management.entity.VoAlf;
import com.example.loan_management.repository.GroupRepository;
import com.example.loan_management.repository.VoAlfRepository;
import com.example.loan_management.service.GroupService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class GroupServiceImpl implements GroupService {

    private final GroupRepository groupRepository;
    private final VoAlfRepository voAlfRepository;
    private final CurrentUserService currentUserService;

    public GroupServiceImpl(
            GroupRepository groupRepository,
            VoAlfRepository voAlfRepository,
            CurrentUserService currentUserService) {

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
    // GET AUTHORIZED VO / ALF
    //
    // Logged-in User
    //      ↓
    //     CMRC
    //      ↓
    //   VO / ALF
    // =====================================================

    private VoAlf getAuthorizedVoAlf(Long voAlfId) {

        if (voAlfId == null) {

            throw new RuntimeException(
                    "VO / ALF ID is required"
            );
        }

        VoAlf voAlf =
                voAlfRepository.findById(voAlfId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "VO / ALF not found with ID: "
                                                + voAlfId
                                )
                        );

        Long currentCmrcId =
                getCurrentCmrcId();

        if (voAlf.getCmrc() == null ||
                !currentCmrcId.equals(
                        voAlf.getCmrc().getId()
                )) {

            throw new RuntimeException(
                    "Access denied for this VO / ALF"
            );
        }

        return voAlf;
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

        // Verify VO/ALF also belongs to current CMRC
        getAuthorizedVoAlf(
                group.getVoAlfId()
        );

        return group;
    }


    // =====================================================
    // CREATE GROUP
    // =====================================================

    @Override
    public GroupDto create(GroupDto groupDto) {

        if (groupDto.getVoAlfId() == null) {

            throw new RuntimeException(
                    "VO / ALF ID is required"
            );
        }

        // -------------------------------------------------
        // IMPORTANT:
        // Never trust cmrcId from frontend.
        // Verify VO/ALF belongs to logged-in user's CMRC.
        // -------------------------------------------------

        getAuthorizedVoAlf(
                groupDto.getVoAlfId()
        );

        Long currentCmrcId =
                getCurrentCmrcId();


        Group group =
                new Group();


        // -------------------------------------------------
        // CMRC
        // -------------------------------------------------
        // Do NOT use groupDto.getCmrcId()
        // -------------------------------------------------

        group.setCmrcId(
                currentCmrcId
        );


        // -------------------------------------------------
        // VO / ALF
        // -------------------------------------------------

        group.setVoAlfId(
                groupDto.getVoAlfId()
        );


        // -------------------------------------------------
        // VILLAGE
        // -------------------------------------------------

        group.setVillageName(
                groupDto.getVillageName()
        );


        // -------------------------------------------------
        // GROUP NAME
        // -------------------------------------------------

        group.setGroupName(
                groupDto.getGroupName()
        );


        Group savedGroup =
                groupRepository.save(group);

        return convertToDto(savedGroup);
    }


    // =====================================================
    // GET ALL GROUPS
    // =====================================================

    @Override
    public List<GroupDto> getAll() {

        Long currentCmrcId =
                getCurrentCmrcId();

        return groupRepository
                .findByCmrcId(currentCmrcId)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }


    // =====================================================
    // GET GROUP BY ID
    // =====================================================

    @Override
    public GroupDto getById(Long id) {

        Group group =
                getAuthorizedGroup(id);

        return convertToDto(group);
    }


    // =====================================================
    // GET GROUPS BY CMRC ID
    // =====================================================

    @Override
    public List<GroupDto> getByCmrcId(
            Long cmrcId
    ) {

        Long currentCmrcId =
                getCurrentCmrcId();

        // -------------------------------------------------
        // NEVER allow another CMRC ID
        // -------------------------------------------------

        if (!currentCmrcId.equals(cmrcId)) {

            throw new RuntimeException(
                    "Access denied for this CMRC"
            );
        }

        return groupRepository
                .findByCmrcId(currentCmrcId)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }


    // =====================================================
    // GET GROUPS BY VO / ALF ID
    // =====================================================

    @Override
    public List<GroupDto> getByVoAlfId(
            Long voAlfId
    ) {

        // -------------------------------------------------
        // Verify VO/ALF belongs to current CMRC
        // -------------------------------------------------

        getAuthorizedVoAlf(voAlfId);

        return groupRepository
                .findByVoAlfId(voAlfId)
                .stream()
                .filter(group ->
                        getCurrentCmrcId().equals(
                                group.getCmrcId()
                        )
                )
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }


    // =====================================================
    // UPDATE GROUP
    // =====================================================

    @Override
    public GroupDto update(
            Long id,
            GroupDto groupDto
    ) {

        // -------------------------------------------------
        // Existing Group must belong to current CMRC
        // -------------------------------------------------

        Group group =
                getAuthorizedGroup(id);


        // -------------------------------------------------
        // New VO/ALF must belong to current CMRC
        // -------------------------------------------------

        if (groupDto.getVoAlfId() == null) {

            throw new RuntimeException(
                    "VO / ALF ID is required"
            );
        }

        getAuthorizedVoAlf(
                groupDto.getVoAlfId()
        );


        // -------------------------------------------------
        // CMRC
        // -------------------------------------------------
        // Never take CMRC from frontend.
        // Keep current user's CMRC.
        // -------------------------------------------------

        group.setCmrcId(
                getCurrentCmrcId()
        );


        // -------------------------------------------------
        // VO / ALF
        // -------------------------------------------------

        group.setVoAlfId(
                groupDto.getVoAlfId()
        );


        // -------------------------------------------------
        // VILLAGE
        // -------------------------------------------------

        group.setVillageName(
                groupDto.getVillageName()
        );


        // -------------------------------------------------
        // GROUP NAME
        // -------------------------------------------------

        group.setGroupName(
                groupDto.getGroupName()
        );


        Group updatedGroup =
                groupRepository.save(group);

        return convertToDto(updatedGroup);
    }


    // =====================================================
    // DELETE GROUP
    // =====================================================

    @Override
    public void delete(Long id) {

        // -------------------------------------------------
        // Verify ownership first
        // -------------------------------------------------

        Group group =
                getAuthorizedGroup(id);

        groupRepository.delete(group);
    }


    // =====================================================
    // ENTITY → DTO
    // =====================================================

    private GroupDto convertToDto(
            Group group
    ) {

        GroupDto dto =
                new GroupDto();

        dto.setId(
                group.getId()
        );

        // -------------------------------------------------
        // CMRC
        // -------------------------------------------------

        dto.setCmrcId(
                group.getCmrcId()
        );

        // -------------------------------------------------
        // VO / ALF
        // -------------------------------------------------

        dto.setVoAlfId(
                group.getVoAlfId()
        );

        // -------------------------------------------------
        // VILLAGE
        // -------------------------------------------------

        dto.setVillageName(
                group.getVillageName()
        );

        // -------------------------------------------------
        // GROUP NAME
        // -------------------------------------------------

        dto.setGroupName(
                group.getGroupName()
        );

        return dto;
    }
}