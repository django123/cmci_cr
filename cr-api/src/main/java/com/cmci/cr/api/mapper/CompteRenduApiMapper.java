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
        String priereSeule = "00:00";
        if (request.getPriereSeuleMinutes() != null) {
            int hours = request.getPriereSeuleMinutes() / 60;
            int minutes = request.getPriereSeuleMinutes() % 60;
            priereSeule = String.format("%02d:%02d", hours, minutes);
        }

        return CreateCRCommand.builder()
                .utilisateurId(utilisateurId)
                .date(request.getDate())
                .rdqd(request.getRdqd())
                .priereSeule(priereSeule)
                .lectureBiblique(request.getTempsEtudeParoleMinutes() != null ? request.getTempsEtudeParoleMinutes() / 10 : 0) // Approximation: 10 min par chapitre
                .livreBiblique(null)
                .litteraturePages(null)
                .litteratureTotal(null)
                .litteratureTitre(null)
                .priereAutres(request.getNombreContactsUtiles())
                .confession(false)
                .jeune(false)
                .typeJeune(null)
                .evangelisation(request.getEvangelisations())
                .offrande(request.getOffrande() != null && request.getOffrande().compareTo(java.math.BigDecimal.ZERO) > 0)
                .notes(request.getCommentaire())
                .build();
    }

    /**
     * Convertit UpdateCompteRenduRequest en UpdateCRCommand
     */
    public UpdateCRCommand toUpdateCommand(UUID compteRenduId, UpdateCompteRenduRequest request) {
        // Convertir la durée de prière en format HH:mm
        String priereSeule = null;
        if (request.getPriereSeuleMinutes() != null) {
            int hours = request.getPriereSeuleMinutes() / 60;
            int minutes = request.getPriereSeuleMinutes() % 60;
            priereSeule = String.format("%02d:%02d", hours, minutes);
        }

        return UpdateCRCommand.builder()
                .id(compteRenduId)
                .utilisateurId(null) // Sera défini par le controller
                .rdqd(request.getRdqd())
                .priereSeule(priereSeule)
                .lectureBiblique(null)
                .livreBiblique(null)
                .litteraturePages(null)
                .litteratureTotal(null)
                .litteratureTitre(null)
                .priereAutres(null)
                .confession(null)
                .jeune(null)
                .typeJeune(null)
                .evangelisation(request.getEvangelisations())
                .offrande(request.getOffrande() != null ? request.getOffrande().compareTo(java.math.BigDecimal.ZERO) > 0 : null)
                .notes(request.getCommentaire())
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
}
