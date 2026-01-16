package com.cmci.cr.controller;

import com.cmci.cr.application.dto.response.GroupStatisticsResponse;
import com.cmci.cr.application.dto.response.PersonalStatisticsResponse;
import com.cmci.cr.application.usecase.GetGroupStatisticsUseCase;
import com.cmci.cr.application.usecase.GetPersonalStatisticsUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

/**
 * REST Controller pour les statistiques
 */
@RestController
@RequestMapping("/v1/statistiques")
@Tag(name = "Statistiques", description = "API de consultation des statistiques personnelles et de groupe")
public class StatisticsController {

    private final GetPersonalStatisticsUseCase getPersonalStatisticsUseCase;
    private final GetGroupStatisticsUseCase getGroupStatisticsUseCase;

    public StatisticsController(GetPersonalStatisticsUseCase getPersonalStatisticsUseCase,
                                GetGroupStatisticsUseCase getGroupStatisticsUseCase) {
        this.getPersonalStatisticsUseCase = getPersonalStatisticsUseCase;
        this.getGroupStatisticsUseCase = getGroupStatisticsUseCase;
    }

    @GetMapping("/personnel/{utilisateurId}")
    @Operation(summary = "Statistiques personnelles",
               description = "Récupère les statistiques personnelles d'un utilisateur sur une période")
    public ResponseEntity<PersonalStatisticsResponse> getPersonalStatistics(
            @PathVariable UUID utilisateurId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        PersonalStatisticsResponse response = getPersonalStatisticsUseCase.execute(
                utilisateurId, startDate, endDate);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/groupe/{fdId}")
    @Operation(summary = "Statistiques de groupe",
               description = "Récupère les statistiques d'un groupe (disciples d'un FD)")
    public ResponseEntity<GroupStatisticsResponse> getGroupStatistics(
            @PathVariable UUID fdId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        GroupStatisticsResponse response = getGroupStatisticsUseCase.execute(
                fdId, startDate, endDate);
        return ResponseEntity.ok(response);
    }
}
