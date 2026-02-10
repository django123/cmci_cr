package com.cmci.cr.application.usecase;

import com.cmci.cr.application.dto.response.RegionResponse;
import com.cmci.cr.application.dto.response.ZoneResponse;
import com.cmci.cr.domain.model.Region;
import com.cmci.cr.domain.model.Zone;
import com.cmci.cr.domain.port.CountryDataPort;
import com.cmci.cr.domain.port.CountryDataPort.CountryInfo;
import com.cmci.cr.domain.repository.RegionRepository;
import com.cmci.cr.domain.repository.ZoneRepository;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
public class SeedGeographyUseCase {

    private final CountryDataPort countryDataPort;
    private final RegionRepository regionRepository;
    private final ZoneRepository zoneRepository;

    private static final Map<String, String> REGION_CODES = Map.of(
            "Africa", "AFR",
            "Americas", "AME",
            "Asia", "ASI",
            "Europe", "EUR",
            "Oceania", "OCE"
    );

    public SeedResult execute() {
        log.info("Starting geography seed from external API...");

        Map<String, List<CountryInfo>> countriesByRegion = countryDataPort.getCountriesGroupedByRegion();

        int regionsCreated = 0;
        int zonesCreated = 0;
        int regionsSkipped = 0;
        int zonesSkipped = 0;

        for (Map.Entry<String, List<CountryInfo>> entry : countriesByRegion.entrySet()) {
            String regionName = entry.getKey();
            List<CountryInfo> countries = entry.getValue();

            String code = REGION_CODES.getOrDefault(regionName, regionName.substring(0, 3).toUpperCase());

            // Check if region already exists
            Region region;
            Optional<Region> existingRegion = regionRepository.findByCode(code);
            if (existingRegion.isPresent()) {
                region = existingRegion.get();
                regionsSkipped++;
                log.info("Region already exists: {} ({})", regionName, code);
            } else {
                region = Region.builder()
                        .id(UUID.randomUUID())
                        .nom(regionName)
                        .code(code)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build();
                region.validate();
                region = regionRepository.save(region);
                regionsCreated++;
                log.info("Created region: {} ({})", regionName, code);
            }

            // Get existing zones for this region to check duplicates
            List<Zone> existingZones = zoneRepository.findByRegionId(region.getId());
            Set<String> existingZoneNames = existingZones.stream()
                    .map(Zone::getNom)
                    .collect(Collectors.toSet());

            for (CountryInfo country : countries) {
                if (existingZoneNames.contains(country.name())) {
                    zonesSkipped++;
                    continue;
                }

                Zone zone = Zone.builder()
                        .id(UUID.randomUUID())
                        .nom(country.name())
                        .regionId(region.getId())
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build();
                zone.validate();
                zoneRepository.save(zone);
                zonesCreated++;
            }

            log.info("Region {}: {} countries processed", regionName, countries.size());
        }

        log.info("Geography seed completed. Regions: {} created, {} skipped. Zones: {} created, {} skipped.",
                regionsCreated, regionsSkipped, zonesCreated, zonesSkipped);

        return SeedResult.builder()
                .regionsCreated(regionsCreated)
                .regionsSkipped(regionsSkipped)
                .zonesCreated(zonesCreated)
                .zonesSkipped(zonesSkipped)
                .build();
    }

    @Value
    @Builder
    public static class SeedResult {
        int regionsCreated;
        int regionsSkipped;
        int zonesCreated;
        int zonesSkipped;
    }
}
