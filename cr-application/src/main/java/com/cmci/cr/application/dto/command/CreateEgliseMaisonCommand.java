package com.cmci.cr.application.dto.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateEgliseMaisonCommand {

    @NotBlank(message = "Le nom de l'eglise de maison est obligatoire")
    String nom;

    @NotNull(message = "L'eglise locale est obligatoire")
    UUID egliseLocaleId;

    UUID leaderId;

    String adresse;
}
