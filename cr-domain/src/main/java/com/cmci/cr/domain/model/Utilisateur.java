package com.cmci.cr.domain.model;

import com.cmci.cr.domain.valueobject.Role;
import lombok.Builder;
import lombok.Value;
import lombok.With;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entité Utilisateur - Aggregate Root
 * Représente un disciple membre de la CMCI avec différents niveaux de responsabilité
 * (fidèle, FD, leader, pasteur, admin)
 *
 * Note: Tous les utilisateurs sont des disciples de Jésus-Christ. Le rôle définit
 * le niveau de responsabilité dans l'accompagnement spirituel d'autres disciples.
 */
@Value
@Builder
@With
public class Utilisateur {
    UUID id;
    String email;
    String nom;
    String prenom;
    Role role;
    UUID egliseMaisonId;
    UUID fdId;
    String avatarUrl;
    String telephone;
    LocalDate dateNaissance;
    LocalDate dateBapteme;
    StatutUtilisateur statut;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;

    public enum StatutUtilisateur {
        ACTIF("Actif"),
        INACTIF("Inactif"),
        SUSPENDU("Suspendu");

        private final String displayName;

        StatutUtilisateur(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * Obtient le nom complet de l'utilisateur
     */
    public String getNomComplet() {
        return prenom + " " + nom;
    }

    /**
     * Vérifie si l'utilisateur est actif
     */
    public boolean isActif() {
        return statut == StatutUtilisateur.ACTIF;
    }

    /**
     * Vérifie si l'utilisateur a un FD assigné
     */
    public boolean hasFD() {
        return fdId != null;
    }

    /**
     * Vérifie si l'utilisateur peut voir les CR d'un autre utilisateur
     */
    public boolean canViewCROf(Utilisateur otherUser) {
        if (this.id.equals(otherUser.id)) {
            return true; // Peut toujours voir ses propres CR
        }

        return switch (this.role) {
            case FIDELE -> false;
            case FD -> otherUser.fdId != null && otherUser.fdId.equals(this.id);
            case LEADER, PASTEUR -> this.role.canViewCROf(otherUser.role) &&
                    isSameEgliseMaison(otherUser);
            case ADMIN -> true;
        };
    }

    /**
     * Vérifie si deux utilisateurs appartiennent à la même église de maison
     */
    private boolean isSameEgliseMaison(Utilisateur otherUser) {
        return this.egliseMaisonId != null &&
                this.egliseMaisonId.equals(otherUser.egliseMaisonId);
    }

    /**
     * Vérifie si l'utilisateur peut commenter les CR d'un autre utilisateur
     */
    public boolean canCommentCROf(Utilisateur otherUser) {
        return switch (this.role) {
            case FIDELE -> false;
            case FD -> otherUser.fdId != null && otherUser.fdId.equals(this.id);
            case LEADER, PASTEUR, ADMIN -> canViewCROf(otherUser);
        };
    }

    /**
     * Validation métier
     */
    public void validate() {
        if (email == null || !email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new IllegalStateException("Email invalide");
        }
        if (nom == null || nom.trim().isEmpty()) {
            throw new IllegalStateException("Le nom est obligatoire");
        }
        if (prenom == null || prenom.trim().isEmpty()) {
            throw new IllegalStateException("Le prénom est obligatoire");
        }
        if (role == null) {
            throw new IllegalStateException("Le rôle est obligatoire");
        }
    }
}
