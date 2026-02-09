package com.cmci.cr.infrastructure.persistence.mapper;

import com.cmci.cr.domain.model.Zone;
import com.cmci.cr.infrastructure.persistence.entity.ZoneJpaEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper entre Zone (domain) et ZoneJpaEntity (infrastructure)
 */
@Component
public class ZoneMapper {

    public ZoneJpaEntity toJpaEntity(Zone domain) {
        if (domain == null) {
            return null;
        }

        return ZoneJpaEntity.builder()
                .id(domain.getId())
                .nom(domain.getNom())
                .regionId(domain.getRegionId())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }

    public Zone toDomain(ZoneJpaEntity jpa) {
        if (jpa == null) {
            return null;
        }

        return Zone.builder()
                .id(jpa.getId())
                .nom(jpa.getNom())
                .regionId(jpa.getRegionId())
                .createdAt(jpa.getCreatedAt())
                .updatedAt(jpa.getUpdatedAt())
                .build();
    }
}
