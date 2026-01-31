package com.cmci.cr.infrastructure.persistence.adapter;

import com.cmci.cr.domain.model.Utilisateur;
import com.cmci.cr.domain.repository.UtilisateurRepository;
import com.cmci.cr.domain.valueobject.Role;
import com.cmci.cr.infrastructure.persistence.entity.UtilisateurJpaEntity;
import com.cmci.cr.infrastructure.persistence.mapper.UtilisateurMapper;
import com.cmci.cr.infrastructure.persistence.repository.UtilisateurJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Adaptateur qui implémente le port UtilisateurRepository
 * en utilisant Spring Data JPA
 */
@Component
@RequiredArgsConstructor
public class UtilisateurRepositoryAdapter implements UtilisateurRepository {

    private final UtilisateurJpaRepository jpaRepository;
    private final UtilisateurMapper mapper;

    @Override
    public Utilisateur save(Utilisateur utilisateur) {
        UtilisateurJpaEntity jpaEntity = mapper.toJpaEntity(utilisateur);
        UtilisateurJpaEntity saved = jpaRepository.save(jpaEntity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Utilisateur> findById(UUID id) {
        return jpaRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<Utilisateur> findByEmail(String email) {
        return jpaRepository.findByEmail(email)
                .map(mapper::toDomain);
    }

    @Override
    public List<Utilisateur> findByEgliseMaisonId(UUID egliseMaisonId) {
        return jpaRepository.findByEgliseMaisonId(egliseMaisonId)
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Utilisateur> findByFdId(UUID fdId) {
        return jpaRepository.findByFdId(fdId)
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Utilisateur> findByRole(Role role) {
        UtilisateurJpaEntity.RoleEnum jpaRole = UtilisateurJpaEntity.RoleEnum.valueOf(role.name());
        return jpaRepository.findByRole(jpaRole)
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpaRepository.existsByEmail(email);
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public long countByFdId(UUID fdId) {
        return jpaRepository.countByFdId(fdId);
    }

    @Override
    public List<Utilisateur> findByEgliseMaisonIdIn(List<UUID> egliseMaisonIds) {
        if (egliseMaisonIds == null || egliseMaisonIds.isEmpty()) {
            return List.of();
        }
        return jpaRepository.findByEgliseMaisonIdIn(egliseMaisonIds)
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
}
