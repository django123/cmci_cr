package com.cmci.cr.api.controller;

import com.cmci.cr.api.dto.request.CreateRegionRequest;
import com.cmci.cr.api.dto.request.UpdateRegionRequest;
import com.cmci.cr.application.dto.command.CreateRegionCommand;
import com.cmci.cr.application.dto.command.UpdateRegionCommand;
import com.cmci.cr.application.dto.response.RegionResponse;
import com.cmci.cr.application.usecase.CreateRegionUseCase;
import com.cmci.cr.application.usecase.DeleteRegionUseCase;
import com.cmci.cr.application.usecase.GetRegionUseCase;
import com.cmci.cr.application.usecase.UpdateRegionUseCase;
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
@RequestMapping("/v1/admin/regions")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Régions", description = "API de gestion des régions")
@SecurityRequirement(name = "Bearer Authentication")
@CrossOrigin(origins = "*")
public class RegionController {

    private final CreateRegionUseCase createRegionUseCase;
    private final GetRegionUseCase getRegionUseCase;
    private final UpdateRegionUseCase updateRegionUseCase;
    private final DeleteRegionUseCase deleteRegionUseCase;

    @PostMapping
    @PreAuthorize("hasAnyRole('PASTEUR', 'ADMIN')")
    @Operation(summary = "Créer une nouvelle région")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Région créée avec succès"),
        @ApiResponse(responseCode = "400", description = "Données invalides"),
        @ApiResponse(responseCode = "409", description = "Le code existe déjà")
    })
    public ResponseEntity<RegionResponse> create(
            @Valid @RequestBody CreateRegionRequest request) {

        log.info("Creating region: {}", request.getNom());

        CreateRegionCommand command = CreateRegionCommand.builder()
                .nom(request.getNom())
                .code(request.getCode())
                .build();

        RegionResponse response = createRegionUseCase.execute(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('LEADER', 'PASTEUR', 'ADMIN')")
    @Operation(summary = "Récupérer toutes les régions")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Liste des régions")
    })
    public ResponseEntity<List<RegionResponse>> getAll() {
        log.info("Getting all regions");
        List<RegionResponse> responses = getRegionUseCase.getAll();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('LEADER', 'PASTEUR', 'ADMIN')")
    @Operation(summary = "Récupérer une région par ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Région trouvée"),
        @ApiResponse(responseCode = "404", description = "Région non trouvée")
    })
    public ResponseEntity<RegionResponse> getById(
            @Parameter(description = "ID de la région") @PathVariable UUID id) {

        log.info("Getting region {}", id);
        RegionResponse response = getRegionUseCase.getById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/code/{code}")
    @PreAuthorize("hasAnyRole('LEADER', 'PASTEUR', 'ADMIN')")
    @Operation(summary = "Récupérer une région par code")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Région trouvée"),
        @ApiResponse(responseCode = "404", description = "Région non trouvée")
    })
    public ResponseEntity<RegionResponse> getByCode(
            @Parameter(description = "Code de la région") @PathVariable String code) {

        log.info("Getting region by code {}", code);
        RegionResponse response = getRegionUseCase.getByCode(code);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('PASTEUR', 'ADMIN')")
    @Operation(summary = "Mettre à jour une région")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Région mise à jour"),
        @ApiResponse(responseCode = "400", description = "Données invalides"),
        @ApiResponse(responseCode = "404", description = "Région non trouvée"),
        @ApiResponse(responseCode = "409", description = "Le code existe déjà")
    })
    public ResponseEntity<RegionResponse> update(
            @Parameter(description = "ID de la région") @PathVariable UUID id,
            @Valid @RequestBody UpdateRegionRequest request) {

        log.info("Updating region {}", id);

        UpdateRegionCommand command = UpdateRegionCommand.builder()
                .id(id)
                .nom(request.getNom())
                .code(request.getCode())
                .build();

        RegionResponse response = updateRegionUseCase.execute(command);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('PASTEUR', 'ADMIN')")
    @Operation(summary = "Supprimer une région")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Région supprimée"),
        @ApiResponse(responseCode = "404", description = "Région non trouvée"),
        @ApiResponse(responseCode = "409", description = "Des zones sont rattachées à cette région")
    })
    public ResponseEntity<Void> delete(
            @Parameter(description = "ID de la région") @PathVariable UUID id) {

        log.info("Deleting region {}", id);
        deleteRegionUseCase.execute(id);
        return ResponseEntity.noContent().build();
    }
}
