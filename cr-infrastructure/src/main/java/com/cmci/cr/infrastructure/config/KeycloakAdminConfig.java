package com.cmci.cr.infrastructure.config;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration pour le client admin Keycloak
 * Permet de gérer les utilisateurs et les rôles via l'API Admin
 *
 * Utilise les credentials admin pour avoir accès complet à la gestion des utilisateurs.
 * En production, utiliser un client service account avec les permissions appropriées.
 */
@Configuration
public class KeycloakAdminConfig {

    @Value("${keycloak.admin.server-url:http://localhost:8180}")
    private String serverUrl;

    @Value("${keycloak.admin.realm:cmci}")
    private String realm;

    @Value("${keycloak.admin.username:admin}")
    private String adminUsername;

    @Value("${keycloak.admin.password:admin123}")
    private String adminPassword;

    @Bean
    public Keycloak keycloakAdminClient() {
        return KeycloakBuilder.builder()
                .serverUrl(serverUrl)
                .realm("master")  // Admin se connecte au realm master
                .username(adminUsername)
                .password(adminPassword)
                .clientId("admin-cli")
                .build();
    }

    @Bean
    public String keycloakRealm() {
        return realm;
    }
}
