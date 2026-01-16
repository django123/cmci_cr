package com.cmci.cr.application.usecase;

import com.cmci.cr.application.dto.command.AddCommentaireCommand;
import com.cmci.cr.application.dto.response.CommentaireResponse;
import com.cmci.cr.domain.model.Commentaire;
import com.cmci.cr.domain.model.CompteRendu;
import com.cmci.cr.domain.repository.CommentaireRepository;
import com.cmci.cr.domain.repository.CompteRenduRepository;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Use Case: Ajouter un commentaire sur un CR (US3.2)
 */
@RequiredArgsConstructor
public class AddCommentaireUseCase {

    private final CommentaireRepository commentaireRepository;
    private final CompteRenduRepository compteRenduRepository;

    /**
     * Exécute le use case d'ajout d'un commentaire
     *
     * @param command Commande d'ajout de commentaire
     * @return Le commentaire créé
     * @throws IllegalArgumentException si le CR n'existe pas
     */
    public CommentaireResponse execute(AddCommentaireCommand command) {
        // Vérifier que le CR existe
        CompteRendu cr = compteRenduRepository.findById(command.getCompteRenduId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Compte rendu non trouvé avec l'ID: " + command.getCompteRenduId()
                ));

        // Note: La vérification des permissions (l'auteur doit pouvoir commenter ce CR)
        // sera faite au niveau de la couche API/Security

        // Créer le commentaire
        Commentaire commentaire = Commentaire.builder()
                .id(UUID.randomUUID())
                .compteRenduId(command.getCompteRenduId())
                .auteurId(command.getAuteurId())
                .contenu(command.getContenu())
                .createdAt(LocalDateTime.now())
                .build();

        // Valider
        commentaire.validate();

        // Sauvegarder
        Commentaire saved = commentaireRepository.save(commentaire);

        // Émettre un événement pour notifier le propriétaire du CR
        // CommentaireAddedEvent.of(saved.getId(), cr.getId(), command.getAuteurId(), cr.getUtilisateurId(), command.getContenu())

        return CommentaireResponse.builder()
                .id(saved.getId())
                .compteRenduId(saved.getCompteRenduId())
                .auteurId(saved.getAuteurId())
                .auteurNom(null) // Sera enrichi par le repository
                .contenu(saved.getContenu())
                .createdAt(saved.getCreatedAt())
                .build();
    }
}
