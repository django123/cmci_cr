package com.cmci.cr.application.dto.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Value;

import java.util.UUID;

/**
 * Command pour ajouter un commentaire sur un CR
 */
@Value
@Builder
public class AddCommentaireCommand {

    @NotNull(message = "L'ID du compte rendu est obligatoire")
    UUID compteRenduId;

    @NotNull(message = "L'auteur du commentaire est obligatoire")
    UUID auteurId;

    @NotBlank(message = "Le contenu du commentaire ne peut pas être vide")
    @Size(max = 5000, message = "Le commentaire ne peut pas dépasser 5000 caractères")
    String contenu;
}
