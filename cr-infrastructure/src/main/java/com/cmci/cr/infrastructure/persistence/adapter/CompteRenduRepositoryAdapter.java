package com.cmci.cr.infrastructure.persistence.adapter;

import com.cmci.cr.domain.model.CompteRendu;
import com.cmci.cr.domain.repository.CompteRenduRepository;
import com.cmci.cr.infrastructure.persistence.entity.CompteRenduJpaEntity;
import com.cmci.cr.infrastructure.persistence.mapper.CompteRenduMapper;
import com.cmci.cr.infrastructure.persistence.repository.CompteRenduJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Adaptateur qui implémente le port CompteRenduRepository
 * en utilisant Spring Data JPA
 */
@Component
@RequiredArgsConstructor
public class CompteRenduRepositoryAdapter implements CompteRenduRepository {

    private final CompteRenduJpaRepository jpaRepository;
    private final CompteRenduMapper mapper;

    @Override
    public CompteRendu save(CompteRendu compteRendu) {
        CompteRenduJpaEntity jpaEntity = mapper.toJpaEntity(compteRendu);
        CompteRenduJpaEntity saved = jpaRepository.save(jpaEntity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<CompteRendu> findById(UUID id) {
        return jpaRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<CompteRendu> findByUtilisateurIdAndDate(UUID utilisateurId, LocalDate date) {
        return jpaRepository.findByUtilisateurIdAndDate(utilisateurId, date)
                .map(mapper::toDomain);
    }

    @Override
    public List<CompteRendu> findByUtilisateurId(UUID utilisateurId) {
        return jpaRepository.findByUtilisateurIdOrderByDateDesc(utilisateurId)
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<CompteRendu> findByUtilisateurIdAndDateBetween(
            UUID utilisateurId,
            LocalDate startDate,
            LocalDate endDate) {
        return jpaRepository.findByUtilisateurIdAndDateBetween(utilisateurId, startDate, endDate)
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<CompteRendu> findByUtilisateurIdAndVuParFdFalse(UUID utilisateurId) {
        return jpaRepository.findByUtilisateurIdAndVuParFdFalseOrderByDateDesc(utilisateurId)
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public boolean existsByUtilisateurIdAndDate(UUID utilisateurId, LocalDate date) {
        return jpaRepository.existsByUtilisateurIdAndDate(utilisateurId, date);
    }

    @Override
    public long countByUtilisateurIdAndDateBetween(
            UUID utilisateurId,
            LocalDate startDate,
            LocalDate endDate) {
        return jpaRepository.countByUtilisateurIdAndDateBetween(utilisateurId, startDate, endDate);
    }
}
