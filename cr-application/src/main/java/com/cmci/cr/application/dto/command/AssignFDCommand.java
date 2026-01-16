package com.cmci.cr.application.dto.command;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Command pour assigner un FD à un disciple
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignFDCommand {

    @NotNull(message = "L'ID du disciple est obligatoire")
    UUID discipleId;

    UUID fdId; // null pour désassigner
}
