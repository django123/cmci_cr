package com.cmci.cr.domain.event;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Événement émis lors de la soumission d'un Compte Rendu
 */
@Value
@Builder
public class CRSubmittedEvent implements DomainEvent {
    UUID eventId;
    LocalDateTime occurredOn;

    // Données de l'événement
    UUID compteRenduId;
    UUID utilisateurId;
    UUID fdId; // FD à notifier
    LocalDate dateCR;

    @Override
    public String getEventType() {
        return "CR_SUBMITTED";
    }

    /**
     * Crée un nouvel événement de soumission de CR
     */
    public static CRSubmittedEvent of(
            UUID compteRenduId,
            UUID utilisateurId,
            UUID fdId,
            LocalDate dateCR
    ) {
        return CRSubmittedEvent.builder()
                .eventId(UUID.randomUUID())
                .occurredOn(LocalDateTime.now())
                .compteRenduId(compteRenduId)
                .utilisateurId(utilisateurId)
                .fdId(fdId)
                .dateCR(dateCR)
                .build();
    }
}
