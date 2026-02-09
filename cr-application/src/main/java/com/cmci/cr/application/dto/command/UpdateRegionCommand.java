package com.cmci.cr.application.dto.command;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateRegionCommand {

    @NotNull(message = "L'ID est obligatoire")
    UUID id;

    String nom;

    @Size(max = 10, message = "Le code ne peut pas depasser 10 caracteres")
    String code;
}
