package com.cmci.cr.application.dto.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateRegionCommand {

    @NotBlank(message = "Le nom de la region est obligatoire")
    String nom;

    @NotBlank(message = "Le code de la region est obligatoire")
    @Size(max = 10, message = "Le code ne peut pas depasser 10 caracteres")
    String code;
}
