package com.cmci.cr.api.controller;

import com.cmci.cr.api.dto.response.StatisticsResponse;
import com.cmci.cr.api.mapper.StatisticsApiMapper;
import com.cmci.cr.application.dto.response.ExportResponse;
import com.cmci.cr.application.dto.response.PersonalStatisticsResponse;
import com.cmci.cr.application.usecase.ExportGroupStatsUseCase;
import com.cmci.cr.application.usecase.ExportPersonalStatsUseCase;
import com.cmci.cr.application.usecase.GetPersonalStatisticsUseCase;
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
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

/**
 * REST Controller pour les statistiques
 */
@RestController
@RequestMapping("/v1/statistics")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Statistiques", description = "API de statistiques et rapports")
@SecurityRequirement(name = "Bearer Authentication")
@CrossOrigin(origins = "*")
public class StatisticsController {

    private final GetPersonalStatisticsUseCase getPersonalStatisticsUseCase;
    private final ExportPersonalStatsUseCase exportPersonalStatsUseCase;
    private final ExportGroupStatsUseCase exportGroupStatsUseCase;
    private final StatisticsApiMapper mapper;
    private final SecurityContextService securityContextService;

    @GetMapping("/personal")
    @Operation(summary = "Statistiques personnelles",
               description = "Récupère les statistiques personnelles de l'utilisateur authentifié sur une période")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Statistiques récupérées"),
        @ApiResponse(responseCode = "400", description = "Dates invalides"),
        @ApiResponse(responseCode = "401", description = "Non authentifié")
    })
    public ResponseEntity<StatisticsResponse> getPersonalStatistics(
            @Parameter(description = "Date de début") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "Date de fin") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        UUID utilisateurId = securityContextService.getCurrentUserId()
                .orElseThrow(() -> new IllegalStateException("Utilisateur non authentifié"));

        log.info("Getting personal statistics for user {} from {} to {}", utilisateurId, startDate, endDate);

        PersonalStatisticsResponse response = getPersonalStatisticsUseCase.execute(
                utilisateurId, startDate, endDate);

        StatisticsResponse apiResponse = mapper.toApiResponse(response);

        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/user/{utilisateurId}")
    @Operation(summary = "Statistiques d'un utilisateur",
               description = "Récupère les statistiques d'un utilisateur spécifique (FD, Leader, Pasteur, Admin)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Statistiques récupérées"),
        @ApiResponse(responseCode = "403", description = "Accès refusé"),
        @ApiResponse(responseCode = "404", description = "Utilisateur non trouvé")
    })
    @PreAuthorize("hasAnyRole('FD', 'LEADER', 'PASTEUR', 'ADMIN')")
    public ResponseEntity<StatisticsResponse> getUserStatistics(
            @Parameter(description = "ID de l'utilisateur") @PathVariable UUID utilisateurId,
            @Parameter(description = "Date de début") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "Date de fin") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        log.info("Getting statistics for user {} from {} to {}", utilisateurId, startDate, endDate);

        PersonalStatisticsResponse response = getPersonalStatisticsUseCase.execute(
                utilisateurId, startDate, endDate);

        StatisticsResponse apiResponse = mapper.toApiResponse(response);

        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/personal/export")
    @Operation(summary = "Exporter statistiques personnelles",
               description = "Exporte les statistiques personnelles en PDF ou Excel")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Fichier généré"),
        @ApiResponse(responseCode = "400", description = "Format ou dates invalides"),
        @ApiResponse(responseCode = "401", description = "Non authentifié")
    })
    public ResponseEntity<byte[]> exportPersonalStatistics(
            @Parameter(description = "Format d'export (pdf ou excel)") @RequestParam(defaultValue = "pdf") String format,
            @Parameter(description = "Date de début") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "Date de fin") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        UUID utilisateurId = securityContextService.getCurrentUserId()
                .orElseThrow(() -> new IllegalStateException("Utilisateur non authentifié"));

        log.info("Exporting personal statistics for user {} from {} to {} as {}", utilisateurId, startDate, endDate, format);

        ExportResponse export = exportPersonalStatsUseCase.execute(utilisateurId, startDate, endDate, format);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + export.getFilename() + "\"")
                .contentType(MediaType.parseMediaType(export.getContentType()))
                .contentLength(export.getContent().length)
                .body(export.getContent());
    }

    @GetMapping("/group/export")
    @Operation(summary = "Exporter statistiques de groupe",
               description = "Exporte les statistiques de groupe en PDF ou Excel (FD, Leader, Pasteur, Admin)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Fichier généré"),
        @ApiResponse(responseCode = "400", description = "Format ou dates invalides"),
        @ApiResponse(responseCode = "403", description = "Accès refusé")
    })
    @PreAuthorize("hasAnyRole('FD', 'LEADER', 'PASTEUR', 'ADMIN')")
    public ResponseEntity<byte[]> exportGroupStatistics(
            @Parameter(description = "Format d'export (pdf ou excel)") @RequestParam(defaultValue = "pdf") String format,
            @Parameter(description = "Date de début") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "Date de fin") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        UUID utilisateurId = securityContextService.getCurrentUserId()
                .orElseThrow(() -> new IllegalStateException("Utilisateur non authentifié"));

        log.info("Exporting group statistics for FD {} from {} to {} as {}", utilisateurId, startDate, endDate, format);

        ExportResponse export = exportGroupStatsUseCase.execute(utilisateurId, startDate, endDate, format);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + export.getFilename() + "\"")
                .contentType(MediaType.parseMediaType(export.getContentType()))
                .contentLength(export.getContent().length)
                .body(export.getContent());
    }
}
