package com.cmci.cr.domain.event;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Événement émis lors de la création d'un Compte Rendu
 */
@Value
@Builder
public class CRCreatedEvent implements DomainEvent {
    UUID eventId;
    LocalDateTime occurredOn;

    // Données de l'événement
    UUID compteRenduId;
    UUID utilisateurId;
    UUID fdId; // FD à notifier
    LocalDate dateCR;
    String rdqd;
    String statut;

    @Override
    public String getEventType() {
        return "CR_CREATED";
    }

    /**
     * Crée un nouvel événement de création de CR
     */
    public static CRCreatedEvent of(
            UUID compteRenduId,
            UUID utilisateurId,
            UUID fdId,
            LocalDate dateCR,
            String rdqd,
            String statut
    ) {
        return CRCreatedEvent.builder()
                .eventId(UUID.randomUUID())
                .occurredOn(LocalDateTime.now())
                .compteRenduId(compteRenduId)
                .utilisateurId(utilisateurId)
                .fdId(fdId)
                .dateCR(dateCR)
                .rdqd(rdqd)
                .statut(statut)
                .build();
    }
}
