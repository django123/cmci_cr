package com.cmci.cr.api.dto.response;

import com.cmci.cr.application.dto.response.UtilisateurResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO de réponse API pour un disciple
 */
@Value
@Builder
@Schema(description = "Informations d'un disciple")
public class DiscipleApiResponse {

    @Schema(description = "ID unique du disciple")
    UUID id;

    @Schema(description = "Email du disciple")
    String email;

    @Schema(description = "Nom de famille")
    String nom;

    @Schema(description = "Prénom")
    String prenom;

    @Schema(description = "Nom complet")
    String nomComplet;

    @Schema(description = "Rôle dans l'église", example = "FIDELE")
    String role;

    @Schema(description = "ID de l'église de maison")
    UUID egliseMaisonId;

    @Schema(description = "ID du FD assigné")
    UUID fdId;

    @Schema(description = "Nom complet du FD assigné")
    String fdNom;

    @Schema(description = "URL de l'avatar")
    String avatarUrl;

    @Schema(description = "Numéro de téléphone")
    String telephone;

    @Schema(description = "Date de naissance")
    LocalDate dateNaissance;

    @Schema(description = "Date de baptême")
    LocalDate dateBapteme;

    @Schema(description = "Statut du compte", example = "ACTIF")
    String statut;

    @Schema(description = "Date de création")
    LocalDateTime createdAt;

    @Schema(description = "Date de dernière mise à jour")
    LocalDateTime updatedAt;

    public static DiscipleApiResponse fromApplicationResponse(UtilisateurResponse response) {
        return DiscipleApiResponse.builder()
                .id(response.getId())
                .email(response.getEmail())
                .nom(response.getNom())
                .prenom(response.getPrenom())
                .nomComplet(response.getNomComplet())
                .role(response.getRole())
                .egliseMaisonId(response.getEgliseMaisonId())
                .fdId(response.getFdId())
                .fdNom(response.getFdNom())
                .avatarUrl(response.getAvatarUrl())
                .telephone(response.getTelephone())
                .dateNaissance(response.getDateNaissance())
                .dateBapteme(response.getDateBapteme())
                .statut(response.getStatut())
                .createdAt(response.getCreatedAt())
                .updatedAt(response.getUpdatedAt())
                .build();
    }
}
