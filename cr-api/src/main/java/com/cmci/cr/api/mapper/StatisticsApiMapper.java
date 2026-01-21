package com.cmci.cr.api.mapper;

import com.cmci.cr.api.dto.response.StatisticsResponse;
import com.cmci.cr.application.dto.response.PersonalStatisticsResponse;
import org.springframework.stereotype.Component;

/**
 * Mapper entre les DTOs API et les DTOs Application pour Statistiques
 */
@Component
public class StatisticsApiMapper {

    /**
     * Convertit PersonalStatisticsResponse (application) en StatisticsResponse (API)
     */
    public StatisticsResponse toApiResponse(PersonalStatisticsResponse appResponse) {
        // Convertir la durée totale de prière en minutes
        int totalPriereMinutes = 0;
        if (appResponse.getDureeTotalePriere() != null) {
            String[] parts = appResponse.getDureeTotalePriere().split(":");
            if (parts.length >= 2) {
                totalPriereMinutes = Integer.parseInt(parts[0]) * 60 + Integer.parseInt(parts[1]);
            }
        }

        return StatisticsResponse.builder()
                .startDate(appResponse.getStartDate())
                .endDate(appResponse.getEndDate())
                .totalCRSoumis(appResponse.getNombreTotalCRs() != null ? appResponse.getNombreTotalCRs().intValue() : 0)
                .totalCRValides(appResponse.getNombreTotalCRs() != null ? appResponse.getNombreTotalCRs().intValue() : 0)
                .tauxCompletion(appResponse.getTauxRegularite())
                .totalRDQDAccomplis(appResponse.getRdqdCompletCount() != null ? appResponse.getRdqdCompletCount().intValue() : 0)
                .totalRDQDAttendus(appResponse.getNombreTotalCRs() != null ? appResponse.getNombreTotalCRs().intValue() : 0)
                .moyenneRDQD(appResponse.getTauxRDQD())
                .totalPriereSeuleMinutes(totalPriereMinutes)
                .totalPriereCoupleMinutes(0)
                .totalPriereAvecEnfantsMinutes(0)
                .totalTempsEtudeParoleMinutes(0)
                .totalContactsUtiles(0)
                .totalInvitationsCulte(0)
                .totalOffrandes(null)
                .totalEvangelisations(appResponse.getTotalPersonnesEvangelisees())
                .build();
    }
}
