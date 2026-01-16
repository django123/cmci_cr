package com.cmci.cr.domain.event;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Événement émis lorsqu'un CR est marqué comme vu par le FD
 */
@Value
@Builder
public class CRMarkedAsViewedEvent implements DomainEvent {
    UUID eventId;
    LocalDateTime occurredOn;

    // Données de l'événement
    UUID compteRenduId;
    UUID utilisateurId;
    UUID fdId; // FD qui a marqué comme vu
    LocalDate dateCR;

    @Override
    public String getEventType() {
        return "CR_MARKED_AS_VIEWED";
    }

    /**
     * Crée un nouvel événement de marquage comme vu
     */
    public static CRMarkedAsViewedEvent of(
            UUID compteRenduId,
            UUID utilisateurId,
            UUID fdId,
            LocalDate dateCR
    ) {
        return CRMarkedAsViewedEvent.builder()
                .eventId(UUID.randomUUID())
                .occurredOn(LocalDateTime.now())
                .compteRenduId(compteRenduId)
                .utilisateurId(utilisateurId)
                .fdId(fdId)
                .dateCR(dateCR)
                .build();
    }
}
