package com.cmci.cr.application.dto.command;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Command pour mettre à jour un utilisateur existant
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUtilisateurCommand {

    @NotNull(message = "L'ID de l'utilisateur est obligatoire")
    UUID id;

    @Email(message = "Format d'email invalide")
    String email;

    String nom;
    String prenom;
    UUID egliseMaisonId;
    String avatarUrl;
    String telephone;
    LocalDate dateNaissance;
    LocalDate dateBapteme;
}
