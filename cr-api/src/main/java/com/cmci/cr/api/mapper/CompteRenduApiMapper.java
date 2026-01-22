package com.cmci.cr.api.mapper;

import com.cmci.cr.api.dto.request.CreateCompteRenduRequest;
import com.cmci.cr.api.dto.request.UpdateCompteRenduRequest;
import com.cmci.cr.api.dto.response.CompteRenduResponse;
import com.cmci.cr.application.dto.command.CreateCRCommand;
import com.cmci.cr.application.dto.command.UpdateCRCommand;
import com.cmci.cr.application.dto.response.CRResponse;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Mapper entre les DTOs API et les DTOs Application pour CompteRendu
 */
@Component
public class CompteRenduApiMapper {

    /**
     * Convertit CreateCompteRenduRequest en CreateCRCommand
     */
    public CreateCRCommand toCreateCommand(CreateCompteRenduRequest request, UUID utilisateurId) {
        // Convertir la durée de prière en format HH:mm
        String priereSeule = convertMinutesToTimeString(request.getPriereSeuleMinutes());
        String priereCouple = convertMinutesToTimeString(request.getPriereCoupleMinutes());
        String priereAvecEnfants = convertMinutesToTimeString(request.getPriereAvecEnfantsMinutes());

        return CreateCRCommand.builder()
                .utilisateurId(utilisateurId)
                .date(request.getDate())
                .rdqd(request.getRdqd())
                .priereSeule(priereSeule)
                .priereCouple(priereCouple)
                .priereAvecEnfants(priereAvecEnfants)
                .priereAutres(request.getPriereAutres())
                .lectureBiblique(request.getLectureBiblique())
                .livreBiblique(request.getLivreBiblique())
                .litteraturePages(request.getLitteraturePages())
                .litteratureTotal(request.getLitteratureTotal())
                .litteratureTitre(request.getLitteratureTitre())
                .confession(request.getConfession())
                .jeune(request.getJeune())
                .typeJeune(request.getTypeJeune())
                .evangelisation(request.getEvangelisation())
                .offrande(request.getOffrande())
                .notes(request.getNotes())
                .build();
    }

    /**
     * Convertit UpdateCompteRenduRequest en UpdateCRCommand
     */
    public UpdateCRCommand toUpdateCommand(UUID compteRenduId, UpdateCompteRenduRequest request) {
        // Convertir la durée de prière en format HH:mm
        String priereSeule = convertMinutesToTimeString(request.getPriereSeuleMinutes());
        String priereCouple = convertMinutesToTimeString(request.getPriereCoupleMinutes());
        String priereAvecEnfants = convertMinutesToTimeString(request.getPriereAvecEnfantsMinutes());

        return UpdateCRCommand.builder()
                .id(compteRenduId)
                .utilisateurId(null) // Sera défini par le controller
                .rdqd(request.getRdqd())
                .priereSeule(priereSeule)
                .priereCouple(priereCouple)
                .priereAvecEnfants(priereAvecEnfants)
                .priereAutres(request.getPriereAutres())
                .lectureBiblique(request.getLectureBiblique())
                .livreBiblique(request.getLivreBiblique())
                .litteraturePages(request.getLitteraturePages())
                .litteratureTotal(request.getLitteratureTotal())
                .litteratureTitre(request.getLitteratureTitre())
                .confession(request.getConfession())
                .jeune(request.getJeune())
                .typeJeune(request.getTypeJeune())
                .evangelisation(request.getEvangelisation())
                .offrande(request.getOffrande())
                .notes(request.getNotes())
                .build();
    }

    /**
     * Convertit CRResponse (application) en CompteRenduResponse (API)
     */
    public CompteRenduResponse toApiResponse(CRResponse appResponse) {
        return CompteRenduResponse.builder()
                .id(appResponse.getId())
                .utilisateurId(appResponse.getUtilisateurId())
                .date(appResponse.getDate())
                .rdqd(appResponse.getRdqd())
                .priereSeule(appResponse.getPriereSeule())
                .lectureBiblique(appResponse.getLectureBiblique())
                .livreBiblique(appResponse.getLivreBiblique())
                .litteraturePages(appResponse.getLitteraturePages())
                .litteratureTotal(appResponse.getLitteratureTotal())
                .litteratureTitre(appResponse.getLitteratureTitre())
                .priereAutres(appResponse.getPriereAutres())
                .confession(appResponse.getConfession())
                .jeune(appResponse.getJeune())
                .typeJeune(appResponse.getTypeJeune())
                .evangelisation(appResponse.getEvangelisation())
                .offrande(appResponse.getOffrande())
                .notes(appResponse.getNotes())
                .statut(appResponse.getStatut())
                .vuParFd(appResponse.getVuParFd())
                .createdAt(appResponse.getCreatedAt())
                .updatedAt(appResponse.getUpdatedAt())
                .build();
    }

    /**
     * Convertit des minutes en chaîne de format HH:mm
     */
    private String convertMinutesToTimeString(Integer minutes) {
        if (minutes == null) {
            return null;
        }
        int hours = minutes / 60;
        int mins = minutes % 60;
        return String.format("%02d:%02d", hours, mins);
    }
}
