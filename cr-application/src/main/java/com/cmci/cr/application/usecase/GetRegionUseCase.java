package com.cmci.cr.application.usecase;

import com.cmci.cr.application.dto.response.RegionResponse;
import com.cmci.cr.domain.model.Region;
import com.cmci.cr.domain.repository.RegionRepository;
import com.cmci.cr.domain.repository.ZoneRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class GetRegionUseCase {

    private final RegionRepository regionRepository;
    private final ZoneRepository zoneRepository;

    public RegionResponse getById(UUID id) {
        Region region = regionRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Région non trouvée avec l'ID: " + id));
        return mapToResponse(region);
    }

    public RegionResponse getByCode(String code) {
        Region region = regionRepository.findByCode(code)
                .orElseThrow(() -> new NoSuchElementException("Région non trouvée avec le code: " + code));
        return mapToResponse(region);
    }

    public List<RegionResponse> getAll() {
        return regionRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
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
