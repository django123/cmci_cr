package com.cmci.cr.domain.model;

import lombok.Builder;
import lombok.Value;
import lombok.With;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entité Zone - Aggregate Root
 * Représente une zone géographique (Pays/Groupe de pays)
 */
@Value
@Builder
@With
public class Zone {
    UUID id;
    String nom;
    UUID regionId;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;

    /**
     * Validation métier
     */
    public void validate() {
        if (nom == null || nom.trim().isEmpty()) {
            throw new IllegalStateException("Le nom de la zone est obligatoire");
        }
        if (regionId == null) {
            throw new IllegalStateException("La zone doit être associée à une région");
        }
    }
}
