package com.cmci.cr.application.usecase;

import com.cmci.cr.application.dto.response.PersonalStatisticsResponse;
import com.cmci.cr.domain.service.StatisticsService;
import lombok.RequiredArgsConstructor;

import java.time.Duration;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Use Case: Obtenir les statistiques personnelles (US4.1)
 */
@RequiredArgsConstructor
public class GetPersonalStatisticsUseCase {

    private final StatisticsService statisticsService;

    /**
     * Exécute le use case de récupération des statistiques personnelles
     *
     * @param utilisateurId ID de l'utilisateur
     * @param startDate Date de début
     * @param endDate Date de fin
     * @return Statistiques personnelles
     */
    public PersonalStatisticsResponse execute(UUID utilisateurId, LocalDate startDate, LocalDate endDate) {
        // Calculer les statistiques via le domain service
        StatisticsService.PersonalStatistics stats = statisticsService.calculatePersonalStatistics(
                utilisateurId,
                startDate,
                endDate
        );

        // Mapper vers le DTO de réponse
        return PersonalStatisticsResponse.builder()
                .startDate(startDate)
                .endDate(endDate)
                .nombreTotalCRs(stats.getNombreTotalCRs())
                .tauxRegularite(stats.getTauxRegularite())
                .rdqdCompletCount(stats.getRdqdCompletCount())
                .tauxRDQD(stats.getTauxRDQD())
                .dureeTotalePriere(formatDuration(stats.getDureeTotalePriere()))
                .dureeMoyennePriere(formatDuration(stats.getDureeMoyennePriere()))
                .totalChapitresLus(stats.getTotalChapitresLus())
                .moyenneChapitresParJour(stats.getMoyenneChapitresParJour())
                .totalPersonnesEvangelisees(stats.getTotalPersonnesEvangelisees())
                .nombreConfessions(stats.getNombreConfessions())
                .nombreJeunes(stats.getNombreJeunes())
                .tendancePositive(stats.getTauxRegularite() >= 80.0) // Tendance positive si >= 80%
                .build();
    }

    /**
     * Formate une durée au format "HH:mm"
     */
    private String formatDuration(Duration duration) {
        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();
        return String.format("%02d:%02d", hours, minutes);
    }
}
