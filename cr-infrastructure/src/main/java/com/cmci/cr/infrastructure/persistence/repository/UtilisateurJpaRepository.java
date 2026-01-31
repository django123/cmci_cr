package com.cmci.cr.infrastructure.persistence.repository;

import com.cmci.cr.infrastructure.persistence.entity.UtilisateurJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository Spring Data JPA pour Utilisateur
 */
@Repository
public interface UtilisateurJpaRepository extends JpaRepository<UtilisateurJpaEntity, UUID> {

    /**
     * Trouve un utilisateur par son email
     */
    Optional<UtilisateurJpaEntity> findByEmail(String email);

    /**
     * Trouve tous les utilisateurs d'une église de maison
     */
    List<UtilisateurJpaEntity> findByEgliseMaisonId(UUID egliseMaisonId);

    /**
     * Trouve tous les disciples d'un FD
     */
    List<UtilisateurJpaEntity> findByFdId(UUID fdId);

    /**
     * Trouve tous les utilisateurs avec un rôle spécifique
     */
    List<UtilisateurJpaEntity> findByRole(UtilisateurJpaEntity.RoleEnum role);

    /**
     * Vérifie si un email existe déjà
     */
    boolean existsByEmail(String email);

    /**
     * Compte le nombre de disciples d'un FD
     */
    long countByFdId(UUID fdId);

    /**
     * Trouve tous les utilisateurs appartenant à plusieurs églises de maison
     */
    List<UtilisateurJpaEntity> findByEgliseMaisonIdIn(List<UUID> egliseMaisonIds);
}
