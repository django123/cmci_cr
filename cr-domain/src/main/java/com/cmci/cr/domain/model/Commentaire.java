package com.cmci.cr.domain.model;

import lombok.Builder;
import lombok.Value;
import lombok.With;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entité Commentaire - Value Entity
 * Représente un commentaire d'un FD/Leader/Pasteur sur un Compte Rendu
 */
@Value
@Builder
@With
public class Commentaire {
    UUID id;
    UUID compteRenduId;
    UUID auteurId;
    String contenu;
    LocalDateTime createdAt;

    /**
     * Validation métier
     */
    public void validate() {
        if (compteRenduId == null) {
            throw new IllegalStateException("Le commentaire doit être associé à un compte rendu");
        }
        if (auteurId == null) {
            throw new IllegalStateException("Le commentaire doit avoir un auteur");
        }
        if (contenu == null || contenu.trim().isEmpty()) {
            throw new IllegalStateException("Le contenu du commentaire ne peut pas être vide");
        }
        if (contenu.length() > 5000) {
            throw new IllegalStateException("Le commentaire ne peut pas dépasser 5000 caractères");
        }
    }

    /**
     * Vérifie si le commentaire appartient à un utilisateur spécifique
     */
    public boolean isAuthoredBy(UUID utilisateurId) {
        return this.auteurId.equals(utilisateurId);
    }

    /**
     * Obtient un aperçu du commentaire (premiers 100 caractères)
     */
    public String getApercu() {
        if (contenu.length() <= 100) {
            return contenu;
        }
        return contenu.substring(0, 97) + "...";
    }
}
