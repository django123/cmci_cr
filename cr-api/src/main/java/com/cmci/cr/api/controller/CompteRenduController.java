package com.cmci.cr.api.controller;

import com.cmci.cr.api.dto.request.CreateCompteRenduRequest;
import com.cmci.cr.api.dto.request.UpdateCompteRenduRequest;
import com.cmci.cr.api.dto.response.CompteRenduResponse;
import com.cmci.cr.api.mapper.CompteRenduApiMapper;
import com.cmci.cr.application.dto.command.CreateCRCommand;
import com.cmci.cr.application.dto.command.UpdateCRCommand;
import com.cmci.cr.application.dto.response.CRResponse;
import com.cmci.cr.application.usecase.*;
import com.cmci.cr.infrastructure.security.SecurityContextService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * REST Controller pour la gestion des Comptes Rendus
 */
@RestController
@RequestMapping("/api/v1/cr")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Comptes Rendus", description = "API de gestion des comptes rendus quotidiens")
@SecurityRequirement(name = "Bearer Authentication")
public class CompteRenduController {

    private final CreateCRUseCase createCRUseCase;
    private final UpdateCRUseCase updateCRUseCase;
    private final GetCRUseCase getCRUseCase;
    private final DeleteCRUseCase deleteCRUseCase;
    private final ValidateCRUseCase validateCRUseCase;
    private final MarkCRAsViewedUseCase markAsViewedUseCase;
    private final CompteRenduApiMapper mapper;
    private final SecurityContextService securityContextService;

