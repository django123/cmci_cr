package com.cmci.cr.application.usecase;

import com.cmci.cr.application.dto.response.CommentaireResponse;
import com.cmci.cr.domain.model.Commentaire;
import com.cmci.cr.domain.repository.CommentaireRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Use Case: Consulter les commentaires d'un CR
 */
@RequiredArgsConstructor
public class GetCommentairesUseCase {

    private final CommentaireRepository commentaireRepository;

    /**
     * Récupère tous les commentaires d'un CR
     */
    public List<CommentaireResponse> getByCompteRenduId(UUID compteRenduId) {
        return commentaireRepository.findByCompteRenduId(compteRenduId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Récupère tous les commentaires d'un auteur
     */
    public List<CommentaireResponse> getByAuteurId(UUID auteurId) {
        return commentaireRepository.findByAuteurId(auteurId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Compte le nombre de commentaires sur un CR
     */
    public long countByCompteRenduId(UUID compteRenduId) {
        return commentaireRepository.countByCompteRenduId(compteRenduId);
    }

    /**
     * Mappe un Commentaire vers CommentaireResponse
     */
    private CommentaireResponse mapToResponse(Commentaire commentaire) {
        return CommentaireResponse.builder()
                .id(commentaire.getId())
                .compteRenduId(commentaire.getCompteRenduId())
                .auteurId(commentaire.getAuteurId())
                .auteurNom(null) // Sera enrichi par le repository avec jointure
                .contenu(commentaire.getContenu())
                .createdAt(commentaire.getCreatedAt())
                .build();
    }
}
