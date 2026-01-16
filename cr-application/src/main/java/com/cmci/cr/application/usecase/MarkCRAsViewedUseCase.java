package com.cmci.cr.application.usecase;

import com.cmci.cr.application.dto.response.CRResponse;
import com.cmci.cr.domain.model.CompteRendu;
import com.cmci.cr.domain.repository.CompteRenduRepository;
import lombok.RequiredArgsConstructor;

import java.time.Duration;
import java.util.UUID;

/**
 * Use Case: Marquer un CR comme "vu" par le FD (US3.3)
 */
@RequiredArgsConstructor
public class MarkCRAsViewedUseCase {

    private final CompteRenduRepository compteRenduRepository;

    /**
     * Exécute le use case de marquage comme vu
     *
     * @param crId ID du CR à marquer comme vu
     * @param fdId ID du FD qui a consulté le CR
     * @return Le CR marqué comme vu
     * @throws IllegalArgumentException si le CR n'existe pas
     */
    public CRResponse execute(UUID crId, UUID fdId) {
        // Récupérer le CR existant
        CompteRendu existingCR = compteRenduRepository.findById(crId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Compte rendu non trouvé avec l'ID: " + crId
                ));

        // Note: La vérification des permissions (le fdId doit être le FD du propriétaire)
        // sera faite au niveau de la couche API/Security

        // Marquer comme vu
        CompteRendu markedCR = existingCR.marquerCommeVu();

        // Sauvegarder
        CompteRendu saved = compteRenduRepository.save(markedCR);

        return mapToResponse(saved);
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
