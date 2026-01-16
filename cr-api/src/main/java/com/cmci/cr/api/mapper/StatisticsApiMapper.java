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
        return StatisticsResponse.builder()
                .startDate(appResponse.getStartDate())
                .endDate(appResponse.getEndDate())
                .totalCRSoumis(appResponse.getTotalCRSoumis())
                .totalCRValides(appResponse.getTotalCRValides())
                .tauxCompletion(appResponse.getTauxCompletion())
                .totalRDQDAccomplis(appResponse.getTotalRDQDAccomplis())
                .totalRDQDAttendus(appResponse.getTotalRDQDAttendus())
                .moyenneRDQD(appResponse.getMoyenneRDQD())
                .totalPriereSeuleMinutes(appResponse.getTotalPriereSeule() != null ?
                        (int) appResponse.getTotalPriereSeule().toMinutes() : 0)
                .totalPriereCoupleMinutes(appResponse.getTotalPriereCouple() != null ?
                        (int) appResponse.getTotalPriereCouple().toMinutes() : 0)
                .totalPriereAvecEnfantsMinutes(appResponse.getTotalPriereAvecEnfants() != null ?
                        (int) appResponse.getTotalPriereAvecEnfants().toMinutes() : 0)
                .totalTempsEtudeParoleMinutes(appResponse.getTotalTempsEtudeParole() != null ?
                        (int) appResponse.getTotalTempsEtudeParole().toMinutes() : 0)
                .totalContactsUtiles(appResponse.getTotalContactsUtiles())
                .totalInvitationsCulte(appResponse.getTotalInvitationsCulte())
                .totalOffrandes(appResponse.getTotalOffrandes())
                .totalEvangelisations(appResponse.getTotalEvangelisations())
                .build();
    }
}
