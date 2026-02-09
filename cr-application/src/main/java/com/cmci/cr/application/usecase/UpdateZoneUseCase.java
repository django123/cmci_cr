package com.cmci.cr.application.usecase;

import com.cmci.cr.application.dto.command.UpdateZoneCommand;
import com.cmci.cr.application.dto.response.ZoneResponse;
import com.cmci.cr.domain.model.Zone;
import com.cmci.cr.domain.repository.EgliseLocaleRepository;
import com.cmci.cr.domain.repository.RegionRepository;
import com.cmci.cr.domain.repository.ZoneRepository;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;

@RequiredArgsConstructor
public class UpdateZoneUseCase {

    private final ZoneRepository zoneRepository;
    private final RegionRepository regionRepository;
    private final EgliseLocaleRepository egliseLocaleRepository;

    public ZoneResponse execute(UpdateZoneCommand command) {
        Zone existing = zoneRepository.findById(command.getId())
                .orElseThrow(() -> new NoSuchElementException(
                        "Zone non trouvée avec l'ID: " + command.getId()));

        Zone updated = existing;

        if (command.getNom() != null) {
            updated = updated.withNom(command.getNom());
        }
        if (command.getRegionId() != null) {
            regionRepository.findById(command.getRegionId())
                    .orElseThrow(() -> new NoSuchElementException(
                            "Région non trouvée avec l'ID: " + command.getRegionId()));
            updated = updated.withRegionId(command.getRegionId());
        }

        updated = updated.withUpdatedAt(LocalDateTime.now());
        updated.validate();

        Zone saved = zoneRepository.save(updated);
        return mapToResponse(saved);
    }

    private ZoneResponse mapToResponse(Zone zone) {
        String regionNom = regionRepository.findById(zone.getRegionId())
                .map(r -> r.getNom())
                .orElse(null);

        return ZoneResponse.builder()
                .id(zone.getId())
                .nom(zone.getNom())
                .regionId(zone.getRegionId())
                .regionNom(regionNom)
                .nombreEglisesLocales(egliseLocaleRepository.countByZoneId(zone.getId()))
                .createdAt(zone.getCreatedAt())
                .updatedAt(zone.getUpdatedAt())
                .build();
    }
}
