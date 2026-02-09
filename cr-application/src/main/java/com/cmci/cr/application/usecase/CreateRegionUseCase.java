package com.cmci.cr.application.usecase;

import com.cmci.cr.application.dto.command.CreateRegionCommand;
import com.cmci.cr.application.dto.response.RegionResponse;
import com.cmci.cr.domain.model.Region;
import com.cmci.cr.domain.repository.RegionRepository;
import com.cmci.cr.domain.repository.ZoneRepository;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@RequiredArgsConstructor
public class CreateRegionUseCase {

    private final RegionRepository regionRepository;
    private final ZoneRepository zoneRepository;

    public RegionResponse execute(CreateRegionCommand command) {
        if (regionRepository.existsByCode(command.getCode())) {
            throw new IllegalArgumentException(
                    "Une région avec ce code existe déjà: " + command.getCode()
            );
        }

        Region region = Region.builder()
                .id(UUID.randomUUID())
                .nom(command.getNom())
                .code(command.getCode())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        region.validate();

        Region saved = regionRepository.save(region);
        return mapToResponse(saved);
    }

    private RegionResponse mapToResponse(Region region) {
        return RegionResponse.builder()
                .id(region.getId())
                .nom(region.getNom())
                .code(region.getCode())
                .nombreZones(zoneRepository.countByRegionId(region.getId()))
                .createdAt(region.getCreatedAt())
                .updatedAt(region.getUpdatedAt())
                .build();
    }
}
