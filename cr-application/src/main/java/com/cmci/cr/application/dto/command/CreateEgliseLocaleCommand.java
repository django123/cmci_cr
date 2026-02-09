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
public class CreateEgliseLocaleCommand {

    @NotBlank(message = "Le nom de l'eglise locale est obligatoire")
    String nom;

    @NotNull(message = "La zone est obligatoire")
    UUID zoneId;

    String adresse;

    UUID pasteurId;
}
