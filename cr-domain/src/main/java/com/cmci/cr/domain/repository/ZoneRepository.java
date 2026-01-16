package com.cmci.cr.domain.repository;

import com.cmci.cr.domain.model.Zone;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Port (interface) pour la persistence des Zones
 * Implémenté dans le module infrastructure
 */
public interface ZoneRepository {

    /**
     * Sauvegarde une zone
     */
    Zone save(Zone zone);

    /**
     * Trouve une zone par son ID
     */
    Optional<Zone> findById(UUID id);

    /**
     * Trouve toutes les zones d'une région
     */
    List<Zone> findByRegionId(UUID regionId);

    /**
     * Trouve toutes les zones
     */
    List<Zone> findAll();

    /**
     * Supprime une zone
     */
    void deleteById(UUID id);

    /**
     * Compte le nombre de zones dans une région
     */
    long countByRegionId(UUID regionId);
}
