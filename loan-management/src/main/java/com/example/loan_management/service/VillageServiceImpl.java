package com.example.loan_management.service.impl;

import com.example.loan_management.dto.VillageDto;
import com.example.loan_management.entity.Village;
import com.example.loan_management.entity.VoAlf;
import com.example.loan_management.repository.VillageRepository;
import com.example.loan_management.service.VillageService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class VillageServiceImpl implements VillageService {

    private final VillageRepository villageRepository;

    public VillageServiceImpl(VillageRepository villageRepository) {
        this.villageRepository = villageRepository;
    }

    // =====================================================
    // CREATE VILLAGE
    // =====================================================
    @Override
    public VillageDto create(VillageDto villageDto) {

        Village village = new Village();

        village.setVillageName(villageDto.getVillageName());

        // VO / ALF Mapping
        if (villageDto.getVoAlfId() != null) {

            VoAlf voAlf = new VoAlf();
            voAlf.setId(villageDto.getVoAlfId());

            village.setVoAlf(voAlf);
        }

        Village savedVillage = villageRepository.save(village);

        return convertToDto(savedVillage);
    }

    // =====================================================
    // GET ALL VILLAGES
    // =====================================================
    @Override
    public List<VillageDto> getAll() {

        return villageRepository.findAll()
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // =====================================================
    // GET VILLAGE BY ID
    // =====================================================
    @Override
    public VillageDto getById(Long id) {

        Village village = villageRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Village not found with ID: " + id
                        )
                );

        return convertToDto(village);
    }

    // =====================================================
    // GET VILLAGES BY VO / ALF ID
    // =====================================================
    @Override
    public List<VillageDto> getByVoAlfId(Long voAlfId) {

        return villageRepository.findByVoAlfId(voAlfId)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // =====================================================
    // UPDATE VILLAGE
    // =====================================================
    @Override
    public VillageDto update(Long id, VillageDto villageDto) {

        Village village = villageRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Village not found with ID: " + id
                        )
                );

        village.setVillageName(villageDto.getVillageName());

        // Update VO / ALF
        if (villageDto.getVoAlfId() != null) {

            VoAlf voAlf = new VoAlf();
            voAlf.setId(villageDto.getVoAlfId());

            village.setVoAlf(voAlf);
        }

        Village updatedVillage = villageRepository.save(village);

        return convertToDto(updatedVillage);
    }

    // =====================================================
    // DELETE VILLAGE
    // =====================================================
    @Override
    public void delete(Long id) {

        if (!villageRepository.existsById(id)) {
            throw new RuntimeException(
                    "Village not found with ID: " + id
            );
        }

        villageRepository.deleteById(id);
    }

    // =====================================================
    // ENTITY → DTO
    // =====================================================
    private VillageDto convertToDto(Village village) {

        VillageDto dto = new VillageDto();

        dto.setId(village.getId());
        dto.setVillageName(village.getVillageName());

        if (village.getVoAlf() != null) {
            dto.setVoAlfId(
                    village.getVoAlf().getId()
            );
        }

        return dto;
    }
}