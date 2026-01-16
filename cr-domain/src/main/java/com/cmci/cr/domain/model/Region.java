package com.cmci.cr.domain.model;

import lombok.Builder;
import lombok.Value;
import lombok.With;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entité Region - Aggregate Root
 * Représente une région géographique (Continent/Zone géographique)
 */
@Value
@Builder
@With
public class Region {
    UUID id;
    String nom;
    String code;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;

    /**
     * Validation métier
     */
    public void validate() {
        if (nom == null || nom.trim().isEmpty()) {
            throw new IllegalStateException("Le nom de la région est obligatoire");
        }
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalStateException("Le code de la région est obligatoire");
        }
        if (code.length() > 10) {
            throw new IllegalStateException("Le code de la région ne peut pas dépasser 10 caractères");
        }
    }
}
