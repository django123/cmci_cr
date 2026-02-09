package com.cmci.cr.api.controller;

import com.cmci.cr.api.dto.request.CreateEgliseMaisonRequest;
import com.cmci.cr.api.dto.request.UpdateEgliseMaisonRequest;
import com.cmci.cr.application.dto.command.CreateEgliseMaisonCommand;
import com.cmci.cr.application.dto.command.UpdateEgliseMaisonCommand;
import com.cmci.cr.application.dto.response.EgliseMaisonResponse;
import com.cmci.cr.application.usecase.CreateEgliseMaisonUseCase;
import com.cmci.cr.application.usecase.DeleteEgliseMaisonUseCase;
import com.cmci.cr.application.usecase.GetEgliseMaisonUseCase;
import com.cmci.cr.application.usecase.UpdateEgliseMaisonUseCase;
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
@RequestMapping("/v1/admin/eglises-maison")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Églises de Maison", description = "API de gestion des églises de maison")
@SecurityRequirement(name = "Bearer Authentication")
@CrossOrigin(origins = "*")
public class EgliseMaisonController {

    private final CreateEgliseMaisonUseCase createEgliseMaisonUseCase;
    private final GetEgliseMaisonUseCase getEgliseMaisonUseCase;
    private final UpdateEgliseMaisonUseCase updateEgliseMaisonUseCase;
    private final DeleteEgliseMaisonUseCase deleteEgliseMaisonUseCase;

    @PostMapping
    @PreAuthorize("hasAnyRole('PASTEUR', 'ADMIN')")
    @Operation(summary = "Créer une nouvelle église de maison")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Église de maison créée avec succès"),
        @ApiResponse(responseCode = "400", description = "Données invalides"),
        @ApiResponse(responseCode = "404", description = "Église locale ou leader non trouvé")
    })
    public ResponseEntity<EgliseMaisonResponse> create(
            @Valid @RequestBody CreateEgliseMaisonRequest request) {

        log.info("Creating eglise de maison: {}", request.getNom());

        CreateEgliseMaisonCommand command = CreateEgliseMaisonCommand.builder()
                .nom(request.getNom())
                .egliseLocaleId(request.getEgliseLocaleId())
                .leaderId(request.getLeaderId())
                .adresse(request.getAdresse())
                .build();

        EgliseMaisonResponse response = createEgliseMaisonUseCase.execute(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('LEADER', 'PASTEUR', 'ADMIN')")
    @Operation(summary = "Récupérer toutes les églises de maison")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Liste des églises de maison")
    })
    public ResponseEntity<List<EgliseMaisonResponse>> getAll(
            @Parameter(description = "Filtrer par église locale") @RequestParam(required = false) UUID egliseLocaleId) {

        log.info("Getting eglises de maison, egliseLocaleId={}", egliseLocaleId);

        List<EgliseMaisonResponse> responses;
        if (egliseLocaleId != null) {
            responses = getEgliseMaisonUseCase.getByEgliseLocaleId(egliseLocaleId);
        } else {
            responses = getEgliseMaisonUseCase.getAll();
        }

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('LEADER', 'PASTEUR', 'ADMIN')")
    @Operation(summary = "Récupérer une église de maison par ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Église de maison trouvée"),
        @ApiResponse(responseCode = "404", description = "Église de maison non trouvée")
    })
    public ResponseEntity<EgliseMaisonResponse> getById(
            @Parameter(description = "ID de l'église de maison") @PathVariable UUID id) {

        log.info("Getting eglise de maison {}", id);
        EgliseMaisonResponse response = getEgliseMaisonUseCase.getById(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('PASTEUR', 'ADMIN')")
    @Operation(summary = "Mettre à jour une église de maison")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Église de maison mise à jour"),
        @ApiResponse(responseCode = "400", description = "Données invalides"),
        @ApiResponse(responseCode = "404", description = "Église de maison, église locale ou leader non trouvé")
    })
    public ResponseEntity<EgliseMaisonResponse> update(
            @Parameter(description = "ID de l'église de maison") @PathVariable UUID id,
            @Valid @RequestBody UpdateEgliseMaisonRequest request) {

        log.info("Updating eglise de maison {}", id);

        UpdateEgliseMaisonCommand command = UpdateEgliseMaisonCommand.builder()
                .id(id)
                .nom(request.getNom())
                .egliseLocaleId(request.getEgliseLocaleId())
                .leaderId(request.getLeaderId())
                .adresse(request.getAdresse())
                .build();

        EgliseMaisonResponse response = updateEgliseMaisonUseCase.execute(command);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('PASTEUR', 'ADMIN')")
    @Operation(summary = "Supprimer une église de maison")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Église de maison supprimée"),
        @ApiResponse(responseCode = "404", description = "Église de maison non trouvée"),
        @ApiResponse(responseCode = "409", description = "Des fidèles sont rattachés à cette église")
    })
    public ResponseEntity<Void> delete(
            @Parameter(description = "ID de l'église de maison") @PathVariable UUID id) {

        log.info("Deleting eglise de maison {}", id);
        deleteEgliseMaisonUseCase.execute(id);
        return ResponseEntity.noContent().build();
    }
}
