package com.cmci.cr.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO Request pour ajouter un commentaire
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddCommentaireRequest {

    @NotBlank(message = "Le contenu du commentaire est obligatoire")
    @Size(min = 1, max = 2000, message = "Le commentaire doit contenir entre 1 et 2000 caractères")
    private String contenu;
}
