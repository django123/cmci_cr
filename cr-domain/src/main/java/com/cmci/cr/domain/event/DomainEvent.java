package com.cmci.cr.domain.event;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Interface de base pour tous les événements du domaine
 */
public interface DomainEvent {

    /**
     * Identifiant unique de l'événement
     */
    UUID getEventId();

    /**
     * Date et heure de l'événement
     */
    LocalDateTime getOccurredOn();

    /**
     * Type de l'événement
     */
    String getEventType();
}
