package com.cmci.cr.application.dto.response;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * DTO de réponse pour un subordonné avec ses CR
 * Utilisé pour la visibilité hiérarchique (FD → Disciples, Leader → FD/Disciples, Pasteur → tous)
 */
@Value
@Builder
public class SubordinateWithCRsResponse {
    UUID utilisateurId;
    String nom;
    String prenom;
    String nomComplet;
    String email;
    String role;
    String roleDisplayName;
    String avatarUrl;

    // Statistiques CR
    LocalDate lastCRDate;
    Integer daysSinceLastCR;
    Double regularityRate;
    Integer totalCRs;

    // Indicateurs d'alerte
    String alertLevel; // NONE, WARNING (3-7 jours), CRITICAL (>7 jours ou jamais)
    Boolean hasAlert;

    // Liste des CR
    List<SubordinateCRResponse> compteRendus;
}
