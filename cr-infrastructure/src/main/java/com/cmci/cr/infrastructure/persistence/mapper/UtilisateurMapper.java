package com.cmci.cr.infrastructure.persistence.mapper;

import com.cmci.cr.domain.model.Utilisateur;
import com.cmci.cr.domain.model.Utilisateur.StatutUtilisateur;
import com.cmci.cr.domain.valueobject.Role;
import com.cmci.cr.infrastructure.persistence.entity.UtilisateurJpaEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper entre Utilisateur (domain) et UtilisateurJpaEntity (infrastructure)
 */
@Component
public class UtilisateurMapper {

    public UtilisateurJpaEntity toJpaEntity(Utilisateur domain) {
        if (domain == null) {
            return null;
        }

        return UtilisateurJpaEntity.builder()
                .id(domain.getId())
                .nom(domain.getNom())
                .prenom(domain.getPrenom())
                .email(domain.getEmail())
                .telephone(domain.getTelephone())
                .role(UtilisateurJpaEntity.RoleEnum.valueOf(domain.getRole().name()))
                .statut(UtilisateurJpaEntity.StatutUtilisateurEnum.valueOf(domain.getStatut().name()))
                .fdId(domain.getFdId())
                .egliseMaisonId(domain.getEgliseMaisonId())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }

    public Utilisateur toDomain(UtilisateurJpaEntity jpa) {
        if (jpa == null) {
            return null;
        }

        return Utilisateur.builder()
                .id(jpa.getId())
                .nom(jpa.getNom())
                .prenom(jpa.getPrenom())
                .email(jpa.getEmail())
                .telephone(jpa.getTelephone())
                .role(Role.valueOf(jpa.getRole().name()))
                .statut(StatutUtilisateur.valueOf(jpa.getStatut().name()))
                .fdId(jpa.getFdId())
                .egliseMaisonId(jpa.getEgliseMaisonId())
                .createdAt(jpa.getCreatedAt())
                .updatedAt(jpa.getUpdatedAt())
                .build();
    }
}
