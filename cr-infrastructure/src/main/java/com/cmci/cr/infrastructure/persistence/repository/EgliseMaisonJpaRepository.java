package com.cmci.cr.infrastructure.persistence.repository;

import com.cmci.cr.infrastructure.persistence.entity.EgliseMaisonJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository Spring Data JPA pour EgliseMaison
 */
@Repository
public interface EgliseMaisonJpaRepository extends JpaRepository<EgliseMaisonJpaEntity, UUID> {

    /**
     * Trouve une église de maison par son code
     */
    Optional<EgliseMaisonJpaEntity> findByCode(String code);

    /**
     * Trouve toutes les églises de maison d'une église locale
     */
    List<EgliseMaisonJpaEntity> findByEgliseLocaleId(UUID egliseLocaleId);

    /**
     * Vérifie si un code existe déjà
     */
    boolean existsByCode(String code);

    /**
     * Trouve l'église de maison dirigée par un leader
     */
    List<EgliseMaisonJpaEntity> findByLeaderId(UUID leaderId);
}
