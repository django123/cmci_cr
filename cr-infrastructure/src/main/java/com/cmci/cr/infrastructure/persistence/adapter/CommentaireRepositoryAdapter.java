package com.cmci.cr.infrastructure.persistence.adapter;

import com.cmci.cr.domain.model.Commentaire;
import com.cmci.cr.domain.repository.CommentaireRepository;
import com.cmci.cr.infrastructure.persistence.entity.CommentaireJpaEntity;
import com.cmci.cr.infrastructure.persistence.mapper.CommentaireMapper;
import com.cmci.cr.infrastructure.persistence.repository.CommentaireJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Adaptateur qui implémente le port CommentaireRepository
 * en utilisant Spring Data JPA
 */
@Component
@RequiredArgsConstructor
public class CommentaireRepositoryAdapter implements CommentaireRepository {

    private final CommentaireJpaRepository jpaRepository;
    private final CommentaireMapper mapper;

    @Override
    public Commentaire save(Commentaire commentaire) {
        CommentaireJpaEntity jpaEntity = mapper.toJpaEntity(commentaire);
        CommentaireJpaEntity saved = jpaRepository.save(jpaEntity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Commentaire> findById(UUID id) {
        return jpaRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public List<Commentaire> findByCompteRenduId(UUID compteRenduId) {
        return jpaRepository.findByCompteRenduIdOrderByCreatedAtAsc(compteRenduId)
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Commentaire> findByAuteurId(UUID auteurId) {
        return jpaRepository.findByAuteurIdOrderByCreatedAtDesc(auteurId)
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public long countByCompteRenduId(UUID compteRenduId) {
        return jpaRepository.countByCompteRenduId(compteRenduId);
    }
}
