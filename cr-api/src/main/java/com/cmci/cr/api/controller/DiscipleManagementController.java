package com.cmci.cr.api.controller;

import com.cmci.cr.api.dto.request.AssignDiscipleToFDRequest;
import com.cmci.cr.api.dto.response.DiscipleApiResponse;
import com.cmci.cr.application.dto.command.AssignFDCommand;
import com.cmci.cr.application.dto.response.UtilisateurResponse;
import com.cmci.cr.application.usecase.AssignFDUseCase;
import com.cmci.cr.application.usecase.GetUtilisateurUseCase;
import com.cmci.cr.domain.model.Utilisateur;
import com.cmci.cr.domain.repository.UtilisateurRepository;
import com.cmci.cr.domain.valueobject.Role;
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
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Controller pour la gestion des disciples et leur assignation aux FD
 */
@RestController
@RequestMapping("/v1/disciples")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Gestion des Disciples", description = "API de gestion des disciples et assignation aux FD")
@SecurityRequirement(name = "Bearer Authentication")
@CrossOrigin(origins = "*")
public class DiscipleManagementController {

    private final AssignFDUseCase assignFDUseCase;
    private final GetUtilisateurUseCase getUtilisateurUseCase;
    private final UtilisateurRepository utilisateurRepository;
    private final SecurityContextService securityContextService;

