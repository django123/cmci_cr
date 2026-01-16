package com.cmci.cr.api.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuration OpenAPI/Swagger pour la documentation de l'API
 */
@Configuration
public class OpenApiConfig {

    @Value("${app.api.version:1.0.0}")
    private String apiVersion;

    @Value("${app.api.title:CMCI Compte Rendu API}")
    private String apiTitle;

    @Value("${app.api.description:API de gestion des comptes rendus quotidiens pour les disciples}")
    private String apiDescription;

    @Value("${server.port:8081}")
    private String serverPort;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title(apiTitle)
                        .version(apiVersion)
                        .description(apiDescription)
                        .contact(new Contact()
                                .name("CMCI Team")
                                .email("tech@cmci.org")
                                .url("https://cmci.org"))
                        .license(new License()
                                .name("Proprietary")
                                .url("https://cmci.org/license")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:" + serverPort)
                                .description("Serveur de développement local"),
                        new Server()
                                .url("https://api.cmci.org")
                                .description("Serveur de production")))
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("JWT token obtenu depuis Keycloak")));
    }
}
