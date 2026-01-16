package com.cmci.cr.application.dto.response;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO de réponse pour un disciple avec son statut de CR
 * Utilisé pour le tableau de bord FD (US3.1)
 */
@Value
@Builder
public class DiscipleWithCRStatusResponse {
    UUID discipleId;
    String nom;
    String prenom;
    String nomComplet;
    String email;
    String avatarUrl;

    // Statut CR
    LocalDate dernierCRDate;
    Boolean crAujourdhui;
    Integer joursDepuisDernierCR;
    Double tauxRegularite30j; // Pourcentage sur les 30 derniers jours

    // Indicateurs d'alerte
    Boolean alerte; // true si >= 3 jours sans CR
    String niveauAlerte; // NONE, WARNING (3-7 jours), CRITICAL (>7 jours)
}
