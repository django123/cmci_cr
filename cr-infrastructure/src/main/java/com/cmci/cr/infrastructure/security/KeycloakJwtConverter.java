package com.cmci.cr.infrastructure.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Convertisseur personnalisé pour extraire les rôles et autorités depuis un JWT Keycloak
 */
@Component
public class KeycloakJwtConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        Collection<GrantedAuthority> authorities = extractAuthorities(jwt);
        String principalName = jwt.getClaimAsString("preferred_username");

        if (principalName == null) {
            principalName = jwt.getSubject();
        }

        return new JwtAuthenticationToken(jwt, authorities, principalName);
    }

    /**
     * Extrait les autorités (rôles) depuis les claims du JWT Keycloak
     */
    private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
        Set<GrantedAuthority> authorities = new HashSet<>();

        // Extraction des rôles realm
        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        if (realmAccess != null && realmAccess.containsKey("roles")) {
            @SuppressWarnings("unchecked")
            List<String> realmRoles = (List<String>) realmAccess.get("roles");
            authorities.addAll(
                    realmRoles.stream()
                            .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                            .collect(Collectors.toSet())
            );
        }

        // Extraction des rôles resource (optionnel)
        Map<String, Object> resourceAccess = jwt.getClaim("resource_access");
        if (resourceAccess != null) {
            resourceAccess.values().forEach(resource -> {
                if (resource instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> resourceMap = (Map<String, Object>) resource;
                    if (resourceMap.containsKey("roles")) {
                        @SuppressWarnings("unchecked")
                        List<String> resourceRoles = (List<String>) resourceMap.get("roles");
                        authorities.addAll(
                                resourceRoles.stream()
                                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                                        .collect(Collectors.toSet())
                        );
                    }
                }
            });
        }

        // Extraction des scopes OAuth2 standards
        String scopeClaim = jwt.getClaimAsString("scope");
        if (scopeClaim != null) {
            authorities.addAll(
                    Arrays.stream(scopeClaim.split(" "))
                            .map(scope -> new SimpleGrantedAuthority("SCOPE_" + scope))
                            .collect(Collectors.toSet())
            );
        }

        return authorities;
    }
}
