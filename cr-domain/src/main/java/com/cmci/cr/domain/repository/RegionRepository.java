package com.cmci.cr.domain.repository;

import com.cmci.cr.domain.model.Region;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Port (interface) pour la persistence des Régions
 * Implémenté dans le module infrastructure
 */
public interface RegionRepository {

    /**
     * Sauvegarde une région
     */
    Region save(Region region);

    /**
     * Trouve une région par son ID
     */
    Optional<Region> findById(UUID id);

    /**
     * Trouve une région par son code
     */
    Optional<Region> findByCode(String code);

    /**
     * Trouve toutes les régions
     */
    List<Region> findAll();

    /**
     * Vérifie si un code existe déjà
     */
    boolean existsByCode(String code);

    /**
     * Supprime une région
     */
    void deleteById(UUID id);
}
