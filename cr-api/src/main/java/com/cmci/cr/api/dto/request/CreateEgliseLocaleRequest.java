package com.cmci.cr.api.dto.request;

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
public class CreateEgliseLocaleRequest {

    @NotBlank(message = "Le nom de l'église locale est obligatoire")
    private String nom;

    @NotNull(message = "La zone est obligatoire")
    private UUID zoneId;

    private String adresse;

    private UUID pasteurId;
}
