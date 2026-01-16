package com.cmci.cr.application.dto.response;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO de réponse pour un utilisateur
 */
@Value
@Builder
public class UtilisateurResponse {
    UUID id;
    String email;
    String nom;
    String prenom;
    String nomComplet;
    String role;
    UUID egliseMaisonId;
    UUID fdId;
    String fdNom; // Nom complet du FD
    String avatarUrl;
    String telephone;
    LocalDate dateNaissance;
    LocalDate dateBapteme;
    String statut;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
