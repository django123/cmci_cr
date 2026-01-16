package com.cmci.cr.api.mapper;

import com.cmci.cr.api.dto.request.AddCommentaireRequest;
import com.cmci.cr.api.dto.response.CommentaireResponse;
import com.cmci.cr.application.dto.command.AddCommentaireCommand;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Mapper entre les DTOs API et les DTOs Application pour Commentaire
 */
@Component
public class CommentaireApiMapper {

    /**
     * Convertit AddCommentaireRequest en AddCommentaireCommand
     */
    public AddCommentaireCommand toCommand(
            UUID compteRenduId,
            UUID auteurId,
            AddCommentaireRequest request) {

        return AddCommentaireCommand.builder()
                .compteRenduId(compteRenduId)
                .auteurId(auteurId)
                .contenu(request.getContenu())
                .build();
    }

    /**
     * Convertit CommentaireResponse (application) en CommentaireResponse (API)
     */
    public CommentaireResponse toApiResponse(
            com.cmci.cr.application.dto.response.CommentaireResponse appResponse) {

        return CommentaireResponse.builder()
                .id(appResponse.getId())
                .compteRenduId(appResponse.getCompteRenduId())
                .auteurId(appResponse.getAuteurId())
                .auteurNom(appResponse.getAuteurNom())
                .auteurPrenom(appResponse.getAuteurPrenom())
                .contenu(appResponse.getContenu())
                .createdAt(appResponse.getCreatedAt())
                .build();
    }
}
