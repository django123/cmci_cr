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
public class CreateEgliseMaisonRequest {

    @NotBlank(message = "Le nom de l'église de maison est obligatoire")
    private String nom;

    @NotNull(message = "L'église locale est obligatoire")
    private UUID egliseLocaleId;

    private UUID leaderId;

    private String adresse;
}
