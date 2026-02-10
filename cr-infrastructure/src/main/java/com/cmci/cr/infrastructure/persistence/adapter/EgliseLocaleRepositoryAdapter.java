package com.cmci.cr.infrastructure.persistence.adapter;

import com.cmci.cr.domain.model.EgliseLocale;
import com.cmci.cr.domain.repository.EgliseLocaleRepository;
import com.cmci.cr.infrastructure.persistence.entity.EgliseLocaleJpaEntity;
import com.cmci.cr.infrastructure.persistence.mapper.EgliseLocaleMapper;
import com.cmci.cr.infrastructure.persistence.repository.EgliseLocaleJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Adaptateur qui implémente le port EgliseLocaleRepository
 * en utilisant Spring Data JPA
 */
@Component
@RequiredArgsConstructor
public class EgliseLocaleRepositoryAdapter implements EgliseLocaleRepository {

    private final EgliseLocaleJpaRepository jpaRepository;
    private final EgliseLocaleMapper mapper;

    @Override
    public EgliseLocale save(EgliseLocale egliseLocale) {
        EgliseLocaleJpaEntity jpaEntity = mapper.toJpaEntity(egliseLocale);
        EgliseLocaleJpaEntity saved = jpaRepository.save(jpaEntity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<EgliseLocale> findById(UUID id) {
        return jpaRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public List<EgliseLocale> findByZoneId(UUID zoneId) {
        return jpaRepository.findByZoneId(zoneId)
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<EgliseLocale> findByPasteurId(UUID pasteurId) {
        return jpaRepository.findByPasteurId(pasteurId)
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<EgliseLocale> findAll() {
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
    public long countByZoneId(UUID zoneId) {
        return jpaRepository.countByZoneId(zoneId);
    }

    @Override
    public List<EgliseLocale> findByPasteurIdIsNull() {
        return jpaRepository.findByPasteurIdIsNull()
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
}
