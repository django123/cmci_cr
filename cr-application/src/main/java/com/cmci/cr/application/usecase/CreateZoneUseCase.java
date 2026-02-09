package com.cmci.cr.application.usecase;

import com.cmci.cr.application.dto.command.CreateZoneCommand;
import com.cmci.cr.application.dto.response.ZoneResponse;
import com.cmci.cr.domain.model.Region;
import com.cmci.cr.domain.model.Zone;
import com.cmci.cr.domain.repository.EgliseLocaleRepository;
import com.cmci.cr.domain.repository.RegionRepository;
import com.cmci.cr.domain.repository.ZoneRepository;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.UUID;

@RequiredArgsConstructor
public class CreateZoneUseCase {

    private final ZoneRepository zoneRepository;
    private final RegionRepository regionRepository;
    private final EgliseLocaleRepository egliseLocaleRepository;

    public ZoneResponse execute(CreateZoneCommand command) {
        Region region = regionRepository.findById(command.getRegionId())
                .orElseThrow(() -> new NoSuchElementException(
                        "Région non trouvée avec l'ID: " + command.getRegionId()));

        Zone zone = Zone.builder()
                .id(UUID.randomUUID())
                .nom(command.getNom())
                .regionId(command.getRegionId())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        zone.validate();

        Zone saved = zoneRepository.save(zone);
        return mapToResponse(saved, region.getNom());
    }

    private ZoneResponse mapToResponse(Zone zone, String regionNom) {
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
