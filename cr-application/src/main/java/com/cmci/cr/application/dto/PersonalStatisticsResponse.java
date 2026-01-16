package com.cmci.cr.application.dto;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;

/**
 * DTO de réponse pour les statistiques personnelles (US4.1)
 */
@Value
@Builder
public class PersonalStatisticsResponse {
    LocalDate startDate;
    LocalDate endDate;

    // Statistiques CR
    Long nombreTotalCRs;
    Double tauxRegularite; // Pourcentage

    // RDQD
    Long rdqdCompletCount;
    Double tauxRDQD; // Pourcentage

    // Prière
    String dureeTotalePriere; // Format "HH:mm"
    String dureeMoyennePriere; // Format "HH:mm"

    // Lecture Biblique
    Integer totalChapitresLus;
    Double moyenneChapitresParJour;

    // Évangélisation
    Integer totalPersonnesEvangelisees;

    // Pratiques spirituelles
    Long nombreConfessions;
    Long nombreJeunes;

    // Tendances (optionnel pour graph)
    Boolean tendancePositive; // true si taux de régularité en progression
}
