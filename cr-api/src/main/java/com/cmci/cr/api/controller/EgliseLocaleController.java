package com.cmci.cr.api.controller;

import com.cmci.cr.api.dto.request.CreateEgliseLocaleRequest;
import com.cmci.cr.api.dto.request.UpdateEgliseLocaleRequest;
import com.cmci.cr.application.dto.command.CreateEgliseLocaleCommand;
import com.cmci.cr.application.dto.command.UpdateEgliseLocaleCommand;
import com.cmci.cr.application.dto.response.EgliseLocaleResponse;
import com.cmci.cr.application.usecase.CreateEgliseLocaleUseCase;
import com.cmci.cr.application.usecase.DeleteEgliseLocaleUseCase;
import com.cmci.cr.application.usecase.GetEgliseLocaleUseCase;
import com.cmci.cr.application.usecase.UpdateEgliseLocaleUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/admin/eglises-locales")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Églises Locales", description = "API de gestion des églises locales")
@SecurityRequirement(name = "Bearer Authentication")
@CrossOrigin(origins = "*")
public class EgliseLocaleController {

    private final CreateEgliseLocaleUseCase createEgliseLocaleUseCase;
    private final GetEgliseLocaleUseCase getEgliseLocaleUseCase;
    private final UpdateEgliseLocaleUseCase updateEgliseLocaleUseCase;
    private final DeleteEgliseLocaleUseCase deleteEgliseLocaleUseCase;

    @PostMapping
    @PreAuthorize("hasAnyRole('PASTEUR', 'ADMIN')")
    @Operation(summary = "Créer une nouvelle église locale")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Église locale créée avec succès"),
        @ApiResponse(responseCode = "400", description = "Données invalides"),
        @ApiResponse(responseCode = "404", description = "Zone ou pasteur non trouvé")
    })
    public ResponseEntity<EgliseLocaleResponse> create(
            @Valid @RequestBody CreateEgliseLocaleRequest request) {

        log.info("Creating eglise locale: {}", request.getNom());

        CreateEgliseLocaleCommand command = CreateEgliseLocaleCommand.builder()
                .nom(request.getNom())
                .zoneId(request.getZoneId())
                .adresse(request.getAdresse())
                .pasteurId(request.getPasteurId())
                .build();

        EgliseLocaleResponse response = createEgliseLocaleUseCase.execute(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('LEADER', 'PASTEUR', 'ADMIN')")
    @Operation(summary = "Récupérer toutes les églises locales")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Liste des églises locales")
    })
    public ResponseEntity<List<EgliseLocaleResponse>> getAll(
            @Parameter(description = "Filtrer par zone") @RequestParam(required = false) UUID zoneId) {

        log.info("Getting eglises locales, zoneId={}", zoneId);

        List<EgliseLocaleResponse> responses;
        if (zoneId != null) {
            responses = getEgliseLocaleUseCase.getByZoneId(zoneId);
        } else {
            responses = getEgliseLocaleUseCase.getAll();
        }

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('LEADER', 'PASTEUR', 'ADMIN')")
    @Operation(summary = "Récupérer une église locale par ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Église locale trouvée"),
        @ApiResponse(responseCode = "404", description = "Église locale non trouvée")
    })
    public ResponseEntity<EgliseLocaleResponse> getById(
            @Parameter(description = "ID de l'église locale") @PathVariable UUID id) {

        log.info("Getting eglise locale {}", id);
        EgliseLocaleResponse response = getEgliseLocaleUseCase.getById(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('PASTEUR', 'ADMIN')")
    @Operation(summary = "Mettre à jour une église locale")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Église locale mise à jour"),
        @ApiResponse(responseCode = "400", description = "Données invalides"),
        @ApiResponse(responseCode = "404", description = "Église locale, zone ou pasteur non trouvé")
    })
    public ResponseEntity<EgliseLocaleResponse> update(
            @Parameter(description = "ID de l'église locale") @PathVariable UUID id,
            @Valid @RequestBody UpdateEgliseLocaleRequest request) {

        log.info("Updating eglise locale {}", id);

        UpdateEgliseLocaleCommand command = UpdateEgliseLocaleCommand.builder()
                .id(id)
                .nom(request.getNom())
                .zoneId(request.getZoneId())
                .adresse(request.getAdresse())
                .pasteurId(request.getPasteurId())
                .build();

        EgliseLocaleResponse response = updateEgliseLocaleUseCase.execute(command);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('PASTEUR', 'ADMIN')")
    @Operation(summary = "Supprimer une église locale")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Église locale supprimée"),
        @ApiResponse(responseCode = "404", description = "Église locale non trouvée"),
        @ApiResponse(responseCode = "409", description = "Des églises de maison sont rattachées")
    })
    public ResponseEntity<Void> delete(
            @Parameter(description = "ID de l'église locale") @PathVariable UUID id) {

        log.info("Deleting eglise locale {}", id);
        deleteEgliseLocaleUseCase.execute(id);
        return ResponseEntity.noContent().build();
    }
}
