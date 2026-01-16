package com.cmci.cr.application.usecase;

import com.cmci.cr.application.dto.response.CRResponse;
import com.cmci.cr.application.dto.command.UpdateCRCommand;
import com.cmci.cr.domain.model.CompteRendu;
import com.cmci.cr.domain.repository.CompteRenduRepository;
import com.cmci.cr.domain.service.CRDomainService;
import com.cmci.cr.domain.valueobject.RDQD;
import lombok.RequiredArgsConstructor;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Use Case: Modifier un Compte Rendu existant
 */
@RequiredArgsConstructor
public class UpdateCRUseCase {

    private final CompteRenduRepository compteRenduRepository;
    private final CRDomainService crDomainService;

    /**
     * Exécute le use case de modification d'un CR
     *
     * @param command Commande de modification
     * @return Le CR mis à jour
     * @throws IllegalArgumentException si le CR n'existe pas
     * @throws IllegalStateException si le CR n'est pas modifiable
     */
    public CRResponse execute(UpdateCRCommand command) {
        // Récupérer le CR existant
        CompteRendu existingCR = compteRenduRepository.findById(command.getId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Compte rendu non trouvé avec l'ID: " + command.getId()
                ));

        // Vérifier que l'utilisateur est bien le propriétaire
        if (!existingCR.getUtilisateurId().equals(command.getUtilisateurId())) {
            throw new IllegalArgumentException(
                    "Vous n'êtes pas autorisé à modifier ce compte rendu"
            );
        }

        // Vérifier que le CR est modifiable
        if (!crDomainService.canModifyCR(existingCR)) {
            throw new IllegalStateException(
                    "Ce compte rendu ne peut plus être modifié (statut: " +
                    existingCR.getStatut() + ", créé le: " + existingCR.getCreatedAt() + ")"
            );
        }

        // Créer le CR mis à jour
        CompteRendu updatedCR = existingCR
                .withRdqd(command.getRdqd() != null ? RDQD.fromString(command.getRdqd()) : existingCR.getRdqd())
                .withPriereSeule(command.getPriereSeule() != null ? parseDuration(command.getPriereSeule()) : existingCR.getPriereSeule())
                .withLectureBiblique(command.getLectureBiblique() != null ? command.getLectureBiblique() : existingCR.getLectureBiblique())
                .withLivreBiblique(command.getLivreBiblique() != null ? command.getLivreBiblique() : existingCR.getLivreBiblique())
                .withLitteraturePages(command.getLitteraturePages())
                .withLitteratureTotal(command.getLitteratureTotal())
                .withLitteratureTitre(command.getLitteratureTitre())
                .withPriereAutres(command.getPriereAutres())
                .withConfession(command.getConfession())
                .withJeune(command.getJeune())
                .withTypeJeune(command.getTypeJeune())
                .withEvangelisation(command.getEvangelisation())
                .withOffrande(command.getOffrande())
                .withNotes(command.getNotes())
                .withUpdatedAt(LocalDateTime.now());

        // Valider le CR
        updatedCR.validate();

        // Sauvegarder
        CompteRendu saved = compteRenduRepository.save(updatedCR);

        // Mapper vers le DTO de réponse
        return mapToResponse(saved);
    }

    /**
     * Parse une durée au format "HH:mm" ou ISO
     */
    private Duration parseDuration(String durationStr) {
        try {
            // Essayer le format HH:mm
            if (durationStr.contains(":")) {
                String[] parts = durationStr.split(":");
                int hours = Integer.parseInt(parts[0]);
                int minutes = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;
                return Duration.ofHours(hours).plusMinutes(minutes);
            }
            // Sinon essayer le format ISO
            return Duration.parse(durationStr);
        } catch (Exception e) {
            throw new IllegalArgumentException("Format de durée invalide: " + durationStr, e);
        }
    }

    /**
     * Mappe un CompteRendu vers CRResponse
     */
    private CRResponse mapToResponse(CompteRendu cr) {
        return CRResponse.builder()
                .id(cr.getId())
                .utilisateurId(cr.getUtilisateurId())
                .date(cr.getDate())
                .rdqd(cr.getRdqd().toString())
                .priereSeule(formatDuration(cr.getPriereSeule()))
                .lectureBiblique(cr.getLectureBiblique())
                .livreBiblique(cr.getLivreBiblique())
                .litteraturePages(cr.getLitteraturePages())
                .litteratureTotal(cr.getLitteratureTotal())
                .litteratureTitre(cr.getLitteratureTitre())
                .priereAutres(cr.getPriereAutres())
                .confession(cr.getConfession())
                .jeune(cr.getJeune())
                .typeJeune(cr.getTypeJeune())
                .evangelisation(cr.getEvangelisation())
                .offrande(cr.getOffrande())
                .notes(cr.getNotes())
                .statut(cr.getStatut().name())
                .vuParFd(cr.getVuParFd())
                .createdAt(cr.getCreatedAt())
                .updatedAt(cr.getUpdatedAt())
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
