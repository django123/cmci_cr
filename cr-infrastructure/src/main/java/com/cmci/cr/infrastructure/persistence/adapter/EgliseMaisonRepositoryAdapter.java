package com.cmci.cr.infrastructure.persistence.adapter;

import com.cmci.cr.domain.model.EgliseMaison;
import com.cmci.cr.domain.repository.EgliseMaisonRepository;
import com.cmci.cr.infrastructure.persistence.entity.EgliseMaisonJpaEntity;
import com.cmci.cr.infrastructure.persistence.mapper.EgliseMaisonMapper;
import com.cmci.cr.infrastructure.persistence.repository.EgliseMaisonJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Adaptateur qui implémente le port EgliseMaisonRepository
 * en utilisant Spring Data JPA
 */
@Component
@RequiredArgsConstructor
public class EgliseMaisonRepositoryAdapter implements EgliseMaisonRepository {

    private final EgliseMaisonJpaRepository jpaRepository;
    private final EgliseMaisonMapper mapper;

    @Override
    public EgliseMaison save(EgliseMaison egliseMaison) {
        EgliseMaisonJpaEntity jpaEntity = mapper.toJpaEntity(egliseMaison);
        EgliseMaisonJpaEntity saved = jpaRepository.save(jpaEntity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<EgliseMaison> findById(UUID id) {
        return jpaRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public List<EgliseMaison> findByEgliseLocaleId(UUID egliseLocaleId) {
        return jpaRepository.findByEgliseLocaleId(egliseLocaleId)
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<EgliseMaison> findByLeaderId(UUID leaderId) {
        return jpaRepository.findByLeaderId(leaderId)
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<EgliseMaison> findAll() {
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
    public long countByEgliseLocaleId(UUID egliseLocaleId) {
        return jpaRepository.findByEgliseLocaleId(egliseLocaleId).size();
    }

    @Override
    public List<EgliseMaison> findByLeaderIdIsNull() {
        return jpaRepository.findAll()
                .stream()
                .filter(e -> e.getLeaderId() == null)
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
}
