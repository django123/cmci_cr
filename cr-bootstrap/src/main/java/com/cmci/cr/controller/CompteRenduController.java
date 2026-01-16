package com.cmci.cr.controller;

import com.cmci.cr.application.dto.command.CreateCRCommand;
import com.cmci.cr.application.dto.command.UpdateCRCommand;
import com.cmci.cr.application.dto.response.CRResponse;
import com.cmci.cr.application.usecase.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * REST Controller pour la gestion des Comptes Rendus
 */
@RestController
@RequestMapping("/v1/comptes-rendus")
@Tag(name = "Comptes Rendus", description = "API de gestion des comptes rendus quotidiens")
public class CompteRenduController {

    private final CreateCRUseCase createCRUseCase;
    private final UpdateCRUseCase updateCRUseCase;
    private final GetCRUseCase getCRUseCase;
    private final DeleteCRUseCase deleteCRUseCase;
    private final ValidateCRUseCase validateCRUseCase;
    private final MarkCRAsViewedUseCase markCRAsViewedUseCase;

    public CompteRenduController(CreateCRUseCase createCRUseCase,
                                 UpdateCRUseCase updateCRUseCase,
                                 GetCRUseCase getCRUseCase,
                                 DeleteCRUseCase deleteCRUseCase,
                                 ValidateCRUseCase validateCRUseCase,
                                 MarkCRAsViewedUseCase markCRAsViewedUseCase) {
        this.createCRUseCase = createCRUseCase;
        this.updateCRUseCase = updateCRUseCase;
        this.getCRUseCase = getCRUseCase;
        this.deleteCRUseCase = deleteCRUseCase;
        this.validateCRUseCase = validateCRUseCase;
        this.markCRAsViewedUseCase = markCRAsViewedUseCase;
    }

    @PostMapping
    @Operation(summary = "Créer un nouveau compte rendu",
               description = "Crée un nouveau compte rendu quotidien pour un utilisateur")
    public ResponseEntity<CRResponse> createCompteRendu(
            @Valid @RequestBody CreateCRCommand command) {
        CRResponse response = createCRUseCase.execute(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Mettre à jour un compte rendu",
               description = "Met à jour un compte rendu existant")
    public ResponseEntity<CRResponse> updateCompteRendu(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateCRCommand command) {
        // Rebuild command with the ID from path parameter
        UpdateCRCommand commandWithId = UpdateCRCommand.builder()
                .id(id)
                .utilisateurId(command.getUtilisateurId())
                .rdqd(command.getRdqd())
                .priereSeule(command.getPriereSeule())
                .lectureBiblique(command.getLectureBiblique())
                .livreBiblique(command.getLivreBiblique())
                .litteraturePages(command.getLitteraturePages())
                .litteratureTotal(command.getLitteratureTotal())
                .litteratureTitre(command.getLitteratureTitre())
                .priereAutres(command.getPriereAutres())
                .confession(command.getConfession())
                .jeune(command.getJeune())
                .typeJeune(command.getTypeJeune())
                .evangelisation(command.getEvangelisation())
                .offrande(command.getOffrande())
                .notes(command.getNotes())
                .build();
        CRResponse response = updateCRUseCase.execute(commandWithId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Récupérer un compte rendu par ID",
               description = "Récupère les détails d'un compte rendu spécifique")
    public ResponseEntity<CRResponse> getCompteRendu(@PathVariable UUID id) {
        CRResponse response = getCRUseCase.getById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/utilisateur/{utilisateurId}")
    @Operation(summary = "Récupérer les comptes rendus d'un utilisateur",
               description = "Récupère tous les comptes rendus d'un utilisateur pour une période donnée")
    public ResponseEntity<List<CRResponse>> getCompteRendusByUtilisateur(
            @PathVariable UUID utilisateurId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        LocalDate start = startDate != null ? startDate : LocalDate.now().minusMonths(1);
        LocalDate end = endDate != null ? endDate : LocalDate.now();

        List<CRResponse> responses = getCRUseCase.getByUtilisateurIdAndDateRange(utilisateurId, start, end);
        return ResponseEntity.ok(responses);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer un compte rendu",
               description = "Supprime un compte rendu (soft delete)")
    public ResponseEntity<Void> deleteCompteRendu(
            @PathVariable UUID id,
            @RequestParam UUID utilisateurId) {
        deleteCRUseCase.execute(id, utilisateurId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/valider")
    @Operation(summary = "Valider un compte rendu",
               description = "Valide un compte rendu (réservé aux FD/Leaders/Pasteurs)")
    public ResponseEntity<CRResponse> validerCompteRendu(
            @PathVariable UUID id,
            @RequestParam UUID validatorId) {
        CRResponse response = validateCRUseCase.execute(id, validatorId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/marquer-vu")
    @Operation(summary = "Marquer un compte rendu comme vu",
               description = "Marque un compte rendu comme vu par le FD")
    public ResponseEntity<CRResponse> marquerCommeVu(
            @PathVariable UUID id,
            @RequestParam UUID fdId) {
        CRResponse response = markCRAsViewedUseCase.execute(id, fdId);
        return ResponseEntity.ok(response);
    }
}
