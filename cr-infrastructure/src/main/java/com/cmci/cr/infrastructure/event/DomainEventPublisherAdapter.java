package com.cmci.cr.infrastructure.event;

import com.cmci.cr.domain.event.DomainEvent;
import com.cmci.cr.domain.event.DomainEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Adaptateur qui implémente le port DomainEventPublisher
 * en utilisant Kafka comme système de messagerie
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DomainEventPublisherAdapter implements DomainEventPublisher {

    private final KafkaEventPublisher kafkaEventPublisher;

    @Override
    public void publish(DomainEvent event) {
        log.debug("Publishing domain event: {}", event.getClass().getSimpleName());
        kafkaEventPublisher.publish(event);
    }
}
