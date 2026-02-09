package com.cmci.cr.api.controller;

import com.cmci.cr.api.dto.request.CreateZoneRequest;
import com.cmci.cr.api.dto.request.UpdateZoneRequest;
import com.cmci.cr.application.dto.command.CreateZoneCommand;
import com.cmci.cr.application.dto.command.UpdateZoneCommand;
import com.cmci.cr.application.dto.response.ZoneResponse;
import com.cmci.cr.application.usecase.CreateZoneUseCase;
import com.cmci.cr.application.usecase.DeleteZoneUseCase;
import com.cmci.cr.application.usecase.GetZoneUseCase;
import com.cmci.cr.application.usecase.UpdateZoneUseCase;
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
@RequestMapping("/v1/admin/zones")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Zones", description = "API de gestion des zones")
@SecurityRequirement(name = "Bearer Authentication")
@CrossOrigin(origins = "*")
public class ZoneController {

    private final CreateZoneUseCase createZoneUseCase;
    private final GetZoneUseCase getZoneUseCase;
    private final UpdateZoneUseCase updateZoneUseCase;
    private final DeleteZoneUseCase deleteZoneUseCase;

    @PostMapping
    @PreAuthorize("hasAnyRole('PASTEUR', 'ADMIN')")
    @Operation(summary = "Créer une nouvelle zone")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Zone créée avec succès"),
        @ApiResponse(responseCode = "400", description = "Données invalides"),
        @ApiResponse(responseCode = "404", description = "Région non trouvée")
    })
    public ResponseEntity<ZoneResponse> create(
            @Valid @RequestBody CreateZoneRequest request) {

        log.info("Creating zone: {}", request.getNom());

        CreateZoneCommand command = CreateZoneCommand.builder()
                .nom(request.getNom())
                .regionId(request.getRegionId())
                .build();

        ZoneResponse response = createZoneUseCase.execute(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('LEADER', 'PASTEUR', 'ADMIN')")
    @Operation(summary = "Récupérer toutes les zones")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Liste des zones")
    })
    public ResponseEntity<List<ZoneResponse>> getAll(
            @Parameter(description = "Filtrer par région") @RequestParam(required = false) UUID regionId) {

        log.info("Getting zones, regionId={}", regionId);

        List<ZoneResponse> responses;
        if (regionId != null) {
            responses = getZoneUseCase.getByRegionId(regionId);
        } else {
            responses = getZoneUseCase.getAll();
        }

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('LEADER', 'PASTEUR', 'ADMIN')")
    @Operation(summary = "Récupérer une zone par ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Zone trouvée"),
        @ApiResponse(responseCode = "404", description = "Zone non trouvée")
    })
    public ResponseEntity<ZoneResponse> getById(
            @Parameter(description = "ID de la zone") @PathVariable UUID id) {

        log.info("Getting zone {}", id);
        ZoneResponse response = getZoneUseCase.getById(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('PASTEUR', 'ADMIN')")
    @Operation(summary = "Mettre à jour une zone")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Zone mise à jour"),
        @ApiResponse(responseCode = "400", description = "Données invalides"),
        @ApiResponse(responseCode = "404", description = "Zone ou région non trouvée")
    })
    public ResponseEntity<ZoneResponse> update(
            @Parameter(description = "ID de la zone") @PathVariable UUID id,
            @Valid @RequestBody UpdateZoneRequest request) {

        log.info("Updating zone {}", id);

        UpdateZoneCommand command = UpdateZoneCommand.builder()
                .id(id)
                .nom(request.getNom())
                .regionId(request.getRegionId())
                .build();

        ZoneResponse response = updateZoneUseCase.execute(command);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('PASTEUR', 'ADMIN')")
    @Operation(summary = "Supprimer une zone")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Zone supprimée"),
        @ApiResponse(responseCode = "404", description = "Zone non trouvée"),
        @ApiResponse(responseCode = "409", description = "Des églises locales sont rattachées à cette zone")
    })
    public ResponseEntity<Void> delete(
            @Parameter(description = "ID de la zone") @PathVariable UUID id) {

        log.info("Deleting zone {}", id);
        deleteZoneUseCase.execute(id);
        return ResponseEntity.noContent().build();
    }
}
