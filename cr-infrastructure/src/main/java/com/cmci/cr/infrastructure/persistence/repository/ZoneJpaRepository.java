package com.cmci.cr.infrastructure.persistence.repository;

import com.cmci.cr.infrastructure.persistence.entity.ZoneJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository Spring Data JPA pour Zone
 */
@Repository
public interface ZoneJpaRepository extends JpaRepository<ZoneJpaEntity, UUID> {

    /**
     * Trouve une zone par son code
     */
    Optional<ZoneJpaEntity> findByCode(String code);

    /**
     * Trouve toutes les zones d'une région
     */
    List<ZoneJpaEntity> findByRegionId(UUID regionId);

    /**
     * Vérifie si un code existe déjà
     */
    boolean existsByCode(String code);
}
