package com.cmci.cr.application.dto;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;

/**
 * DTO de réponse pour les statistiques de groupe (US4.2 - FD/Leader)
 */
@Value
@Builder
public class GroupStatisticsResponse {
    LocalDate startDate;
    LocalDate endDate;

    // Informations groupe
    Long nombreMembres;

    // CR aujourd'hui
    Long nombreCRsAujourdhui;
    Double tauxSoumissionJour; // Pourcentage

    // CR sur la période
    Long totalCRsPeriode;
    Double tauxRegulariteGroupe; // Pourcentage moyen du groupe

    // Prière
    String dureeTotalePriere; // Format "HH:mm"
    String moyennePriereParMembre; // Format "HH:mm"

    // Membres en difficulté
    Long membresAvecAlerte; // Nombre de membres >= 3 jours sans CR
    Long membresInactifs; // Nombre de membres >= 7 jours sans CR

    // Top performers (optionnel)
    String meilleurDisciple; // Nom du disciple le plus régulier
    Double meilleurTaux; // Son taux de régularité
}
