package com.cmci.cr.domain.repository;

import com.cmci.cr.domain.model.Utilisateur;
import com.cmci.cr.domain.valueobject.Role;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Port (interface) pour la persistence des Utilisateurs
 * Implémenté dans le module infrastructure
 */
public interface UtilisateurRepository {

    /**
     * Sauvegarde un utilisateur
     */
    Utilisateur save(Utilisateur utilisateur);

    /**
     * Trouve un utilisateur par son ID
     */
    Optional<Utilisateur> findById(UUID id);

    /**
     * Trouve un utilisateur par son email
     */
    Optional<Utilisateur> findByEmail(String email);

    /**
     * Trouve tous les utilisateurs d'une église de maison
     */
    List<Utilisateur> findByEgliseMaisonId(UUID egliseMaisonId);

    /**
     * Trouve tous les disciples d'un FD
     */
    List<Utilisateur> findByFdId(UUID fdId);

    /**
     * Trouve tous les utilisateurs avec un rôle spécifique
     */
    List<Utilisateur> findByRole(Role role);

    /**
     * Vérifie si un email existe déjà
     */
    boolean existsByEmail(String email);

    /**
     * Supprime un utilisateur
     */
    void deleteById(UUID id);

    /**
     * Compte le nombre de disciples d'un FD
     */
    long countByFdId(UUID fdId);

    /**
     * Trouve tous les utilisateurs appartenant à plusieurs églises de maison
     */
    List<Utilisateur> findByEgliseMaisonIdIn(List<UUID> egliseMaisonIds);
}
