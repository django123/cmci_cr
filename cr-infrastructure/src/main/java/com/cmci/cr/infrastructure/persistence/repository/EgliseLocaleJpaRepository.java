package com.cmci.cr.infrastructure.persistence.repository;

import com.cmci.cr.infrastructure.persistence.entity.EgliseLocaleJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository Spring Data JPA pour EgliseLocale
 */
@Repository
public interface EgliseLocaleJpaRepository extends JpaRepository<EgliseLocaleJpaEntity, UUID> {

    /**
     * Trouve une église locale par son code
     */
    Optional<EgliseLocaleJpaEntity> findByCode(String code);

    /**
     * Trouve toutes les églises locales d'une zone
     */
    List<EgliseLocaleJpaEntity> findByZoneId(UUID zoneId);

    /**
     * Vérifie si un code existe déjà
     */
    boolean existsByCode(String code);
}
