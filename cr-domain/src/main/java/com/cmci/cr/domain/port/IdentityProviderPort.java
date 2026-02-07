package com.cmci.cr.domain.port;

import com.cmci.cr.domain.valueobject.Role;

import java.util.List;
import java.util.Optional;

/**
 * Port pour l'interaction avec le fournisseur d'identité (Keycloak)
 * Permet de gérer les utilisateurs et leurs rôles
 */
public interface IdentityProviderPort {

    /**
     * Représentation d'un utilisateur dans le système d'identité
     */
    record IdentityUser(
            String id,
            String username,
            String email,
            String firstName,
            String lastName,
            boolean enabled,
            boolean emailVerified,
            List<String> roles,
            Long createdTimestamp
    ) {}

    /**
     * Récupère tous les utilisateurs avec un rôle spécifique
     */
    List<IdentityUser> getUsersByRole(Role role);

    /**
     * Récupère tous les utilisateurs (avec pagination)
     */
    List<IdentityUser> getAllUsers(int first, int max);

    /**
     * Récupère un utilisateur par son ID
     */
    Optional<IdentityUser> getUserById(String userId);

    /**
     * Récupère un utilisateur par son email
     */
    Optional<IdentityUser> getUserByEmail(String email);

    /**
     * Attribue un rôle à un utilisateur
     */
    void assignRole(String userId, Role role);

    /**
     * Retire un rôle d'un utilisateur
     */
    void removeRole(String userId, Role role);

    /**
     * Récupère les rôles d'un utilisateur
     */
    List<Role> getUserRoles(String userId);

    /**
     * Recherche des utilisateurs par nom ou email
     */
    List<IdentityUser> searchUsers(String search, int first, int max);

    /**
     * Compte le nombre total d'utilisateurs
     */
    int countUsers();

    /**
     * Compte le nombre d'utilisateurs avec un rôle spécifique
     */
    int countUsersByRole(Role role);
}
