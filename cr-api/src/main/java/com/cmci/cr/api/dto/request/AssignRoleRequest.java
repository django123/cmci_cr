package com.cmci.cr.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Requête pour assigner un rôle à un utilisateur
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Requête d'attribution de rôle")
public class AssignRoleRequest {

    @NotNull(message = "Le rôle est obligatoire")
    @Schema(description = "Nouveau rôle à attribuer",
            example = "FD",
            allowableValues = {"FIDELE", "FD", "LEADER", "PASTEUR", "ADMIN"})
    private String role;
}
