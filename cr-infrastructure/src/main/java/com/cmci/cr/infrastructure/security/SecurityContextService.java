package com.cmci.cr.infrastructure.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

/**
 * Service utilitaire pour accéder au contexte de sécurité
 */
@Service
public class SecurityContextService {

    /**
     * Récupère l'utilisateur actuellement authentifié
     */
    public Optional<Authentication> getCurrentAuthentication() {
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication());
    }

    /**
     * Récupère l'email de l'utilisateur courant depuis le JWT
     */
    public Optional<String> getCurrentUserEmail() {
        return getCurrentAuthentication()
                .filter(auth -> auth.getPrincipal() instanceof Jwt)
                .map(auth -> (Jwt) auth.getPrincipal())
                .map(jwt -> jwt.getClaimAsString("email"));
    }

    /**
     * Récupère le sub (subject) du JWT, qui correspond généralement à l'ID utilisateur Keycloak
     */
    public Optional<String> getCurrentUserSubject() {
        return getCurrentAuthentication()
                .filter(auth -> auth.getPrincipal() instanceof Jwt)
                .map(auth -> (Jwt) auth.getPrincipal())
                .map(Jwt::getSubject);
    }

    /**
     * Récupère l'UUID de l'utilisateur courant depuis le claim "user_id"
     * Keycloak peut être configuré pour inclure ce claim personnalisé
     */
    public Optional<UUID> getCurrentUserId() {
        return getCurrentAuthentication()
                .filter(auth -> auth.getPrincipal() instanceof Jwt)
                .map(auth -> (Jwt) auth.getPrincipal())
                .map(jwt -> jwt.getClaimAsString("user_id"))
                .map(UUID::fromString);
    }

    /**
     * Vérifie si l'utilisateur courant a un rôle spécifique
     */
    public boolean hasRole(String role) {
        return getCurrentAuthentication()
                .map(auth -> auth.getAuthorities().stream()
                        .anyMatch(authority -> authority.getAuthority().equals("ROLE_" + role)))
                .orElse(false);
    }

    /**
     * Vérifie si l'utilisateur courant a l'un des rôles spécifiés
     */
    public boolean hasAnyRole(String... roles) {
        for (String role : roles) {
            if (hasRole(role)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Vérifie si l'utilisateur est authentifié
     */
    public boolean isAuthenticated() {
        return getCurrentAuthentication()
                .map(Authentication::isAuthenticated)
                .orElse(false);
    }

    /**
     * Récupère le JWT complet
     */
    public Optional<Jwt> getCurrentJwt() {
        return getCurrentAuthentication()
                .filter(auth -> auth.getPrincipal() instanceof Jwt)
                .map(auth -> (Jwt) auth.getPrincipal());
    }
}
