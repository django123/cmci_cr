package com.cmci.cr.application.dto.response;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO de réponse pour les statistiques d'un subordonné
 * Utilisé pour la visibilité hiérarchique (FD → Disciples, Leader → FD/Disciples, Pasteur → tous)
 */
@Value
@Builder
public class SubordinateStatisticsResponse {
    // Informations utilisateur
    UUID utilisateurId;
    String nom;
    String prenom;
    String nomComplet;
    String email;
    String role;
    String roleDisplayName;
    String avatarUrl;

    // Période
    LocalDate startDate;
    LocalDate endDate;

    // Statistiques CR
    Long nombreTotalCRs;
    Double tauxRegularite;

    // RDQD
    Long rdqdCompletCount;
    Double tauxRDQD;

    // Prière
    String dureeTotalePriere;
    String dureeMoyennePriere;

    // Lecture Biblique
    Integer totalChapitresLus;
    Double moyenneChapitresParJour;

    // Évangélisation
    Integer totalPersonnesEvangelisees;

    // Pratiques spirituelles
    Long nombreConfessions;
    Long nombreJeunes;

    // Indicateurs
    Boolean tendancePositive;
    String alertLevel; // NONE, WARNING, CRITICAL
    Boolean hasAlert;
}
