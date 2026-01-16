package com.cmci.cr.api.mapper;

import com.cmci.cr.api.dto.request.CreateCompteRenduRequest;
import com.cmci.cr.api.dto.request.UpdateCompteRenduRequest;
import com.cmci.cr.api.dto.response.CompteRenduResponse;
import com.cmci.cr.application.dto.command.CreateCRCommand;
import com.cmci.cr.application.dto.command.UpdateCRCommand;
import com.cmci.cr.application.dto.response.CRResponse;
import org.springframework.stereotype.Component;

import java.time.Duration;
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
        return CreateCRCommand.builder()
                .utilisateurId(utilisateurId)
                .date(request.getDate())
                .rdqd(request.getRdqd())
                .priereSeule(Duration.ofMinutes(request.getPriereSeuleMinutes()))
                .priereCouple(request.getPriereCoupleMinutes() != null ?
                        Duration.ofMinutes(request.getPriereCoupleMinutes()) : Duration.ZERO)
                .priereAvecEnfants(request.getPriereAvecEnfantsMinutes() != null ?
                        Duration.ofMinutes(request.getPriereAvecEnfantsMinutes()) : Duration.ZERO)
                .tempsEtudeParole(Duration.ofMinutes(request.getTempsEtudeParoleMinutes()))
                .nombreContactsUtiles(request.getNombreContactsUtiles())
                .invitationsCulte(request.getInvitationsCulte())
                .offrande(request.getOffrande())
                .evangelisations(request.getEvangelisations())
                .commentaire(request.getCommentaire())
                .build();
    }

    /**
     * Convertit UpdateCompteRenduRequest en UpdateCRCommand
     */
    public UpdateCRCommand toUpdateCommand(UUID compteRenduId, UpdateCompteRenduRequest request) {
        return UpdateCRCommand.builder()
                .compteRenduId(compteRenduId)
                .rdqd(request.getRdqd())
                .priereSeule(request.getPriereSeuleMinutes() != null ?
                        Duration.ofMinutes(request.getPriereSeuleMinutes()) : null)
                .priereCouple(request.getPriereCoupleMinutes() != null ?
                        Duration.ofMinutes(request.getPriereCoupleMinutes()) : null)
                .priereAvecEnfants(request.getPriereAvecEnfantsMinutes() != null ?
                        Duration.ofMinutes(request.getPriereAvecEnfantsMinutes()) : null)
                .tempsEtudeParole(request.getTempsEtudeParoleMinutes() != null ?
                        Duration.ofMinutes(request.getTempsEtudeParoleMinutes()) : null)
                .nombreContactsUtiles(request.getNombreContactsUtiles())
                .invitationsCulte(request.getInvitationsCulte())
                .offrande(request.getOffrande())
                .evangelisations(request.getEvangelisations())
                .commentaire(request.getCommentaire())
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
