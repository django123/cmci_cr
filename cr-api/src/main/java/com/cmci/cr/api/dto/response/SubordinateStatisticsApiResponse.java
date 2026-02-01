package com.cmci.cr.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO API Response pour les statistiques d'un subordonné
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubordinateStatisticsApiResponse {
    // Informations utilisateur
    private UUID utilisateurId;
    private String nom;
    private String prenom;
    private String nomComplet;
    private String email;
    private String role;
    private String roleDisplayName;
    private String avatarUrl;

    // Période
    private LocalDate startDate;
    private LocalDate endDate;

    // Statistiques CR
    private Long nombreTotalCRs;
    private Double tauxRegularite;

    // RDQD
    private Long rdqdCompletCount;
    private Double tauxRDQD;

    // Prière
    private String dureeTotalePriere;
    private String dureeMoyennePriere;

    // Lecture Biblique
    private Integer totalChapitresLus;
    private Double moyenneChapitresParJour;

    // Évangélisation
    private Integer totalPersonnesEvangelisees;

    // Pratiques spirituelles
    private Long nombreConfessions;
    private Long nombreJeunes;

    // Indicateurs
    private Boolean tendancePositive;
    private String alertLevel;
    private Boolean hasAlert;
}
