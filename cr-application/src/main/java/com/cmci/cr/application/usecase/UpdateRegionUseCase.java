package com.cmci.cr.application.usecase;

import com.cmci.cr.application.dto.command.UpdateRegionCommand;
import com.cmci.cr.application.dto.response.RegionResponse;
import com.cmci.cr.domain.model.Region;
import com.cmci.cr.domain.repository.RegionRepository;
import com.cmci.cr.domain.repository.ZoneRepository;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;

@RequiredArgsConstructor
public class UpdateRegionUseCase {

    private final RegionRepository regionRepository;
    private final ZoneRepository zoneRepository;

    public RegionResponse execute(UpdateRegionCommand command) {
        Region existing = regionRepository.findById(command.getId())
                .orElseThrow(() -> new NoSuchElementException(
                        "Région non trouvée avec l'ID: " + command.getId()));

        Region updated = existing;

        if (command.getNom() != null) {
            updated = updated.withNom(command.getNom());
        }
        if (command.getCode() != null) {
            if (!command.getCode().equals(existing.getCode())
                    && regionRepository.existsByCode(command.getCode())) {
                throw new IllegalArgumentException(
                        "Une région avec ce code existe déjà: " + command.getCode());
            }
            updated = updated.withCode(command.getCode());
        }

        updated = updated.withUpdatedAt(LocalDateTime.now());
        updated.validate();

        Region saved = regionRepository.save(updated);
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
