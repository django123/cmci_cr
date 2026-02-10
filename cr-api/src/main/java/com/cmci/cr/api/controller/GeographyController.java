package com.cmci.cr.api.controller;

import com.cmci.cr.application.usecase.SeedGeographyUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/admin/geography")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Géographie", description = "API d'initialisation des données géographiques")
@SecurityRequirement(name = "Bearer Authentication")
@CrossOrigin(origins = "*")
public class GeographyController {

    private final SeedGeographyUseCase seedGeographyUseCase;

    @PostMapping("/seed")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Initialiser les régions et zones depuis l'API RestCountries",
            description = "Récupère les continents et pays depuis l'API RestCountries et crée les régions/zones correspondantes. Les doublons sont ignorés.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Seed exécuté avec succès"),
        @ApiResponse(responseCode = "500", description = "Erreur lors de l'appel à l'API externe")
    })
    public ResponseEntity<SeedGeographyUseCase.SeedResult> seedGeography() {
        log.info("Seed geography requested");
        SeedGeographyUseCase.SeedResult result = seedGeographyUseCase.execute();
        return ResponseEntity.ok(result);
    }
}
