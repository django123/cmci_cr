package com.cmci.cr;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Point d'entrée de l'application CMCI Compte Rendu
 *
 * Cette application permet aux disciples de gérer leurs comptes rendus quotidiens
 * et aux Faiseurs de Disciples (FD) de suivre leurs disciples.
 *
 * Architecture: Hexagonale (Ports & Adapters)
 * Modules:
 * - cr-domain: Entités et logique métier
 * - cr-application: Use Cases et DTOs
 * - cr-infrastructure: Persistence, Cache, Kafka, Security
 * - cr-api: REST Controllers
 * - cr-bootstrap: Point d'entrée Spring Boot
 */
@SpringBootApplication(scanBasePackages = "com.cmci.cr")
@EnableJpaRepositories(basePackages = "com.cmci.cr.infrastructure.persistence.repository")
@EnableCaching
public class CmciCrApplication {

    public static void main(String[] args) {
        SpringApplication.run(CmciCrApplication.class, args);
    }
}
