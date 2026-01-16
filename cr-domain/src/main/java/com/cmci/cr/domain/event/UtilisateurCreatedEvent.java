package com.cmci.cr.domain.event;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Événement émis lors de la création d'un nouvel utilisateur
 */
@Value
@Builder
public class UtilisateurCreatedEvent implements DomainEvent {
    UUID eventId;
    LocalDateTime occurredOn;

    // Données de l'événement
    UUID utilisateurId;
    String email;
    String nom;
    String prenom;
    String role;
    UUID fdId;
    UUID egliseMaisonId;

    @Override
    public String getEventType() {
        return "UTILISATEUR_CREATED";
    }

    /**
     * Crée un nouvel événement de création d'utilisateur
     */
    public static UtilisateurCreatedEvent of(
            UUID utilisateurId,
            String email,
            String nom,
            String prenom,
            String role,
            UUID fdId,
            UUID egliseMaisonId
    ) {
        return UtilisateurCreatedEvent.builder()
                .eventId(UUID.randomUUID())
                .occurredOn(LocalDateTime.now())
                .utilisateurId(utilisateurId)
                .email(email)
                .nom(nom)
                .prenom(prenom)
                .role(role)
                .fdId(fdId)
                .egliseMaisonId(egliseMaisonId)
                .build();
    }
}
