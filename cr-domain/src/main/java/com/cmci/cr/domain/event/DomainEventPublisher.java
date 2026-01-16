package com.cmci.cr.domain.event;

/**
 * Port pour publier des événements de domaine
 * Implémenté par l'infrastructure (Kafka, etc.)
 */
public interface DomainEventPublisher {

    /**
     * Publie un événement de domaine
     *
     * @param event l'événement à publier
     */
    void publish(DomainEvent event);
}
