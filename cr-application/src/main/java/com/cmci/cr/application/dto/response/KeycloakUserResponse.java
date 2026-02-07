package com.cmci.cr.application.dto.response;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

/**
 * DTO représentant un utilisateur Keycloak
 */
@Value
@Builder
public class KeycloakUserResponse {
    String id;
    String username;
    String email;
    String firstName;
    String lastName;
    boolean enabled;
    boolean emailVerified;
    List<String> roles;
    LocalDateTime createdAt;

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public String getPrimaryRole() {
        if (roles == null || roles.isEmpty()) {
            return "FIDELE";
        }
        // Retourne le rôle avec le niveau hiérarchique le plus élevé
        if (roles.contains("ADMIN")) return "ADMIN";
        if (roles.contains("PASTEUR")) return "PASTEUR";
        if (roles.contains("LEADER")) return "LEADER";
        if (roles.contains("FD")) return "FD";
        return "FIDELE";
    }

    public static KeycloakUserResponse fromIdentityUser(
            com.cmci.cr.domain.port.IdentityProviderPort.IdentityUser user) {
        LocalDateTime createdAt = null;
        if (user.createdTimestamp() != null) {
            createdAt = LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(user.createdTimestamp()),
                    ZoneId.systemDefault()
            );
        }

        return KeycloakUserResponse.builder()
                .id(user.id())
                .username(user.username())
                .email(user.email())
                .firstName(user.firstName())
                .lastName(user.lastName())
                .enabled(user.enabled())
                .emailVerified(user.emailVerified())
                .roles(user.roles())
                .createdAt(createdAt)
                .build();
    }
}
