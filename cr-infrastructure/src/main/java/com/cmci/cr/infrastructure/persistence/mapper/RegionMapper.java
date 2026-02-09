package com.cmci.cr.infrastructure.persistence.mapper;

import com.cmci.cr.domain.model.Region;
import com.cmci.cr.infrastructure.persistence.entity.RegionJpaEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper entre Region (domain) et RegionJpaEntity (infrastructure)
 */
@Component
public class RegionMapper {

    public RegionJpaEntity toJpaEntity(Region domain) {
        if (domain == null) {
            return null;
        }

        return RegionJpaEntity.builder()
                .id(domain.getId())
                .nom(domain.getNom())
                .code(domain.getCode())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }

    public Region toDomain(RegionJpaEntity jpa) {
        if (jpa == null) {
            return null;
        }

        return Region.builder()
                .id(jpa.getId())
                .nom(jpa.getNom())
                .code(jpa.getCode())
                .createdAt(jpa.getCreatedAt())
                .updatedAt(jpa.getUpdatedAt())
                .build();
    }
}
