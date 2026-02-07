package com.cmci.cr.application.usecase;

import com.cmci.cr.application.dto.command.AssignRoleCommand;
import com.cmci.cr.application.dto.response.KeycloakUserResponse;
import com.cmci.cr.domain.port.IdentityProviderPort;
import com.cmci.cr.domain.port.IdentityProviderPort.IdentityUser;
import com.cmci.cr.domain.valueobject.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Use case pour l'administration des utilisateurs et des rôles
 * Permet aux Pasteurs et Admins de gérer les rôles des utilisateurs
 */
public class UserAdministrationUseCase {

    private static final Logger log = LoggerFactory.getLogger(UserAdministrationUseCase.class);
    private final IdentityProviderPort identityProvider;

    public UserAdministrationUseCase(IdentityProviderPort identityProvider) {
        this.identityProvider = identityProvider;
    }

    /**
     * Récupère tous les utilisateurs avec pagination
     */
    public List<KeycloakUserResponse> getAllUsers(int page, int size) {
        log.debug("Fetching all users - page: {}, size: {}", page, size);

        int first = page * size;
        List<IdentityUser> users = identityProvider.getAllUsers(first, size);

        return users.stream()
                .map(KeycloakUserResponse::fromIdentityUser)
                .collect(Collectors.toList());
    }

    /**
     * Récupère les utilisateurs par rôle
     */
    public List<KeycloakUserResponse> getUsersByRole(Role role) {
        log.debug("Fetching users with role: {}", role);

        List<IdentityUser> users = identityProvider.getUsersByRole(role);

        return users.stream()
                .map(KeycloakUserResponse::fromIdentityUser)
                .collect(Collectors.toList());
    }

    /**
     * Récupère un utilisateur par son ID
     */
    public KeycloakUserResponse getUserById(String userId) {
        log.debug("Fetching user by ID: {}", userId);

        return identityProvider.getUserById(userId)
                .map(KeycloakUserResponse::fromIdentityUser)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé: " + userId));
    }

    /**
     * Recherche des utilisateurs
     */
    public List<KeycloakUserResponse> searchUsers(String search, int page, int size) {
        log.debug("Searching users with term: {} - page: {}, size: {}", search, page, size);

        int first = page * size;
        List<IdentityUser> users = identityProvider.searchUsers(search, first, size);

        return users.stream()
                .map(KeycloakUserResponse::fromIdentityUser)
                .collect(Collectors.toList());
    }

    /**
     * Attribue un rôle à un utilisateur
     * Vérifie que l'assignateur a les droits nécessaires
     */
    public KeycloakUserResponse assignRole(AssignRoleCommand command) {
        log.info("Assigning role {} to user {} by {}",
                command.getNewRole(), command.getUserId(), command.getAssignedByUserId());

        // Vérifier que l'utilisateur cible existe
        IdentityUser targetUser = identityProvider.getUserById(command.getUserId())
                .orElseThrow(() -> new RuntimeException("Utilisateur cible non trouvé: " + command.getUserId()));

        // Vérifier que l'assignateur a les droits
        List<Role> assignerRoles = identityProvider.getUserRoles(command.getAssignedByUserId());
        validateAssignmentPermission(assignerRoles, command.getNewRole());

        // Récupérer les rôles actuels de l'utilisateur cible
        List<Role> currentRoles = identityProvider.getUserRoles(command.getUserId());

        // Retirer l'ancien rôle applicatif principal (si différent du nouveau)
        for (Role currentRole : currentRoles) {
            if (currentRole != command.getNewRole() && isApplicationRole(currentRole)) {
                identityProvider.removeRole(command.getUserId(), currentRole);
            }
        }

        // Assigner le nouveau rôle
        identityProvider.assignRole(command.getUserId(), command.getNewRole());

        // Retourner l'utilisateur mis à jour
        return identityProvider.getUserById(command.getUserId())
                .map(KeycloakUserResponse::fromIdentityUser)
                .orElseThrow(() -> new RuntimeException("Erreur lors de la mise à jour"));
    }

    /**
     * Récupère les statistiques des utilisateurs par rôle
     */
    public UserStatistics getUserStatistics() {
        return new UserStatistics(
                identityProvider.countUsers(),
                identityProvider.countUsersByRole(Role.FIDELE),
                identityProvider.countUsersByRole(Role.FD),
                identityProvider.countUsersByRole(Role.LEADER),
                identityProvider.countUsersByRole(Role.PASTEUR),
                identityProvider.countUsersByRole(Role.ADMIN)
        );
    }

    /**
     * Vérifie que l'assignateur a le droit d'assigner le rôle demandé
     */
    private void validateAssignmentPermission(List<Role> assignerRoles, Role targetRole) {
        // ADMIN peut tout faire
        if (assignerRoles.contains(Role.ADMIN)) {
            return;
        }

        // PASTEUR peut assigner FIDELE, FD, LEADER
        if (assignerRoles.contains(Role.PASTEUR)) {
            if (targetRole == Role.PASTEUR || targetRole == Role.ADMIN) {
                throw new SecurityException("Un Pasteur ne peut pas promouvoir au rang de Pasteur ou Admin");
            }
            return;
        }

        // LEADER peut assigner FIDELE, FD
        if (assignerRoles.contains(Role.LEADER)) {
            if (targetRole == Role.LEADER || targetRole == Role.PASTEUR || targetRole == Role.ADMIN) {
                throw new SecurityException("Un Leader ne peut pas promouvoir au rang de Leader ou supérieur");
            }
            return;
        }

        throw new SecurityException("Vous n'avez pas les droits pour modifier les rôles");
    }

    private boolean isApplicationRole(Role role) {
        return role == Role.FIDELE || role == Role.FD ||
               role == Role.LEADER || role == Role.PASTEUR || role == Role.ADMIN;
    }

    /**
     * Record pour les statistiques utilisateurs
     */
    public record UserStatistics(
            int totalUsers,
            int fideles,
            int fds,
            int leaders,
            int pasteurs,
            int admins
    ) {}
}