    @PostMapping
    @Operation(summary = "Créer un nouveau compte rendu",
               description = "Crée un compte rendu pour l'utilisateur authentifié")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Compte rendu créé avec succès"),
        @ApiResponse(responseCode = "400", description = "Données invalides"),
        @ApiResponse(responseCode = "401", description = "Non authentifié"),
        @ApiResponse(responseCode = "409", description = "Un compte rendu existe déjà pour cette date")
    })
    public ResponseEntity<CompteRenduResponse> createCompteRendu(
            @Valid @RequestBody CreateCompteRenduRequest request) {

        UUID utilisateurId = securityContextService.getCurrentUserId()
                .orElseThrow(() -> new IllegalStateException("Utilisateur non authentifié"));

        log.info("Creating CR for user {} on date {}", utilisateurId, request.getDate());

        CreateCRCommand command = mapper.toCreateCommand(request, utilisateurId);
        CRResponse response = createCRUseCase.execute(command);
        CompteRenduResponse apiResponse = mapper.toApiResponse(response);

        return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Mettre à jour un compte rendu",
               description = "Met à jour un compte rendu existant (uniquement le propriétaire)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Compte rendu mis à jour"),
        @ApiResponse(responseCode = "400", description = "Données invalides"),
        @ApiResponse(responseCode = "403", description = "Accès refusé"),
        @ApiResponse(responseCode = "404", description = "Compte rendu non trouvé")
    })
    public ResponseEntity<CompteRenduResponse> updateCompteRendu(
            @Parameter(description = "ID du compte rendu") @PathVariable UUID id,
            @Valid @RequestBody UpdateCompteRenduRequest request) {

        log.info("Updating CR {}", id);

        UpdateCRCommand command = mapper.toUpdateCommand(id, request);
        CRResponse response = updateCRUseCase.execute(command);
        CompteRenduResponse apiResponse = mapper.toApiResponse(response);

        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Récupérer un compte rendu par ID",
               description = "Récupère les détails d'un compte rendu")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Compte rendu trouvé"),
        @ApiResponse(responseCode = "404", description = "Compte rendu non trouvé")
    })
    public ResponseEntity<CompteRenduResponse> getCompteRendu(
            @Parameter(description = "ID du compte rendu") @PathVariable UUID id) {

        log.info("Getting CR {}", id);

        CRResponse response = getCRUseCase.getById(id);
        CompteRenduResponse apiResponse = mapper.toApiResponse(response);
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/user/{utilisateurId}")
    @Operation(summary = "Récupérer tous les comptes rendus d'un utilisateur",
               description = "Retourne la liste des comptes rendus d'un utilisateur, triés par date décroissante")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Liste des comptes rendus"),
        @ApiResponse(responseCode = "403", description = "Accès refusé")
    })
    @PreAuthorize("hasAnyRole('FD', 'LEADER', 'PASTEUR', 'ADMIN') or #utilisateurId == authentication.principal.claims['user_id']")
    public ResponseEntity<List<CompteRenduResponse>> getUserCompteRendus(
            @Parameter(description = "ID de l'utilisateur") @PathVariable UUID utilisateurId) {

        log.info("Getting CRs for user {}", utilisateurId);

        List<CRResponse> responses = getCRUseCase.getByUtilisateurId(utilisateurId);
        List<CompteRenduResponse> apiResponses = responses.stream()
                .map(mapper::toApiResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(apiResponses);
    }

    @GetMapping("/user/{utilisateurId}/period")
    @Operation(summary = "Récupérer les comptes rendus d'un utilisateur sur une période",
               description = "Retourne les comptes rendus entre deux dates")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Liste des comptes rendus sur la période"),
        @ApiResponse(responseCode = "400", description = "Dates invalides")
    })
    @PreAuthorize("hasAnyRole('FD', 'LEADER', 'PASTEUR', 'ADMIN') or #utilisateurId == authentication.principal.claims['user_id']")
    public ResponseEntity<List<CompteRenduResponse>> getUserCompteRendusByPeriod(
            @Parameter(description = "ID de l'utilisateur") @PathVariable UUID utilisateurId,
            @Parameter(description = "Date de début") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "Date de fin") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        log.info("Getting CRs for user {} between {} and {}", utilisateurId, startDate, endDate);

        List<CRResponse> responses = getCRUseCase.getByUtilisateurIdAndDateRange(utilisateurId, startDate, endDate);
        List<CompteRenduResponse> apiResponses = responses.stream()
                .map(mapper::toApiResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(apiResponses);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer un compte rendu",
               description = "Supprime un compte rendu (uniquement le propriétaire)")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Compte rendu supprimé"),
        @ApiResponse(responseCode = "403", description = "Accès refusé"),
        @ApiResponse(responseCode = "404", description = "Compte rendu non trouvé")
    })
    public ResponseEntity<Void> deleteCompteRendu(
            @Parameter(description = "ID du compte rendu") @PathVariable UUID id) {

        UUID utilisateurId = securityContextService.getCurrentUserId()
                .orElseThrow(() -> new IllegalStateException("Utilisateur non authentifié"));

        log.info("Deleting CR {} by user {}", id, utilisateurId);

        deleteCRUseCase.execute(id, utilisateurId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/submit")
    @Operation(summary = "Soumettre un compte rendu",
               description = "Soumet un compte rendu pour validation par le FD")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Compte rendu soumis"),
        @ApiResponse(responseCode = "400", description = "Compte rendu déjà soumis"),
        @ApiResponse(responseCode = "404", description = "Compte rendu non trouvé")
    })
    public ResponseEntity<CompteRenduResponse> submitCompteRendu(
            @Parameter(description = "ID du compte rendu") @PathVariable UUID id) {

        log.info("Submitting CR {}", id);

        // La soumission se fait via l'update en changeant le statut
        CRResponse response = getCRUseCase.getById(id);

        // Vérifier que le CR est en brouillon
        if (!"BROUILLON".equals(response.getStatut())) {
            throw new IllegalStateException("Seuls les CR en brouillon peuvent être soumis");
        }

        // TODO: Implémenter SubmitCRUseCase si nécessaire
        // Pour l'instant, on retourne le CR tel quel
        CompteRenduResponse apiResponse = mapper.toApiResponse(response);
        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/{id}/validate")
    @Operation(summary = "Valider un compte rendu",
               description = "Valide un compte rendu (FD, Leader, Pasteur, Admin uniquement)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Compte rendu validé"),
        @ApiResponse(responseCode = "400", description = "Compte rendu non soumis"),
        @ApiResponse(responseCode = "403", description = "Permissions insuffisantes"),
        @ApiResponse(responseCode = "404", description = "Compte rendu non trouvé")
    })
    @PreAuthorize("hasAnyRole('FD', 'LEADER', 'PASTEUR', 'ADMIN')")
    public ResponseEntity<CompteRenduResponse> validateCompteRendu(
            @Parameter(description = "ID du compte rendu") @PathVariable UUID id) {

        UUID validatorId = securityContextService.getCurrentUserId()
                .orElseThrow(() -> new IllegalStateException("Utilisateur non authentifié"));

        log.info("Validating CR {} by user {}", id, validatorId);

        CRResponse response = validateCRUseCase.execute(id, validatorId);
        CompteRenduResponse apiResponse = mapper.toApiResponse(response);

        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/{id}/mark-viewed")
    @Operation(summary = "Marquer un compte rendu comme vu",
               description = "Marque un compte rendu comme vu par le FD")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Compte rendu marqué comme vu"),
        @ApiResponse(responseCode = "403", description = "Permissions insuffisantes"),
        @ApiResponse(responseCode = "404", description = "Compte rendu non trouvé")
    })
    @PreAuthorize("hasAnyRole('FD', 'LEADER', 'PASTEUR', 'ADMIN')")
    public ResponseEntity<CompteRenduResponse> markAsViewed(
            @Parameter(description = "ID du compte rendu") @PathVariable UUID id) {

        UUID viewerId = securityContextService.getCurrentUserId()
                .orElseThrow(() -> new IllegalStateException("Utilisateur non authentifié"));

        log.info("Marking CR {} as viewed by user {}", id, viewerId);

        CRResponse response = markAsViewedUseCase.execute(id, viewerId);
        CompteRenduResponse apiResponse = mapper.toApiResponse(response);

        return ResponseEntity.ok(apiResponse);
    }
}
