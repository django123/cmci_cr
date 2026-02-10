package com.cmci.cr.infrastructure.persistence.adapter;

import com.cmci.cr.domain.model.Zone;
import com.cmci.cr.domain.repository.ZoneRepository;
import com.cmci.cr.infrastructure.persistence.mapper.ZoneMapper;
import com.cmci.cr.infrastructure.persistence.repository.ZoneJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Adaptateur qui implemente le port ZoneRepository
 * en utilisant Spring Data JPA
 */
@Component
@RequiredArgsConstructor
public class ZoneRepositoryAdapter implements ZoneRepository {

    private final ZoneJpaRepository jpaRepository;
    private final ZoneMapper mapper;

    @Override
    public Zone save(Zone zone) {
        var jpaEntity = mapper.toJpaEntity(zone);
        var saved = jpaRepository.save(jpaEntity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Zone> findById(UUID id) {
        return jpaRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public List<Zone> findByRegionId(UUID regionId) {
        return jpaRepository.findByRegionId(regionId)
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Zone> findAll() {
        return jpaRepository.findAll()
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public long countByRegionId(UUID regionId) {
        return jpaRepository.countByRegionId(regionId);
    }
}
