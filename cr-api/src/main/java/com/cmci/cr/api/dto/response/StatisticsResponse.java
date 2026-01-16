package com.cmci.cr.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO Response pour les statistiques
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatisticsResponse {

    private LocalDate startDate;
    private LocalDate endDate;
    private Integer totalCRSoumis;
    private Integer totalCRValides;
    private Double tauxCompletion;
    private Integer totalRDQDAccomplis;
    private Integer totalRDQDAttendus;
    private Double moyenneRDQD;
    private Integer totalPriereSeuleMinutes;
    private Integer totalPriereCoupleMinutes;
    private Integer totalPriereAvecEnfantsMinutes;
    private Integer totalTempsEtudeParoleMinutes;
    private Integer totalContactsUtiles;
    private Integer totalInvitationsCulte;
    private BigDecimal totalOffrandes;
    private Integer totalEvangelisations;
}
