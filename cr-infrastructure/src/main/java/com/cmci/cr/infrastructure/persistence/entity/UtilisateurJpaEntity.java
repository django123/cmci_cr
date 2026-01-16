package com.cmci.cr.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entité JPA pour la table utilisateur
 */
@Entity
@Table(name = "utilisateur")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UtilisateurJpaEntity {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "nom", nullable = false, length = 100)
    private String nom;

    @Column(name = "prenom", nullable = false, length = 100)
    private String prenom;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private RoleEnum role;

    @Column(name = "eglise_maison_id")
    private UUID egliseMaisonId;

    @Column(name = "fd_id")
    private UUID fdId;

    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    @Column(name = "telephone", length = 20)
    private String telephone;

    @Column(name = "date_naissance")
    private LocalDate dateNaissance;

    @Column(name = "date_bapteme")
    private LocalDate dateBapteme;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false, length = 20)
    private StatutUtilisateurEnum statut;

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

    public enum RoleEnum {
        FIDELE,
        FD,
        LEADER,
        PASTEUR,
        ADMIN
    }

    public enum StatutUtilisateurEnum {
        ACTIF,
        INACTIF,
        SUSPENDU
    }
}
