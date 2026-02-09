package com.cmci.cr.application.usecase;

import com.cmci.cr.domain.repository.RegionRepository;
import com.cmci.cr.domain.repository.ZoneRepository;
import lombok.RequiredArgsConstructor;

import java.util.NoSuchElementException;
import java.util.UUID;

@RequiredArgsConstructor
public class DeleteRegionUseCase {

    private final RegionRepository regionRepository;
    private final ZoneRepository zoneRepository;

    public void execute(UUID id) {
        regionRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException(
                        "Région non trouvée avec l'ID: " + id));

        long zonesCount = zoneRepository.countByRegionId(id);
        if (zonesCount > 0) {
            throw new IllegalStateException(
                    "Impossible de supprimer la région: " + zonesCount + " zone(s) y sont rattachée(s)");
        }

        regionRepository.deleteById(id);
    }
}
