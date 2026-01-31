package com.cmci.cr.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * DTO API Response pour un subordonné avec ses CR
 * Utilisé pour la visibilité hiérarchique (FD → Disciples, Leader → FD/Disciples, Pasteur → tous)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubordinateWithCRsApiResponse {
    private UUID utilisateurId;
    private String nom;
    private String prenom;
    private String nomComplet;
    private String email;
    private String role;
    private String roleDisplayName;
    private String avatarUrl;

    // Statistiques CR
    private LocalDate lastCRDate;
    private Integer daysSinceLastCR;
    private Double regularityRate;
    private Integer totalCRs;

    // Indicateurs d'alerte
    private String alertLevel;
    private Boolean hasAlert;

    // Liste des CR
    private List<SubordinateCRApiResponse> compteRendus;
}
