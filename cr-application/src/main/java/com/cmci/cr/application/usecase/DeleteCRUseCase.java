package com.cmci.cr.application.usecase;

import com.cmci.cr.domain.model.CompteRendu;
import com.cmci.cr.domain.repository.CompteRenduRepository;
import com.cmci.cr.domain.service.CRDomainService;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

/**
 * Use Case: Supprimer un Compte Rendu (soft delete)
 */
@RequiredArgsConstructor
public class DeleteCRUseCase {

    private final CompteRenduRepository compteRenduRepository;
    private final CRDomainService crDomainService;

    /**
     * Exécute le use case de suppression d'un CR
     *
     * @param id ID du CR à supprimer
     * @param utilisateurId ID de l'utilisateur demandant la suppression
     * @throws IllegalArgumentException si le CR n'existe pas
     * @throws IllegalStateException si le CR n'est pas supprimable
     */
    public void execute(UUID id, UUID utilisateurId) {
        // Récupérer le CR existant
        CompteRendu existingCR = compteRenduRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Compte rendu non trouvé avec l'ID: " + id
                ));

        // Vérifier que l'utilisateur est bien le propriétaire
        if (!existingCR.getUtilisateurId().equals(utilisateurId)) {
            throw new IllegalArgumentException(
                    "Vous n'êtes pas autorisé à supprimer ce compte rendu"
            );
        }

        // Vérifier que le CR est supprimable (même règle que modification)
        if (!crDomainService.canModifyCR(existingCR)) {
            throw new IllegalStateException(
                    "Ce compte rendu ne peut plus être supprimé (statut: " +
                    existingCR.getStatut() + ", créé le: " + existingCR.getCreatedAt() + ")"
            );
        }

        // Supprimer le CR (soft delete dans l'infrastructure)
        compteRenduRepository.deleteById(id);
    }
}
