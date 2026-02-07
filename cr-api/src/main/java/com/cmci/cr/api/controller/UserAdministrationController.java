package com.cmci.cr.api.controller;

import com.cmci.cr.api.dto.request.AssignRoleRequest;
import com.cmci.cr.api.dto.response.KeycloakUserApiResponse;
import com.cmci.cr.api.dto.response.UserStatisticsApiResponse;
import com.cmci.cr.application.dto.command.AssignRoleCommand;
import com.cmci.cr.application.dto.response.KeycloakUserResponse;
import com.cmci.cr.application.usecase.UserAdministrationUseCase;
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
import java.util.stream.Collectors;

/**
 * Controller pour l'administration des utilisateurs et des rôles
 * Permet aux Pasteurs et Admins de gérer les membres de leur communauté
 */
@RestController
@RequestMapping("/v1/admin/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Administration Utilisateurs", description = "API d'administration des utilisateurs et rôles")
@SecurityRequirement(name = "Bearer Authentication")
@CrossOrigin(origins = "*")
public class UserAdministrationController {

    private final UserAdministrationUseCase userAdministrationUseCase;
    private final SecurityContextService securityContextService;

    @GetMapping
    @Operation(summary = "Lister tous les utilisateurs",
               description = "Récupère la liste de tous les utilisateurs avec pagination")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Liste des utilisateurs"),
        @ApiResponse(responseCode = "401", description = "Non authentifié"),
        @ApiResponse(responseCode = "403", description = "Accès refusé")
    })
    @PreAuthorize("hasAnyRole('LEADER', 'PASTEUR', 'ADMIN')")
    public ResponseEntity<List<KeycloakUserApiResponse>> getAllUsers(
            @Parameter(description = "Numéro de page (commence à 0)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Nombre d'éléments par page")
            @RequestParam(defaultValue = "20") int size) {

        log.info("Fetching all users - page: {}, size: {}", page, size);

        List<KeycloakUserResponse> users = userAdministrationUseCase.getAllUsers(page, size);
        List<KeycloakUserApiResponse> response = users.stream()
                .map(KeycloakUserApiResponse::fromApplicationResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/role/{role}")
    @Operation(summary = "Lister les utilisateurs par rôle",
               description = "Récupère la liste des utilisateurs ayant un rôle spécifique")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Liste des utilisateurs avec le rôle"),
        @ApiResponse(responseCode = "400", description = "Rôle invalide"),
        @ApiResponse(responseCode = "403", description = "Accès refusé")
    })
    @PreAuthorize("hasAnyRole('LEADER', 'PASTEUR', 'ADMIN')")
    public ResponseEntity<List<KeycloakUserApiResponse>> getUsersByRole(
            @Parameter(description = "Rôle à filtrer", example = "FIDELE")
            @PathVariable String role) {

        log.info("Fetching users with role: {}", role);

        try {
            Role targetRole = Role.valueOf(role.toUpperCase());
            List<KeycloakUserResponse> users = userAdministrationUseCase.getUsersByRole(targetRole);
            List<KeycloakUserApiResponse> response = users.stream()
                    .map(KeycloakUserApiResponse::fromApplicationResponse)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Rôle invalide: " + role);
        }
    }

    @GetMapping("/{userId}")
    @Operation(summary = "Récupérer un utilisateur",
               description = "Récupère les détails d'un utilisateur par son ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Détails de l'utilisateur"),
        @ApiResponse(responseCode = "404", description = "Utilisateur non trouvé"),
        @ApiResponse(responseCode = "403", description = "Accès refusé")
    })
    @PreAuthorize("hasAnyRole('LEADER', 'PASTEUR', 'ADMIN')")
    public ResponseEntity<KeycloakUserApiResponse> getUserById(
            @Parameter(description = "ID de l'utilisateur")
            @PathVariable String userId) {

        log.info("Fetching user by ID: {}", userId);

        KeycloakUserResponse user = userAdministrationUseCase.getUserById(userId);
        KeycloakUserApiResponse response = KeycloakUserApiResponse.fromApplicationResponse(user);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    @Operation(summary = "Rechercher des utilisateurs",
               description = "Recherche des utilisateurs par nom, prénom ou email")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Résultats de recherche"),
        @ApiResponse(responseCode = "403", description = "Accès refusé")
    })
    @PreAuthorize("hasAnyRole('LEADER', 'PASTEUR', 'ADMIN')")
    public ResponseEntity<List<KeycloakUserApiResponse>> searchUsers(
            @Parameter(description = "Terme de recherche", example = "Jean")
            @RequestParam String q,
            @Parameter(description = "Numéro de page")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Nombre d'éléments par page")
            @RequestParam(defaultValue = "20") int size) {

        log.info("Searching users with term: {} - page: {}, size: {}", q, page, size);

        List<KeycloakUserResponse> users = userAdministrationUseCase.searchUsers(q, page, size);
        List<KeycloakUserApiResponse> response = users.stream()
                .map(KeycloakUserApiResponse::fromApplicationResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{userId}/role")
    @Operation(summary = "Attribuer un rôle",
               description = "Attribue un nouveau rôle à un utilisateur. " +
                       "Un Pasteur peut promouvoir jusqu'au rang de Leader. " +
                       "Seul un Admin peut promouvoir au rang de Pasteur ou Admin.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Rôle attribué avec succès"),
        @ApiResponse(responseCode = "400", description = "Rôle invalide"),
        @ApiResponse(responseCode = "403", description = "Permissions insuffisantes"),
        @ApiResponse(responseCode = "404", description = "Utilisateur non trouvé")
    })
    @PreAuthorize("hasAnyRole('LEADER', 'PASTEUR', 'ADMIN')")
    public ResponseEntity<KeycloakUserApiResponse> assignRole(
            @Parameter(description = "ID de l'utilisateur")
            @PathVariable String userId,
            @Valid @RequestBody AssignRoleRequest request) {

        String currentUserSubject = securityContextService.getCurrentUserSubject()
                .orElseThrow(() -> new IllegalStateException("Utilisateur non authentifié"));

        log.info("User {} assigning role {} to user {}", currentUserSubject, request.getRole(), userId);

        try {
            Role newRole = Role.valueOf(request.getRole().toUpperCase());

            AssignRoleCommand command = AssignRoleCommand.builder()
                    .userId(userId)
                    .newRole(newRole)
                    .assignedByUserId(currentUserSubject)
                    .build();

            KeycloakUserResponse updatedUser = userAdministrationUseCase.assignRole(command);
            KeycloakUserApiResponse response = KeycloakUserApiResponse.fromApplicationResponse(updatedUser);

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Rôle invalide: " + request.getRole());
        }
    }

    @GetMapping("/statistics")
    @Operation(summary = "Statistiques des utilisateurs",
               description = "Récupère les statistiques de répartition des utilisateurs par rôle")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Statistiques"),
        @ApiResponse(responseCode = "403", description = "Accès refusé")
    })
    @PreAuthorize("hasAnyRole('PASTEUR', 'ADMIN')")
    public ResponseEntity<UserStatisticsApiResponse> getUserStatistics() {
        log.info("Fetching user statistics");

        var stats = userAdministrationUseCase.getUserStatistics();
        UserStatisticsApiResponse response = UserStatisticsApiResponse.fromDomain(stats);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/pending")
    @Operation(summary = "Utilisateurs en attente",
               description = "Récupère les nouveaux fidèles qui n'ont pas encore été promus")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Liste des fidèles"),
        @ApiResponse(responseCode = "403", description = "Accès refusé")
    })
    @PreAuthorize("hasAnyRole('LEADER', 'PASTEUR', 'ADMIN')")
    public ResponseEntity<List<KeycloakUserApiResponse>> getPendingUsers() {
        log.info("Fetching pending users (FIDELE role)");

        List<KeycloakUserResponse> users = userAdministrationUseCase.getUsersByRole(Role.FIDELE);
        List<KeycloakUserApiResponse> response = users.stream()
                .map(KeycloakUserApiResponse::fromApplicationResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }
}
