package com.cmci.cr.application.dto.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Command pour changer le rôle d'un utilisateur
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChangeRoleCommand {

    @NotNull(message = "L'ID de l'utilisateur est obligatoire")
    UUID utilisateurId;

    @NotBlank(message = "Le nouveau rôle est obligatoire")
    String newRole; // FIDELE, FD, LEADER, PASTEUR, ADMIN
}
