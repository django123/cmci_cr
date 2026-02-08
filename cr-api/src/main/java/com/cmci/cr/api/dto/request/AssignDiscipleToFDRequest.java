package com.cmci.cr.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Requête pour assigner un disciple à un FD
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Requête d'assignation d'un disciple à un FD")
public class AssignDiscipleToFDRequest {

    @NotNull(message = "L'ID du FD est obligatoire")
    @Schema(description = "ID du FD (Faiseur de Disciples) à qui assigner le disciple",
            example = "d1000000-0000-0000-0000-000000000020")
    private UUID fdId;
}
