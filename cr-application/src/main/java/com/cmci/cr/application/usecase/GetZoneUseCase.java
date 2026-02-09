package com.cmci.cr.application.usecase;

import com.cmci.cr.application.dto.response.ZoneResponse;
import com.cmci.cr.domain.model.Zone;
import com.cmci.cr.domain.repository.EgliseLocaleRepository;
import com.cmci.cr.domain.repository.RegionRepository;
import com.cmci.cr.domain.repository.ZoneRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class GetZoneUseCase {

    private final ZoneRepository zoneRepository;
    private final RegionRepository regionRepository;
    private final EgliseLocaleRepository egliseLocaleRepository;

    public ZoneResponse getById(UUID id) {
        Zone zone = zoneRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Zone non trouvée avec l'ID: " + id));
        return mapToResponse(zone);
    }

    public List<ZoneResponse> getAll() {
        return zoneRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<ZoneResponse> getByRegionId(UUID regionId) {
        return zoneRepository.findByRegionId(regionId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
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
