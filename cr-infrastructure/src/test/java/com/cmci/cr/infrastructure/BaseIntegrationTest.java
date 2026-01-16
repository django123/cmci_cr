package com.cmci.cr.infrastructure;

import org.junit.jupiter.api.BeforeAll;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

/**
 * Classe de base pour les tests d'intégration avec Testcontainers
 */
@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
public abstract class BaseIntegrationTest {

    // PostgreSQL Container
    static final PostgreSQLContainer<?> POSTGRES_CONTAINER =
            new PostgreSQLContainer<>(DockerImageName.parse("postgres:16-alpine"))
                    .withDatabaseName("cmci_cr_test")
                    .withUsername("test")
                    .withPassword("test")
                    .withReuse(true);

    // Redis Container
    static final GenericContainer<?> REDIS_CONTAINER =
            new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
                    .withExposedPorts(6379)
                    .withReuse(true);

    // Kafka Container
    static final KafkaContainer KAFKA_CONTAINER =
            new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.6.0"))
                    .withReuse(true);

    @BeforeAll
    static void startContainers() {
        POSTGRES_CONTAINER.start();
        REDIS_CONTAINER.start();
        KAFKA_CONTAINER.start();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        // PostgreSQL properties
        registry.add("spring.datasource.url", POSTGRES_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES_CONTAINER::getUsername);
        registry.add("spring.datasource.password", POSTGRES_CONTAINER::getPassword);

        // Redis properties
        registry.add("spring.data.redis.host", REDIS_CONTAINER::getHost);
        registry.add("spring.data.redis.port", () -> REDIS_CONTAINER.getMappedPort(6379));

        // Kafka properties
        registry.add("spring.kafka.bootstrap-servers", KAFKA_CONTAINER::getBootstrapServers);
    }
}
