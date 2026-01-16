package com.cmci.cr.domain.model;

import lombok.Builder;
import lombok.Value;
import lombok.With;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entité EgliseMaison - Aggregate Root
 * Représente une église de maison (10-100 fidèles)
 * Petite communauté rattachée à une église locale
 */
@Value
@Builder
@With
public class EgliseMaison {
    UUID id;
    String nom;
    UUID egliseLocaleId;
    UUID leaderId;
    String adresse;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;

    /**
     * Validation métier
     */
    public void validate() {
        if (nom == null || nom.trim().isEmpty()) {
            throw new IllegalStateException("Le nom de l'église de maison est obligatoire");
        }
        if (egliseLocaleId == null) {
            throw new IllegalStateException("L'église de maison doit être associée à une église locale");
        }
    }

    /**
     * Vérifie si l'église a un leader assigné
     */
    public boolean hasLeader() {
        return leaderId != null;
    }

    /**
     * Assigne un leader à l'église
     */
    public EgliseMaison assignerLeader(UUID leaderId) {
        if (leaderId == null) {
            throw new IllegalArgumentException("L'ID du leader ne peut pas être null");
        }
        return this.withLeaderId(leaderId)
                .withUpdatedAt(LocalDateTime.now());
    }
}
