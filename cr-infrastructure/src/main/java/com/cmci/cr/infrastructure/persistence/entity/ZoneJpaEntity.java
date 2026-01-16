package com.cmci.cr.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entité JPA pour la table zone
 */
@Entity
@Table(name = "zone")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ZoneJpaEntity {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "nom", nullable = false, length = 100)
    private String nom;

    @Column(name = "code", nullable = false, unique = true, length = 10)
    private String code;

    @Column(name = "region_id", nullable = false)
    private UUID regionId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
