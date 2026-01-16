package com.cmci.cr.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entité JPA pour la table commentaire_cr
 */
@Entity
@Table(name = "commentaire_cr")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentaireJpaEntity {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "compte_rendu_id", nullable = false)
    private UUID compteRenduId;

    @Column(name = "auteur_id", nullable = false)
    private UUID auteurId;

    @Column(name = "contenu", nullable = false, columnDefinition = "TEXT")
    private String contenu;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
