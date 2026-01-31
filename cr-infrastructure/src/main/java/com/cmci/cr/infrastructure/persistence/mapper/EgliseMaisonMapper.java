package com.cmci.cr.infrastructure.persistence.mapper;

import com.cmci.cr.domain.model.EgliseMaison;
import com.cmci.cr.infrastructure.persistence.entity.EgliseMaisonJpaEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper entre EgliseMaison (domain) et EgliseMaisonJpaEntity (infrastructure)
 */
@Component
public class EgliseMaisonMapper {

    public EgliseMaisonJpaEntity toJpaEntity(EgliseMaison domain) {
        if (domain == null) {
            return null;
        }

        return EgliseMaisonJpaEntity.builder()
                .id(domain.getId())
                .nom(domain.getNom())
                .adresse(domain.getAdresse())
                .egliseLocaleId(domain.getEgliseLocaleId())
                .leaderId(domain.getLeaderId())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }

    public EgliseMaison toDomain(EgliseMaisonJpaEntity jpa) {
        if (jpa == null) {
            return null;
        }

        return EgliseMaison.builder()
                .id(jpa.getId())
                .nom(jpa.getNom())
                .adresse(jpa.getAdresse())
                .egliseLocaleId(jpa.getEgliseLocaleId())
                .leaderId(jpa.getLeaderId())
                .createdAt(jpa.getCreatedAt())
                .updatedAt(jpa.getUpdatedAt())
                .build();
    }
}
