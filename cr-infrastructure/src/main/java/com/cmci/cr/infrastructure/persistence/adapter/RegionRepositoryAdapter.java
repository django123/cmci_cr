package com.cmci.cr.infrastructure.persistence.adapter;

import com.cmci.cr.domain.model.Region;
import com.cmci.cr.domain.repository.RegionRepository;
import com.cmci.cr.infrastructure.persistence.mapper.RegionMapper;
import com.cmci.cr.infrastructure.persistence.repository.RegionJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Adaptateur qui implemente le port RegionRepository
 * en utilisant Spring Data JPA
 */
@Component
@RequiredArgsConstructor
public class RegionRepositoryAdapter implements RegionRepository {

    private final RegionJpaRepository jpaRepository;
    private final RegionMapper mapper;

    @Override
    public Region save(Region region) {
        var jpaEntity = mapper.toJpaEntity(region);
        var saved = jpaRepository.save(jpaEntity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Region> findById(UUID id) {
        return jpaRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<Region> findByCode(String code) {
        return jpaRepository.findByCode(code)
                .map(mapper::toDomain);
    }

    @Override
    public List<Region> findAll() {
        return jpaRepository.findAll()
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsByCode(String code) {
        return jpaRepository.existsByCode(code);
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }
}
