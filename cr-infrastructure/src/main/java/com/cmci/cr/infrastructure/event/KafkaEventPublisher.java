package com.cmci.cr.infrastructure.event;

import com.cmci.cr.domain.event.*;
import com.cmci.cr.infrastructure.config.KafkaTopics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Publisher d'événements de domaine vers Kafka
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaEventPublisher {

    private final KafkaTemplate<String, DomainEvent> kafkaTemplate;

    /**
     * Publie un événement de domaine vers le topic approprié
     */
    public void publish(DomainEvent event) {
        String topic = determineTopicForEvent(event);
        String key = extractKeyFromEvent(event);

        log.debug("Publishing event {} to topic {} with key {}",
                event.getClass().getSimpleName(), topic, key);

        CompletableFuture<SendResult<String, DomainEvent>> future =
                kafkaTemplate.send(topic, key, event);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Event {} published successfully to topic {} at offset {}",
                        event.getClass().getSimpleName(),
                        topic,
                        result.getRecordMetadata().offset());
            } else {
                log.error("Failed to publish event {} to topic {}: {}",
                        event.getClass().getSimpleName(),
                        topic,
                        ex.getMessage(),
                        ex);
            }
        });
    }

    /**
     * Détermine le topic Kafka en fonction du type d'événement
     */
    private String determineTopicForEvent(DomainEvent event) {
        if (event instanceof CRCreatedEvent ||
            event instanceof CRSubmittedEvent ||
            event instanceof CRValidatedEvent ||
            event instanceof CRMarkedAsViewedEvent) {
            return KafkaTopics.CR_EVENTS;
        } else if (event instanceof CommentaireAddedEvent) {
            return KafkaTopics.COMMENTAIRE_EVENTS;
        } else {
            log.warn("Unknown event type {}, using default topic",
                    event.getClass().getSimpleName());
            return KafkaTopics.CR_EVENTS;
        }
    }

    /**
     * Extrait la clé de partitionnement de l'événement
     * Utilise l'ID de l'agrégat pour garantir l'ordre des événements
     */
    private String extractKeyFromEvent(DomainEvent event) {
        UUID aggregateId = null;

        if (event instanceof CRCreatedEvent crEvent) {
            aggregateId = crEvent.getCompteRenduId();
        } else if (event instanceof CRSubmittedEvent crEvent) {
            aggregateId = crEvent.getCompteRenduId();
        } else if (event instanceof CRValidatedEvent crEvent) {
            aggregateId = crEvent.getCompteRenduId();
        } else if (event instanceof CRMarkedAsViewedEvent crEvent) {
            aggregateId = crEvent.getCompteRenduId();
        } else if (event instanceof CommentaireAddedEvent commentEvent) {
            aggregateId = commentEvent.getCommentaireId();
        }

        return aggregateId != null ? aggregateId.toString() : UUID.randomUUID().toString();
    }
}
