package com.example.loan_management.service.impl;

import com.example.loan_management.dto.GroupDto;
import com.example.loan_management.entity.Group;
import com.example.loan_management.repository.GroupRepository;
import com.example.loan_management.service.GroupService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class GroupServiceImpl implements GroupService {

    private final GroupRepository groupRepository;

    public GroupServiceImpl(GroupRepository groupRepository) {
        this.groupRepository = groupRepository;
    }

    // =====================================================
    // CREATE GROUP
    // =====================================================
    @Override
    public GroupDto create(GroupDto groupDto) {

        Group group = new Group();

        // CMRC
        group.setCmrcId(groupDto.getCmrcId());

        // VO / ALF
        group.setVoAlfId(groupDto.getVoAlfId());

        // Village Name
        group.setVillageName(groupDto.getVillageName());

        // Group Name
        group.setGroupName(groupDto.getGroupName());

        Group savedGroup =
                groupRepository.save(group);

        return convertToDto(savedGroup);
    }

    // =====================================================
    // GET ALL GROUPS
    // =====================================================
    @Override
    public List<GroupDto> getAll() {

        return groupRepository.findAll()
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
                groupRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Group not found with ID: " + id
                                )
                        );

        return convertToDto(group);
    }

    // =====================================================
    // GET GROUPS BY CMRC ID
    // =====================================================
    @Override
    public List<GroupDto> getByCmrcId(Long cmrcId) {

        return groupRepository.findByCmrcId(cmrcId)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // =====================================================
    // GET GROUPS BY VO / ALF ID
    // =====================================================
    @Override
    public List<GroupDto> getByVoAlfId(Long voAlfId) {

        return groupRepository.findByVoAlfId(voAlfId)
                .stream()
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

        Group group =
                groupRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Group not found with ID: " + id
                                )
                        );

        // CMRC
        group.setCmrcId(groupDto.getCmrcId());

        // VO / ALF
        group.setVoAlfId(groupDto.getVoAlfId());

        // Village Name
        group.setVillageName(
                groupDto.getVillageName()
        );

        // Group Name
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

        if (!groupRepository.existsById(id)) {

            throw new RuntimeException(
                    "Group not found with ID: " + id
            );
        }

        groupRepository.deleteById(id);
    }

    // =====================================================
    // ENTITY → DTO
    // =====================================================
    private GroupDto convertToDto(
            Group group
    ) {

        GroupDto dto = new GroupDto();

        dto.setId(group.getId());

        // CMRC
        dto.setCmrcId(
                group.getCmrcId()
        );

        // VO / ALF
        dto.setVoAlfId(
                group.getVoAlfId()
        );

        // Village Name
        dto.setVillageName(
                group.getVillageName()
        );

        // Group Name
        dto.setGroupName(
                group.getGroupName()
        );

        return dto;
    }
}