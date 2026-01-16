package com.cmci.cr.domain.model;

import lombok.Builder;
import lombok.Value;
import lombok.With;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entité EgliseLocale - Aggregate Root
 * Représente une église locale (500-3000 fidèles)
 * Rassemble plusieurs églises de maison
 */
@Value
@Builder
@With
public class EgliseLocale {
    UUID id;
    String nom;
    UUID zoneId;
    String adresse;
    UUID pasteurId;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;

    /**
     * Validation métier
     */
    public void validate() {
        if (nom == null || nom.trim().isEmpty()) {
            throw new IllegalStateException("Le nom de l'église locale est obligatoire");
        }
        if (zoneId == null) {
            throw new IllegalStateException("L'église locale doit être associée à une zone");
        }
    }

    /**
     * Vérifie si l'église a un pasteur assigné
     */
    public boolean hasPasteur() {
        return pasteurId != null;
    }

    /**
     * Assigne un pasteur à l'église
     */
    public EgliseLocale assignerPasteur(UUID pasteurId) {
        if (pasteurId == null) {
            throw new IllegalArgumentException("L'ID du pasteur ne peut pas être null");
        }
        return this.withPasteurId(pasteurId)
                .withUpdatedAt(LocalDateTime.now());
    }
}
