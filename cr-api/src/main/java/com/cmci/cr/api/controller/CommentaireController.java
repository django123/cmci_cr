package com.cmci.cr.api.controller;

import com.cmci.cr.api.dto.request.AddCommentaireRequest;
import com.cmci.cr.api.dto.response.CommentaireResponse;
import com.cmci.cr.api.mapper.CommentaireApiMapper;
import com.cmci.cr.application.dto.command.AddCommentaireCommand;
import com.cmci.cr.application.usecase.AddCommentaireUseCase;
import com.cmci.cr.application.usecase.GetCommentairesUseCase;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * REST Controller pour la gestion des Commentaires
 */
@RestController
@RequestMapping("/api/v1/cr/{compteRenduId}/commentaires")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Commentaires", description = "API de gestion des commentaires sur les comptes rendus")
@SecurityRequirement(name = "Bearer Authentication")
public class CommentaireController {

    private final AddCommentaireUseCase addCommentaireUseCase;
    private final GetCommentairesUseCase getCommentairesUseCase;
    private final CommentaireApiMapper mapper;
    private final SecurityContextService securityContextService;

    @PostMapping
    @Operation(summary = "Ajouter un commentaire",
               description = "Ajoute un commentaire à un compte rendu")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Commentaire ajouté avec succès"),
        @ApiResponse(responseCode = "400", description = "Données invalides"),
        @ApiResponse(responseCode = "401", description = "Non authentifié"),
        @ApiResponse(responseCode = "404", description = "Compte rendu non trouvé")
    })
    public ResponseEntity<CommentaireResponse> addCommentaire(
            @Parameter(description = "ID du compte rendu") @PathVariable UUID compteRenduId,
            @Valid @RequestBody AddCommentaireRequest request) {

        UUID auteurId = securityContextService.getCurrentUserId()
                .orElseThrow(() -> new IllegalStateException("Utilisateur non authentifié"));

        log.info("Adding comment to CR {} by user {}", compteRenduId, auteurId);

        AddCommentaireCommand command = mapper.toCommand(compteRenduId, auteurId, request);
        com.cmci.cr.application.dto.response.CommentaireResponse response =
                addCommentaireUseCase.execute(command);

        CommentaireResponse apiResponse = mapper.toApiResponse(response);

        return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
    }

    @GetMapping
    @Operation(summary = "Récupérer les commentaires d'un compte rendu",
               description = "Retourne tous les commentaires d'un compte rendu, triés par date croissante")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Liste des commentaires"),
        @ApiResponse(responseCode = "404", description = "Compte rendu non trouvé")
    })
    public ResponseEntity<List<CommentaireResponse>> getCommentaires(
            @Parameter(description = "ID du compte rendu") @PathVariable UUID compteRenduId) {

        log.info("Getting comments for CR {}", compteRenduId);

        List<com.cmci.cr.application.dto.response.CommentaireResponse> responses =
                getCommentairesUseCase.getByCompteRenduId(compteRenduId);

        List<CommentaireResponse> apiResponses = responses.stream()
                .map(mapper::toApiResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(apiResponses);
    }
}
