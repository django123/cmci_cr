package com.cmci.cr.infrastructure.persistence.mapper;

import com.cmci.cr.domain.model.EgliseLocale;
import com.cmci.cr.infrastructure.persistence.entity.EgliseLocaleJpaEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper entre EgliseLocale (domain) et EgliseLocaleJpaEntity (infrastructure)
 */
@Component
public class EgliseLocaleMapper {

    public EgliseLocaleJpaEntity toJpaEntity(EgliseLocale domain) {
        if (domain == null) {
            return null;
        }

        return EgliseLocaleJpaEntity.builder()
                .id(domain.getId())
                .nom(domain.getNom())
                .adresse(domain.getAdresse())
                .zoneId(domain.getZoneId())
                .pasteurId(domain.getPasteurId())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }

    public EgliseLocale toDomain(EgliseLocaleJpaEntity jpa) {
        if (jpa == null) {
            return null;
        }

        return EgliseLocale.builder()
                .id(jpa.getId())
                .nom(jpa.getNom())
                .adresse(jpa.getAdresse())
                .zoneId(jpa.getZoneId())
                .pasteurId(jpa.getPasteurId())
                .createdAt(jpa.getCreatedAt())
                .updatedAt(jpa.getUpdatedAt())
                .build();
    }
}
