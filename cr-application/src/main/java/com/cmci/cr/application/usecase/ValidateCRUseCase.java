package com.cmci.cr.application.usecase;

import com.cmci.cr.application.dto.response.CRResponse;
import com.cmci.cr.domain.model.CompteRendu;
import com.cmci.cr.domain.repository.CompteRenduRepository;
import com.cmci.cr.domain.valueobject.StatutCR;
import lombok.RequiredArgsConstructor;

import java.time.Duration;
import java.util.UUID;

/**
 * Use Case: Valider un Compte Rendu (par un FD/Leader/Pasteur)
 * Règle métier US2.5: Seuls les CR au statut SOUMIS peuvent être validés
 */
@RequiredArgsConstructor
public class ValidateCRUseCase {

    private final CompteRenduRepository compteRenduRepository;

    /**
     * Exécute le use case de validation d'un CR
     *
     * @param crId ID du CR à valider
     * @param validatorId ID du FD/Leader/Pasteur qui valide
     * @return Le CR validé
     * @throws IllegalArgumentException si le CR n'existe pas
     * @throws IllegalStateException si le CR n'est pas au statut SOUMIS
     */
    public CRResponse execute(UUID crId, UUID validatorId) {
        // Récupérer le CR existant
        CompteRendu existingCR = compteRenduRepository.findById(crId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Compte rendu non trouvé avec l'ID: " + crId
                ));

        // Vérifier que le CR peut être validé
        if (existingCR.getStatut() != StatutCR.SOUMIS) {
            throw new IllegalStateException(
                    "Seuls les comptes rendus au statut SOUMIS peuvent être validés. " +
                    "Statut actuel: " + existingCR.getStatut()
            );
        }

        // Note: La vérification des permissions (le validatorId doit être le FD du propriétaire)
        // sera faite au niveau de la couche API/Security

        // Valider le CR (transition SOUMIS -> VALIDE)
        CompteRendu validatedCR = existingCR.valider();

        // Sauvegarder
        CompteRendu saved = compteRenduRepository.save(validatedCR);

        // Émettre un événement (sera géré par l'infrastructure)
        // CRValidatedEvent.of(crId, existingCR.getUtilisateurId(), validatorId, existingCR.getDate())

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
