package com.cmci.cr.controller;

import com.cmci.cr.application.dto.command.AssignFDCommand;
import com.cmci.cr.application.dto.command.ChangeRoleCommand;
import com.cmci.cr.application.dto.command.CreateUtilisateurCommand;
import com.cmci.cr.application.dto.command.UpdateUtilisateurCommand;
import com.cmci.cr.application.dto.response.UtilisateurResponse;
import com.cmci.cr.application.usecase.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST Controller pour la gestion des Utilisateurs
 */
@RestController
@RequestMapping("/v1/utilisateurs")
@Tag(name = "Utilisateurs", description = "API de gestion des utilisateurs et disciples")
public class UtilisateurController {

    private final CreateUtilisateurUseCase createUtilisateurUseCase;
    private final GetUtilisateurUseCase getUtilisateurUseCase;
    private final UpdateUtilisateurUseCase updateUtilisateurUseCase;
    private final AssignFDUseCase assignFDUseCase;
    private final ChangeRoleUseCase changeRoleUseCase;

    public UtilisateurController(CreateUtilisateurUseCase createUtilisateurUseCase,
                                 GetUtilisateurUseCase getUtilisateurUseCase,
                                 UpdateUtilisateurUseCase updateUtilisateurUseCase,
                                 AssignFDUseCase assignFDUseCase,
                                 ChangeRoleUseCase changeRoleUseCase) {
        this.createUtilisateurUseCase = createUtilisateurUseCase;
        this.getUtilisateurUseCase = getUtilisateurUseCase;
        this.updateUtilisateurUseCase = updateUtilisateurUseCase;
        this.assignFDUseCase = assignFDUseCase;
        this.changeRoleUseCase = changeRoleUseCase;
    }

    @PostMapping
    @Operation(summary = "Créer un nouvel utilisateur",
               description = "Crée un nouveau disciple dans le système")
    public ResponseEntity<UtilisateurResponse> createUtilisateur(
            @Valid @RequestBody CreateUtilisateurCommand command) {
        UtilisateurResponse response = createUtilisateurUseCase.execute(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Récupérer un utilisateur par ID",
               description = "Récupère les détails d'un utilisateur spécifique")
    public ResponseEntity<UtilisateurResponse> getUtilisateur(@PathVariable UUID id) {
        UtilisateurResponse response = getUtilisateurUseCase.getById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/email/{email}")
    @Operation(summary = "Récupérer un utilisateur par email",
               description = "Récupère les détails d'un utilisateur par son email")
    public ResponseEntity<UtilisateurResponse> getUtilisateurByEmail(@PathVariable String email) {
        UtilisateurResponse response = getUtilisateurUseCase.getByEmail(email);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/fd/{fdId}/disciples")
    @Operation(summary = "Récupérer les disciples d'un FD",
               description = "Récupère tous les disciples assignés à un Faiseur de Disciples")
    public ResponseEntity<List<UtilisateurResponse>> getDisciplesByFd(
            @PathVariable @Parameter(description = "ID du FD") UUID fdId) {
        List<UtilisateurResponse> responses = getUtilisateurUseCase.getDisciplesByFdId(fdId);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/eglise-maison/{egliseMaisonId}")
    @Operation(summary = "Récupérer les membres d'une église de maison",
               description = "Récupère tous les membres d'une église de maison spécifique")
    public ResponseEntity<List<UtilisateurResponse>> getUtilisateursByEgliseMaison(
            @PathVariable @Parameter(description = "ID de l'église de maison") UUID egliseMaisonId) {
        List<UtilisateurResponse> responses = getUtilisateurUseCase.getByEgliseMaisonId(egliseMaisonId);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/role/{role}")
    @Operation(summary = "Récupérer les utilisateurs par rôle",
               description = "Récupère tous les utilisateurs ayant un rôle spécifique (FIDELE, FD, LEADER, PASTEUR, ADMIN)")
    public ResponseEntity<List<UtilisateurResponse>> getUtilisateursByRole(
            @PathVariable @Parameter(description = "Rôle (FIDELE, FD, LEADER, PASTEUR, ADMIN)") String role) {
        List<UtilisateurResponse> responses = getUtilisateurUseCase.getByRole(role);
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Mettre à jour un utilisateur",
               description = "Met à jour les informations d'un utilisateur existant")
    public ResponseEntity<UtilisateurResponse> updateUtilisateur(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateUtilisateurCommand command) {
        // Rebuild command with the ID from path parameter
        UpdateUtilisateurCommand commandWithId = UpdateUtilisateurCommand.builder()
                .id(id)
                .email(command.getEmail())
                .nom(command.getNom())
                .prenom(command.getPrenom())
                .egliseMaisonId(command.getEgliseMaisonId())
                .avatarUrl(command.getAvatarUrl())
                .telephone(command.getTelephone())
                .dateNaissance(command.getDateNaissance())
                .dateBapteme(command.getDateBapteme())
                .build();
        UtilisateurResponse response = updateUtilisateurUseCase.execute(commandWithId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{discipleId}/assign-fd")
    @Operation(summary = "Assigner un FD à un disciple",
               description = "Assigne ou désassigne un Faiseur de Disciples à un disciple")
    public ResponseEntity<UtilisateurResponse> assignFD(
            @PathVariable @Parameter(description = "ID du disciple") UUID discipleId,
            @RequestParam(required = false) @Parameter(description = "ID du FD (null pour désassigner)") UUID fdId) {
        AssignFDCommand command = AssignFDCommand.builder()
                .discipleId(discipleId)
                .fdId(fdId)
                .build();
        UtilisateurResponse response = assignFDUseCase.execute(command);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{utilisateurId}/role")
    @Operation(summary = "Changer le rôle d'un utilisateur",
               description = "Change le rôle d'un utilisateur (réservé aux administrateurs)")
    public ResponseEntity<UtilisateurResponse> changeRole(
            @PathVariable @Parameter(description = "ID de l'utilisateur") UUID utilisateurId,
            @RequestParam @Parameter(description = "Nouveau rôle (FIDELE, FD, LEADER, PASTEUR, ADMIN)") String newRole) {
        ChangeRoleCommand command = ChangeRoleCommand.builder()
                .utilisateurId(utilisateurId)
                .newRole(newRole)
                .build();
        UtilisateurResponse response = changeRoleUseCase.execute(command);
        return ResponseEntity.ok(response);
    }
}
