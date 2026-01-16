package com.cmci.cr.application.dto.response;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO de réponse pour un Commentaire
 */
@Value
@Builder
public class CommentaireResponse {
    UUID id;
    UUID compteRenduId;
    UUID auteurId;
    String auteurNom; // Nom complet de l'auteur
    String contenu;
    LocalDateTime createdAt;
}
