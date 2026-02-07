package com.cmci.cr.infrastructure.identity;

import com.cmci.cr.domain.port.IdentityProviderPort;
import com.cmci.cr.domain.valueobject.Role;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RolesResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service d'administration Keycloak
 * Implémente le port IdentityProviderPort pour gérer les utilisateurs et rôles
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class KeycloakAdminService implements IdentityProviderPort {

    private final Keycloak keycloak;

    @Qualifier("keycloakRealm")
    private final String realm;

    private RealmResource getRealmResource() {
        return keycloak.realm(realm);
    }

    private UsersResource getUsersResource() {
        return getRealmResource().users();
    }

    private RolesResource getRolesResource() {
        return getRealmResource().roles();
    }

    @Override
    public List<IdentityUser> getUsersByRole(Role role) {
        try {
            log.debug("Fetching users with role: {}", role.name());

            List<UserRepresentation> users = getRealmResource()
                    .roles()
                    .get(role.name())
                    .getUserMembers();

            return users.stream()
                    .map(this::toIdentityUser)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error fetching users by role {}: {}", role.name(), e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public List<IdentityUser> getAllUsers(int first, int max) {
        try {
            log.debug("Fetching all users (first: {}, max: {})", first, max);

            List<UserRepresentation> users = getUsersResource().list(first, max);

            return users.stream()
                    .map(this::toIdentityUser)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error fetching all users: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public Optional<IdentityUser> getUserById(String userId) {
        try {
            log.debug("Fetching user by ID: {}", userId);

            UserRepresentation user = getUsersResource().get(userId).toRepresentation();
            return Optional.of(toIdentityUser(user));
        } catch (Exception e) {
            log.error("Error fetching user by ID {}: {}", userId, e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public Optional<IdentityUser> getUserByEmail(String email) {
        try {
            log.debug("Fetching user by email: {}", email);

            List<UserRepresentation> users = getUsersResource().searchByEmail(email, true);

            if (users.isEmpty()) {
                return Optional.empty();
            }

            return Optional.of(toIdentityUser(users.get(0)));
        } catch (Exception e) {
            log.error("Error fetching user by email {}: {}", email, e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public void assignRole(String userId, Role role) {
        try {
            log.info("Assigning role {} to user {}", role.name(), userId);

            UserResource userResource = getUsersResource().get(userId);
            RoleRepresentation roleRep = getRolesResource().get(role.name()).toRepresentation();

            userResource.roles().realmLevel().add(Collections.singletonList(roleRep));

            log.info("Successfully assigned role {} to user {}", role.name(), userId);
        } catch (Exception e) {
            log.error("Error assigning role {} to user {}: {}", role.name(), userId, e.getMessage());
            throw new RuntimeException("Failed to assign role: " + e.getMessage(), e);
        }
    }

    @Override
    public void removeRole(String userId, Role role) {
        try {
            log.info("Removing role {} from user {}", role.name(), userId);

            UserResource userResource = getUsersResource().get(userId);
            RoleRepresentation roleRep = getRolesResource().get(role.name()).toRepresentation();

            userResource.roles().realmLevel().remove(Collections.singletonList(roleRep));

            log.info("Successfully removed role {} from user {}", role.name(), userId);
        } catch (Exception e) {
            log.error("Error removing role {} from user {}: {}", role.name(), userId, e.getMessage());
            throw new RuntimeException("Failed to remove role: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Role> getUserRoles(String userId) {
        try {
            log.debug("Fetching roles for user: {}", userId);

            UserResource userResource = getUsersResource().get(userId);
            List<RoleRepresentation> roles = userResource.roles().realmLevel().listEffective();

            return roles.stream()
                    .map(RoleRepresentation::getName)
                    .filter(this::isApplicationRole)
                    .map(Role::valueOf)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error fetching roles for user {}: {}", userId, e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public List<IdentityUser> searchUsers(String search, int first, int max) {
        try {
            log.debug("Searching users with term: {} (first: {}, max: {})", search, first, max);

            List<UserRepresentation> users = getUsersResource().search(search, first, max);

            return users.stream()
                    .map(this::toIdentityUser)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error searching users: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public int countUsers() {
        try {
            return getUsersResource().count();
        } catch (Exception e) {
            log.error("Error counting users: {}", e.getMessage());
            return 0;
        }
    }

    @Override
    public int countUsersByRole(Role role) {
        try {
            return getRealmResource()
                    .roles()
                    .get(role.name())
                    .getUserMembers()
                    .size();
        } catch (Exception e) {
            log.error("Error counting users by role {}: {}", role.name(), e.getMessage());
            return 0;
        }
    }

    private IdentityUser toIdentityUser(UserRepresentation user) {
        List<String> roles = getUserRoles(user.getId()).stream()
                .map(Role::name)
                .collect(Collectors.toList());

        return new IdentityUser(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.isEnabled(),
                user.isEmailVerified(),
                roles,
                user.getCreatedTimestamp()
        );
    }

    private boolean isApplicationRole(String roleName) {
        try {
            Role.valueOf(roleName);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
