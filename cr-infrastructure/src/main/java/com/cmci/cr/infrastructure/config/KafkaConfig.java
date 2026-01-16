package com.cmci.cr.infrastructure.config;

import com.cmci.cr.domain.event.DomainEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration Kafka pour la publication d'événements de domaine
 */
@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;

    @Value("${spring.kafka.producer.client-id:cmci-cr-producer}")
    private String clientId;

    /**
     * ObjectMapper pour la sérialisation JSON des événements
     */
    @Bean
    public ObjectMapper kafkaObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.findAndRegisterModules();
        return mapper;
    }

    /**
     * Configuration du Producer Kafka
     */
    @Bean
    public ProducerFactory<String, DomainEvent> producerFactory(ObjectMapper kafkaObjectMapper) {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.CLIENT_ID_CONFIG, clientId);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        // Configuration pour améliorer la fiabilité
        configProps.put(ProducerConfig.ACKS_CONFIG, "all"); // Attendre confirmation de tous les brokers
        configProps.put(ProducerConfig.RETRIES_CONFIG, 3); // Nombre de tentatives en cas d'échec
        configProps.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 1); // Garantir l'ordre
        configProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true); // Éviter les doublons

        // Configuration du sérializer JSON
        configProps.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);

        DefaultKafkaProducerFactory<String, DomainEvent> factory =
                new DefaultKafkaProducerFactory<>(configProps);
        factory.setValueSerializer(new JsonSerializer<>(kafkaObjectMapper));

        return factory;
    }

    /**
     * KafkaTemplate pour envoyer les événements
     */
    @Bean
    public KafkaTemplate<String, DomainEvent> kafkaTemplate(
            ProducerFactory<String, DomainEvent> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

    /**
     * Topic pour les événements de Compte Rendu
     */
    @Bean
    public NewTopic crEventsTopic() {
        return TopicBuilder.name(KafkaTopics.CR_EVENTS)
                .partitions(3)
                .replicas(1)
                .config("retention.ms", "604800000") // 7 jours
                .config("compression.type", "snappy")
                .build();
    }

    /**
     * Topic pour les événements de Commentaire
     */
    @Bean
    public NewTopic commentaireEventsTopic() {
        return TopicBuilder.name(KafkaTopics.COMMENTAIRE_EVENTS)
                .partitions(2)
                .replicas(1)
                .config("retention.ms", "604800000") // 7 jours
                .config("compression.type", "snappy")
                .build();
    }

    /**
     * Topic pour les événements d'Utilisateur
     */
    @Bean
    public NewTopic utilisateurEventsTopic() {
        return TopicBuilder.name(KafkaTopics.UTILISATEUR_EVENTS)
                .partitions(2)
                .replicas(1)
                .config("retention.ms", "2592000000") // 30 jours
                .config("compression.type", "snappy")
                .build();
    }

    /**
     * Topic pour les notifications
     */
    @Bean
    public NewTopic notificationsTopic() {
        return TopicBuilder.name(KafkaTopics.NOTIFICATIONS)
                .partitions(3)
                .replicas(1)
                .config("retention.ms", "86400000") // 1 jour
                .config("compression.type", "snappy")
                .build();
    }
}
