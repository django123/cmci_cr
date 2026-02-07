package com.cmci.cr.api.dto.response;

import com.cmci.cr.application.dto.response.KeycloakUserResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO de réponse API pour un utilisateur Keycloak
 */
@Value
@Builder
@Schema(description = "Utilisateur du système")
public class KeycloakUserApiResponse {

    @Schema(description = "ID unique de l'utilisateur", example = "d1000000-0000-0000-0000-000000000001")
    String id;

    @Schema(description = "Nom d'utilisateur", example = "jean.dupont@cmci.org")
    String username;

    @Schema(description = "Email", example = "jean.dupont@cmci.org")
    String email;

    @Schema(description = "Prénom", example = "Jean")
    String firstName;

    @Schema(description = "Nom de famille", example = "Dupont")
    String lastName;

    @Schema(description = "Nom complet", example = "Jean Dupont")
    String fullName;

    @Schema(description = "Compte actif", example = "true")
    boolean enabled;

    @Schema(description = "Email vérifié", example = "true")
    boolean emailVerified;

    @Schema(description = "Liste des rôles", example = "[\"FIDELE\"]")
    List<String> roles;

    @Schema(description = "Rôle principal", example = "FIDELE")
    String primaryRole;

    @Schema(description = "Date de création du compte")
    LocalDateTime createdAt;

    public static KeycloakUserApiResponse fromApplicationResponse(KeycloakUserResponse response) {
        return KeycloakUserApiResponse.builder()
                .id(response.getId())
                .username(response.getUsername())
                .email(response.getEmail())
                .firstName(response.getFirstName())
                .lastName(response.getLastName())
                .fullName(response.getFullName())
                .enabled(response.isEnabled())
                .emailVerified(response.isEmailVerified())
                .roles(response.getRoles())
                .primaryRole(response.getPrimaryRole())
                .createdAt(response.getCreatedAt())
                .build();
    }
}
