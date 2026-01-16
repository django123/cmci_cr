package com.cmci.cr.infrastructure.event;

import com.cmci.cr.domain.event.*;
import com.cmci.cr.infrastructure.BaseIntegrationTest;
import com.cmci.cr.infrastructure.config.KafkaTopics;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/**
 * Tests d'intégration pour la publication d'événements Kafka
 */
class KafkaEventPublisherIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private KafkaEventPublisher kafkaEventPublisher;

    private KafkaConsumer<String, DomainEvent> consumer;

    @BeforeEach
    void setUp() {
        // Configure consumer
        Map<String, Object> consumerProps = new HashMap<>();
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                KAFKA_CONTAINER.getBootstrapServers());
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "test-consumer-group");
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                JsonDeserializer.class);
        consumerProps.put(JsonDeserializer.TRUSTED_PACKAGES, "com.cmci.cr.domain.event");
        consumerProps.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);
        consumerProps.put(JsonDeserializer.VALUE_DEFAULT_TYPE,
                "com.cmci.cr.domain.event.DomainEvent");

        consumer = new KafkaConsumer<>(consumerProps);
    }

    @AfterEach
    void tearDown() {
        if (consumer != null) {
            consumer.close();
        }
    }

    @Test
    @DisplayName("Devrait publier un événement CRCreatedEvent vers Kafka")
    void shouldPublishCRCreatedEvent() {
        // Given
        UUID compteRenduId = UUID.randomUUID();
        UUID utilisateurId = UUID.randomUUID();

        CRCreatedEvent event = CRCreatedEvent.builder()
                .compteRenduId(compteRenduId)
                .utilisateurId(utilisateurId)
                .occurredAt(LocalDateTime.now())
                .build();

        consumer.subscribe(Collections.singletonList(KafkaTopics.CR_EVENTS));

        // When
        kafkaEventPublisher.publish(event);

        // Then
        await().atMost(Duration.ofSeconds(10))
                .untilAsserted(() -> {
                    ConsumerRecords<String, DomainEvent> records =
                            consumer.poll(Duration.ofMillis(100));

                    assertThat(records).isNotEmpty();

                    boolean eventFound = false;
                    for (ConsumerRecord<String, DomainEvent> record : records) {
                        if (record.value() instanceof CRCreatedEvent receivedEvent) {
                            assertThat(receivedEvent.getCompteRenduId()).isEqualTo(compteRenduId);
                            assertThat(receivedEvent.getUtilisateurId()).isEqualTo(utilisateurId);
                            eventFound = true;
                            break;
                        }
                    }
                    assertThat(eventFound).isTrue();
                });
    }

    @Test
    @DisplayName("Devrait publier un événement CRSubmittedEvent vers Kafka")
    void shouldPublishCRSubmittedEvent() {
        // Given
        UUID compteRenduId = UUID.randomUUID();
        UUID utilisateurId = UUID.randomUUID();

        CRSubmittedEvent event = CRSubmittedEvent.builder()
                .compteRenduId(compteRenduId)
                .utilisateurId(utilisateurId)
                .occurredAt(LocalDateTime.now())
                .build();

        consumer.subscribe(Collections.singletonList(KafkaTopics.CR_EVENTS));

        // When
        kafkaEventPublisher.publish(event);

        // Then
        await().atMost(Duration.ofSeconds(10))
                .untilAsserted(() -> {
                    ConsumerRecords<String, DomainEvent> records =
                            consumer.poll(Duration.ofMillis(100));

                    assertThat(records).isNotEmpty();
                });
    }

    @Test
    @DisplayName("Devrait publier un événement CommentaireAddedEvent vers Kafka")
    void shouldPublishCommentaireAddedEvent() {
        // Given
        UUID commentaireId = UUID.randomUUID();
        UUID compteRenduId = UUID.randomUUID();
        UUID auteurId = UUID.randomUUID();

        CommentaireAddedEvent event = CommentaireAddedEvent.builder()
                .commentaireId(commentaireId)
                .compteRenduId(compteRenduId)
                .auteurId(auteurId)
                .occurredAt(LocalDateTime.now())
                .build();

        consumer.subscribe(Collections.singletonList(KafkaTopics.COMMENTAIRE_EVENTS));

        // When
        kafkaEventPublisher.publish(event);

        // Then
        await().atMost(Duration.ofSeconds(10))
                .untilAsserted(() -> {
                    ConsumerRecords<String, DomainEvent> records =
                            consumer.poll(Duration.ofMillis(100));

                    assertThat(records).isNotEmpty();

                    boolean eventFound = false;
                    for (ConsumerRecord<String, DomainEvent> record : records) {
                        if (record.value() instanceof CommentaireAddedEvent receivedEvent) {
                            assertThat(receivedEvent.getCommentaireId()).isEqualTo(commentaireId);
                            assertThat(receivedEvent.getCompteRenduId()).isEqualTo(compteRenduId);
                            assertThat(receivedEvent.getAuteurId()).isEqualTo(auteurId);
                            eventFound = true;
                            break;
                        }
                    }
                    assertThat(eventFound).isTrue();
                });
    }

    @Test
    @DisplayName("Devrait utiliser l'ID de l'agrégat comme clé de partition")
    void shouldUseAggregateIdAsPartitionKey() {
        // Given
        UUID compteRenduId = UUID.randomUUID();
        UUID utilisateurId = UUID.randomUUID();

        CRValidatedEvent event = CRValidatedEvent.builder()
                .compteRenduId(compteRenduId)
                .utilisateurId(utilisateurId)
                .validatedBy(UUID.randomUUID())
                .occurredAt(LocalDateTime.now())
                .build();

        consumer.subscribe(Collections.singletonList(KafkaTopics.CR_EVENTS));

        // When
        kafkaEventPublisher.publish(event);

        // Then
        await().atMost(Duration.ofSeconds(10))
                .untilAsserted(() -> {
                    ConsumerRecords<String, DomainEvent> records =
                            consumer.poll(Duration.ofMillis(100));

                    assertThat(records).isNotEmpty();

                    for (ConsumerRecord<String, DomainEvent> record : records) {
                        if (record.value() instanceof CRValidatedEvent) {
                            // Verify the key is the aggregateId
                            assertThat(record.key()).isEqualTo(compteRenduId.toString());
                            break;
                        }
                    }
                });
    }
}
