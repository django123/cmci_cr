package com.cmci.cr.domain.event;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Événement émis lors de la validation d'un Compte Rendu par un FD
 */
@Value
@Builder
public class CRValidatedEvent implements DomainEvent {
    UUID eventId;
    LocalDateTime occurredOn;

    // Données de l'événement
    UUID compteRenduId;
    UUID utilisateurId;
    UUID validatedByFdId;
    LocalDate dateCR;

    @Override
    public String getEventType() {
        return "CR_VALIDATED";
    }

    /**
     * Crée un nouvel événement de validation de CR
     */
    public static CRValidatedEvent of(
            UUID compteRenduId,
            UUID utilisateurId,
            UUID validatedByFdId,
            LocalDate dateCR
    ) {
        return CRValidatedEvent.builder()
                .eventId(UUID.randomUUID())
                .occurredOn(LocalDateTime.now())
                .compteRenduId(compteRenduId)
                .utilisateurId(utilisateurId)
                .validatedByFdId(validatedByFdId)
                .dateCR(dateCR)
                .build();
    }
}
