package com.cmci.cr.application.dto.command;

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
public class UpdateEgliseLocaleCommand {

    @NotNull(message = "L'ID est obligatoire")
    UUID id;

    String nom;

    UUID zoneId;

    String adresse;

    UUID pasteurId;
}
