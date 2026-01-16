package com.cmci.cr.domain.event;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Événement émis lors de l'ajout d'un commentaire sur un Compte Rendu
 */
@Value
@Builder
public class CommentaireAddedEvent implements DomainEvent {
    UUID eventId;
    LocalDateTime occurredOn;

    // Données de l'événement
    UUID commentaireId;
    UUID compteRenduId;
    UUID auteurId;
    UUID utilisateurConcerneId; // Propriétaire du CR à notifier
    String contenu;

    @Override
    public String getEventType() {
        return "COMMENTAIRE_ADDED";
    }

    /**
     * Crée un nouvel événement d'ajout de commentaire
     */
    public static CommentaireAddedEvent of(
            UUID commentaireId,
            UUID compteRenduId,
            UUID auteurId,
            UUID utilisateurConcerneId,
            String contenu
    ) {
        return CommentaireAddedEvent.builder()
                .eventId(UUID.randomUUID())
                .occurredOn(LocalDateTime.now())
                .commentaireId(commentaireId)
                .compteRenduId(compteRenduId)
                .auteurId(auteurId)
                .utilisateurConcerneId(utilisateurConcerneId)
                .contenu(contenu)
                .build();
    }
}
