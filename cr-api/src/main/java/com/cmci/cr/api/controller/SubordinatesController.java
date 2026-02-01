package com.cmci.cr.api.controller;

import com.cmci.cr.api.dto.response.SubordinateStatisticsApiResponse;
import com.cmci.cr.api.dto.response.SubordinateWithCRsApiResponse;
import com.cmci.cr.api.mapper.SubordinatesApiMapper;
import com.cmci.cr.application.dto.response.DiscipleWithCRStatusResponse;
import com.cmci.cr.application.dto.response.SubordinateStatisticsResponse;
import com.cmci.cr.application.dto.response.SubordinateWithCRsResponse;
import com.cmci.cr.application.usecase.GetSubordinatesCRUseCase;
import com.cmci.cr.application.usecase.GetSubordinatesStatisticsUseCase;
import com.cmci.cr.application.usecase.ViewDisciplesCRUseCase;
import com.cmci.cr.infrastructure.security.SecurityContextService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * REST Controller pour la gestion de la visibilité hiérarchique des CR
 * Permet aux FD, Leaders et Pasteurs de voir les CR de leurs subordonnés
 */
@RestController
@RequestMapping("/v1/subordinates")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Subordonnés", description = "API de visibilité hiérarchique des CR")
@SecurityRequirement(name = "Bearer Authentication")
@CrossOrigin(origins = "*")
public class SubordinatesController {

    private final GetSubordinatesCRUseCase getSubordinatesCRUseCase;
    private final GetSubordinatesStatisticsUseCase getSubordinatesStatisticsUseCase;
    private final ViewDisciplesCRUseCase viewDisciplesCRUseCase;
    private final SubordinatesApiMapper mapper;
    private final SecurityContextService securityContextService;

    @GetMapping("/cr")
    @Operation(summary = "Récupérer les CR des subordonnés",
               description = "Récupère les CR des subordonnés selon la hiérarchie: "
                       + "FD → ses disciples, Leader → membres de son église de maison, "
                       + "Pasteur → membres de son église locale")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Liste des subordonnés avec leurs CR"),
        @ApiResponse(responseCode = "401", description = "Non authentifié"),
        @ApiResponse(responseCode = "403", description = "Accès refusé - rôle insuffisant")
    })
    @PreAuthorize("hasAnyRole('FD', 'LEADER', 'PASTEUR', 'ADMIN')")
    public ResponseEntity<List<SubordinateWithCRsApiResponse>> getSubordinatesCR(
            @Parameter(description = "Date de début")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "Date de fin")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        UUID responsableId = securityContextService.getCurrentUserId()
                .orElseThrow(() -> new IllegalStateException("Utilisateur non authentifié"));

        log.info("Getting subordinates CRs for user {} from {} to {}", responsableId, startDate, endDate);

        List<SubordinateWithCRsResponse> responses = getSubordinatesCRUseCase.execute(
                responsableId, startDate, endDate);

        List<SubordinateWithCRsApiResponse> apiResponses = mapper.toApiResponses(responses);

        return ResponseEntity.ok(apiResponses);
    }

    @GetMapping("/disciples")
    @Operation(summary = "Récupérer le statut CR des disciples (FD)",
               description = "Récupère un résumé du statut de CR de chaque disciple direct du FD")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Liste des disciples avec leur statut CR"),
        @ApiResponse(responseCode = "401", description = "Non authentifié"),
        @ApiResponse(responseCode = "403", description = "Accès refusé - rôle insuffisant")
    })
    @PreAuthorize("hasAnyRole('FD', 'LEADER', 'PASTEUR', 'ADMIN')")
    public ResponseEntity<List<DiscipleWithCRStatusResponse>> getDisciplesStatus() {

        UUID fdId = securityContextService.getCurrentUserId()
                .orElseThrow(() -> new IllegalStateException("Utilisateur non authentifié"));

        log.info("Getting disciples status for FD {}", fdId);

        List<DiscipleWithCRStatusResponse> responses = viewDisciplesCRUseCase.execute(fdId);

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/cr/summary")
    @Operation(summary = "Récupérer un résumé des CR des subordonnés",
               description = "Récupère un résumé statistique des CR des subordonnés sans les détails des CR")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Résumé des subordonnés"),
        @ApiResponse(responseCode = "401", description = "Non authentifié"),
        @ApiResponse(responseCode = "403", description = "Accès refusé - rôle insuffisant")
    })
    @PreAuthorize("hasAnyRole('FD', 'LEADER', 'PASTEUR', 'ADMIN')")
    public ResponseEntity<List<SubordinateWithCRsApiResponse>> getSubordinatesCRSummary(
            @Parameter(description = "Date de début")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "Date de fin")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        UUID responsableId = securityContextService.getCurrentUserId()
                .orElseThrow(() -> new IllegalStateException("Utilisateur non authentifié"));

        log.info("Getting subordinates CR summary for user {} from {} to {}", responsableId, startDate, endDate);

        List<SubordinateWithCRsResponse> responses = getSubordinatesCRUseCase.execute(
                responsableId, startDate, endDate);

        // Convertir sans inclure les détails des CR (juste le résumé)
        List<SubordinateWithCRsApiResponse> apiResponses = responses.stream()
                .map(r -> SubordinateWithCRsApiResponse.builder()
                        .utilisateurId(r.getUtilisateurId())
                        .nom(r.getNom())
                        .prenom(r.getPrenom())
                        .nomComplet(r.getNomComplet())
                        .email(r.getEmail())
                        .role(r.getRole())
                        .roleDisplayName(r.getRoleDisplayName())
                        .avatarUrl(r.getAvatarUrl())
                        .lastCRDate(r.getLastCRDate())
                        .daysSinceLastCR(r.getDaysSinceLastCR())
                        .regularityRate(r.getRegularityRate())
                        .totalCRs(r.getTotalCRs())
                        .alertLevel(r.getAlertLevel())
                        .hasAlert(r.getHasAlert())
                        .compteRendus(List.of()) // Pas de détails
                        .build())
                .toList();

        return ResponseEntity.ok(apiResponses);
    }

    @GetMapping("/statistics")
    @Operation(summary = "Récupérer les statistiques des subordonnés",
               description = "Récupère les statistiques détaillées de chaque subordonné selon la hiérarchie: "
                       + "FD → ses disciples, Leader → membres de son église de maison, "
                       + "Pasteur → membres de son église locale")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Liste des subordonnés avec leurs statistiques"),
        @ApiResponse(responseCode = "401", description = "Non authentifié"),
        @ApiResponse(responseCode = "403", description = "Accès refusé - rôle insuffisant")
    })
    @PreAuthorize("hasAnyRole('FD', 'LEADER', 'PASTEUR', 'ADMIN')")
    public ResponseEntity<List<SubordinateStatisticsApiResponse>> getSubordinatesStatistics(
            @Parameter(description = "Date de début")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "Date de fin")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        UUID responsableId = securityContextService.getCurrentUserId()
                .orElseThrow(() -> new IllegalStateException("Utilisateur non authentifié"));

        log.info("Getting subordinates statistics for user {} from {} to {}", responsableId, startDate, endDate);

        List<SubordinateStatisticsResponse> responses = getSubordinatesStatisticsUseCase.execute(
                responsableId, startDate, endDate);

        List<SubordinateStatisticsApiResponse> apiResponses = mapper.toStatisticsApiResponses(responses);

        return ResponseEntity.ok(apiResponses);
    }
}
