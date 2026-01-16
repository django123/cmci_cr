package com.cmci.cr.infrastructure.persistence.repository;

import com.cmci.cr.infrastructure.persistence.entity.RegionJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository Spring Data JPA pour Region
 */
@Repository
public interface RegionJpaRepository extends JpaRepository<RegionJpaEntity, UUID> {

    /**
     * Trouve une région par son code
     */
    Optional<RegionJpaEntity> findByCode(String code);

    /**
     * Vérifie si un code existe déjà
     */
    boolean existsByCode(String code);
}
