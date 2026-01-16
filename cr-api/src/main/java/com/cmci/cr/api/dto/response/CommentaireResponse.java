package com.cmci.cr.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO Response pour un Commentaire
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentaireResponse {

    private UUID id;
    private UUID compteRenduId;
    private UUID auteurId;
    private String auteurNom;
    private String auteurPrenom;
    private String contenu;
    private LocalDateTime createdAt;
}
