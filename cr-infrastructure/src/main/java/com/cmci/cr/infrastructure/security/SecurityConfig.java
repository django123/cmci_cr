package com.cmci.cr.infrastructure.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Configuration de la sécurité avec OAuth2 et Keycloak
 * Temporairement désactivée pour le développement
 */
// @Configuration
// @EnableWebSecurity
// @EnableMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
public class SecurityConfig {

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String issuerUri;

    @Value("${app.security.cors.allowed-origins:http://localhost:3000}")
    private String[] allowedOrigins;

    /**
     * Configuration de la chaîne de filtres de sécurité
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Désactiver CSRF car on utilise JWT (stateless)
                .csrf(AbstractHttpConfigurer::disable)

                // Configuration CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // Politique de session stateless (JWT)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Configuration des autorisations
                .authorizeHttpRequests(authorize -> authorize
                        // Endpoints publics (health check, metrics pour Prometheus)
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                        .requestMatchers("/actuator/prometheus").permitAll()

                        // Swagger/OpenAPI endpoints
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()

                        // API endpoints - Authentication requise
                        .requestMatchers(HttpMethod.GET, "/api/v1/cr/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/v1/cr/**").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/v1/cr/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/cr/**").authenticated()

                        .requestMatchers(HttpMethod.GET, "/api/v1/utilisateurs/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/v1/utilisateurs/**").hasAnyRole("ADMIN", "PASTEUR")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/utilisateurs/**").hasAnyRole("ADMIN", "PASTEUR")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/utilisateurs/**").hasRole("ADMIN")

                        // Tous les autres endpoints nécessitent une authentification
                        .anyRequest().authenticated()
                )

                // Configuration OAuth2 Resource Server avec JWT
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .jwtAuthenticationConverter(jwtAuthenticationConverter())
                        )
                );

        return http.build();
    }

    /**
     * Configuration CORS pour autoriser les appels depuis le frontend
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(allowedOrigins));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(List.of("Authorization", "Content-Type"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        return source;
    }

    /**
     * Décodeur JWT configuré avec l'URI de l'émetteur Keycloak
     */
    @Bean
    public JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withIssuerLocation(issuerUri).build();
    }

    /**
     * Convertisseur pour extraire les rôles depuis les claims JWT de Keycloak
     */
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter =
                new JwtGrantedAuthoritiesConverter();

        // Keycloak utilise "realm_access.roles" pour les rôles
        grantedAuthoritiesConverter.setAuthoritiesClaimName("realm_access.roles");
        grantedAuthoritiesConverter.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter jwtAuthenticationConverter =
                new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);

        return jwtAuthenticationConverter;
    }
}
