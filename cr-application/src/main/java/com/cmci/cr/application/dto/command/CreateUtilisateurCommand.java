package com.cmci.cr.application.dto.command;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Command pour créer un nouvel utilisateur
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateUtilisateurCommand {

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Format d'email invalide")
    String email;

    @NotBlank(message = "Le nom est obligatoire")
    String nom;

    @NotBlank(message = "Le prénom est obligatoire")
    String prenom;

    @NotNull(message = "Le rôle est obligatoire")
    String role; // FIDELE, FD, LEADER, PASTEUR, ADMIN

    UUID egliseMaisonId;
    UUID fdId;
    String avatarUrl;
    String telephone;
    LocalDate dateNaissance;
    LocalDate dateBapteme;
}
