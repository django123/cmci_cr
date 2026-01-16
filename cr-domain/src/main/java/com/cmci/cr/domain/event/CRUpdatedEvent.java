package com.cmci.cr.domain.event;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Événement émis lors de la modification d'un Compte Rendu
 */
@Value
@Builder
public class CRUpdatedEvent implements DomainEvent {
    UUID eventId;
    LocalDateTime occurredOn;

    // Données de l'événement
    UUID compteRenduId;
    UUID utilisateurId;
    UUID fdId;
    LocalDate dateCR;
    String nouveauStatut;
    String ancienStatut;

    @Override
    public String getEventType() {
        return "CR_UPDATED";
    }

    /**
     * Crée un nouvel événement de mise à jour de CR
     */
    public static CRUpdatedEvent of(
            UUID compteRenduId,
            UUID utilisateurId,
            UUID fdId,
            LocalDate dateCR,
            String nouveauStatut,
            String ancienStatut
    ) {
        return CRUpdatedEvent.builder()
                .eventId(UUID.randomUUID())
                .occurredOn(LocalDateTime.now())
                .compteRenduId(compteRenduId)
                .utilisateurId(utilisateurId)
                .fdId(fdId)
                .dateCR(dateCR)
                .nouveauStatut(nouveauStatut)
                .ancienStatut(ancienStatut)
                .build();
    }
}