    @PostMapping("/{discipleId}/assign-fd")
    @Operation(summary = "Assigner un disciple à un FD",
               description = "Assigne un disciple (fidèle) à un FD pour l'accompagnement spirituel. "
                       + "Seuls les FD, Leaders, Pasteurs et Admins peuvent effectuer cette action.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Disciple assigné avec succès"),
        @ApiResponse(responseCode = "400", description = "Données invalides ou FD non valide"),
        @ApiResponse(responseCode = "403", description = "Permissions insuffisantes"),
        @ApiResponse(responseCode = "404", description = "Disciple ou FD non trouvé")
    })
    @PreAuthorize("hasAnyRole('FD', 'LEADER', 'PASTEUR', 'ADMIN')")
    public ResponseEntity<DiscipleApiResponse> assignDiscipleToFD(
            @Parameter(description = "ID du disciple à assigner")
            @PathVariable UUID discipleId,
            @Valid @RequestBody AssignDiscipleToFDRequest request) {

        UUID currentUserId = securityContextService.getCurrentUserId()
                .orElseThrow(() -> new IllegalStateException("Utilisateur non authentifié"));

        log.info("User {} assigning disciple {} to FD {}", currentUserId, discipleId, request.getFdId());

        // Vérifier les permissions
        validateAssignmentPermission(currentUserId, request.getFdId());

        AssignFDCommand command = AssignFDCommand.builder()
                .discipleId(discipleId)
                .fdId(request.getFdId())
                .build();

        UtilisateurResponse response = assignFDUseCase.execute(command);
        DiscipleApiResponse apiResponse = DiscipleApiResponse.fromApplicationResponse(response);

        return ResponseEntity.ok(apiResponse);
    }

    @DeleteMapping("/{discipleId}/fd")
    @Operation(summary = "Retirer un disciple de son FD",
               description = "Retire l'assignation d'un disciple à son FD actuel. "
                       + "Le disciple n'aura plus de FD assigné.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Assignation retirée avec succès"),
        @ApiResponse(responseCode = "403", description = "Permissions insuffisantes"),
        @ApiResponse(responseCode = "404", description = "Disciple non trouvé")
    })
    @PreAuthorize("hasAnyRole('FD', 'LEADER', 'PASTEUR', 'ADMIN')")
    public ResponseEntity<DiscipleApiResponse> removeDiscipleFromFD(
            @Parameter(description = "ID du disciple")
            @PathVariable UUID discipleId) {

        UUID currentUserId = securityContextService.getCurrentUserId()
                .orElseThrow(() -> new IllegalStateException("Utilisateur non authentifié"));

        log.info("User {} removing FD assignment from disciple {}", currentUserId, discipleId);

        // Vérifier que l'utilisateur a le droit de retirer l'assignation
        Utilisateur disciple = utilisateurRepository.findById(discipleId)
                .orElseThrow(() -> new IllegalArgumentException("Disciple non trouvé: " + discipleId));

        validateUnassignmentPermission(currentUserId, disciple.getFdId());

        AssignFDCommand command = AssignFDCommand.builder()
                .discipleId(discipleId)
                .fdId(null) // null pour désassigner
                .build();

        UtilisateurResponse response = assignFDUseCase.execute(command);
        DiscipleApiResponse apiResponse = DiscipleApiResponse.fromApplicationResponse(response);

        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/fd/{fdId}")
    @Operation(summary = "Lister les disciples d'un FD",
               description = "Récupère la liste de tous les disciples assignés à un FD spécifique")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Liste des disciples"),
        @ApiResponse(responseCode = "403", description = "Permissions insuffisantes"),
        @ApiResponse(responseCode = "404", description = "FD non trouvé")
    })
    @PreAuthorize("hasAnyRole('FD', 'LEADER', 'PASTEUR', 'ADMIN')")
    public ResponseEntity<List<DiscipleApiResponse>> getDisciplesByFD(
            @Parameter(description = "ID du FD")
            @PathVariable UUID fdId) {

        UUID currentUserId = securityContextService.getCurrentUserId()
                .orElseThrow(() -> new IllegalStateException("Utilisateur non authentifié"));

        log.info("User {} fetching disciples of FD {}", currentUserId, fdId);

        // Vérifier que le FD existe
        utilisateurRepository.findById(fdId)
                .orElseThrow(() -> new IllegalArgumentException("FD non trouvé: " + fdId));

        List<Utilisateur> disciples = utilisateurRepository.findByFdId(fdId);

        List<DiscipleApiResponse> responses = disciples.stream()
                .map(this::mapToApiResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/my-disciples")
    @Operation(summary = "Lister mes disciples",
               description = "Récupère la liste des disciples assignés à l'utilisateur connecté (FD)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Liste de mes disciples"),
        @ApiResponse(responseCode = "403", description = "Permissions insuffisantes")
    })
    @PreAuthorize("hasAnyRole('FD', 'LEADER', 'PASTEUR', 'ADMIN')")
    public ResponseEntity<List<DiscipleApiResponse>> getMyDisciples() {

        UUID currentUserId = securityContextService.getCurrentUserId()
                .orElseThrow(() -> new IllegalStateException("Utilisateur non authentifié"));

        log.info("User {} fetching their disciples", currentUserId);

        List<Utilisateur> disciples = utilisateurRepository.findByFdId(currentUserId);

        List<DiscipleApiResponse> responses = disciples.stream()
                .map(this::mapToApiResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/unassigned")
    @Operation(summary = "Lister les disciples sans FD",
               description = "Récupère la liste des fidèles qui n'ont pas encore de FD assigné")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Liste des disciples sans FD"),
        @ApiResponse(responseCode = "403", description = "Permissions insuffisantes")
    })
    @PreAuthorize("hasAnyRole('LEADER', 'PASTEUR', 'ADMIN')")
    public ResponseEntity<List<DiscipleApiResponse>> getUnassignedDisciples() {

        UUID currentUserId = securityContextService.getCurrentUserId()
                .orElseThrow(() -> new IllegalStateException("Utilisateur non authentifié"));

        log.info("User {} fetching unassigned disciples", currentUserId);

        // Récupérer tous les fidèles sans FD
        List<Utilisateur> allFideles = utilisateurRepository.findByRole(Role.FIDELE);

        List<DiscipleApiResponse> responses = allFideles.stream()
                .filter(u -> u.getFdId() == null)
                .map(this::mapToApiResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/count/fd/{fdId}")
    @Operation(summary = "Compter les disciples d'un FD",
               description = "Retourne le nombre de disciples assignés à un FD")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Nombre de disciples")
    })
    @PreAuthorize("hasAnyRole('FD', 'LEADER', 'PASTEUR', 'ADMIN')")
    public ResponseEntity<Long> countDisciplesByFD(
            @Parameter(description = "ID du FD")
            @PathVariable UUID fdId) {

        log.info("Counting disciples of FD {}", fdId);

        long count = utilisateurRepository.countByFdId(fdId);

        return ResponseEntity.ok(count);
    }

    /**
     * Vérifie que l'utilisateur a le droit d'assigner un disciple à un FD
     */
    private void validateAssignmentPermission(UUID currentUserId, UUID targetFdId) {
        Utilisateur currentUser = utilisateurRepository.findById(currentUserId)
                .orElseThrow(() -> new IllegalStateException("Utilisateur courant non trouvé"));

        // ADMIN peut tout faire
        if (currentUser.getRole() == Role.ADMIN) {
            return;
        }

        // Un FD peut s'assigner des disciples à lui-même
        if (currentUser.getRole() == Role.FD && currentUserId.equals(targetFdId)) {
            return;
        }

        // PASTEUR et LEADER peuvent assigner des disciples aux FD de leur église
        if (currentUser.getRole() == Role.PASTEUR || currentUser.getRole() == Role.LEADER) {
            return;
        }

        // Un FD ne peut pas assigner des disciples à un autre FD
        if (currentUser.getRole() == Role.FD && !currentUserId.equals(targetFdId)) {
            throw new SecurityException("Un FD ne peut assigner des disciples qu'à lui-même");
        }
    }

    /**
     * Vérifie que l'utilisateur a le droit de retirer l'assignation
     */
    private void validateUnassignmentPermission(UUID currentUserId, UUID currentFdId) {
        Utilisateur currentUser = utilisateurRepository.findById(currentUserId)
                .orElseThrow(() -> new IllegalStateException("Utilisateur courant non trouvé"));

        // ADMIN peut tout faire
        if (currentUser.getRole() == Role.ADMIN) {
            return;
        }

        // PASTEUR et LEADER peuvent retirer des assignations
        if (currentUser.getRole() == Role.PASTEUR || currentUser.getRole() == Role.LEADER) {
            return;
        }

        // Un FD peut retirer ses propres disciples
        if (currentUser.getRole() == Role.FD && currentUserId.equals(currentFdId)) {
            return;
        }

        throw new SecurityException("Vous n'avez pas les droits pour retirer cette assignation");
    }

    /**
     * Mappe un Utilisateur vers DiscipleApiResponse
     */
    private DiscipleApiResponse mapToApiResponse(Utilisateur utilisateur) {
        String fdNom = null;
        if (utilisateur.getFdId() != null) {
            fdNom = utilisateurRepository.findById(utilisateur.getFdId())
                    .map(Utilisateur::getNomComplet)
                    .orElse(null);
        }

        return DiscipleApiResponse.builder()
                .id(utilisateur.getId())
                .email(utilisateur.getEmail())
                .nom(utilisateur.getNom())
                .prenom(utilisateur.getPrenom())
                .nomComplet(utilisateur.getNomComplet())
                .role(utilisateur.getRole().name())
                .egliseMaisonId(utilisateur.getEgliseMaisonId())
                .fdId(utilisateur.getFdId())
                .fdNom(fdNom)
                .avatarUrl(utilisateur.getAvatarUrl())
                .telephone(utilisateur.getTelephone())
                .dateNaissance(utilisateur.getDateNaissance())
                .dateBapteme(utilisateur.getDateBapteme())
                .statut(utilisateur.getStatut() != null ? utilisateur.getStatut().name() : null)
                .createdAt(utilisateur.getCreatedAt())
                .updatedAt(utilisateur.getUpdatedAt())
                .build();
    }
}
