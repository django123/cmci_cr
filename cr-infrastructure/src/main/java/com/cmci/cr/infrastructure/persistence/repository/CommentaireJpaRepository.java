package com.cmci.cr.infrastructure.persistence.repository;

import com.cmci.cr.infrastructure.persistence.entity.CommentaireJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository Spring Data JPA pour Commentaire
 */
@Repository
public interface CommentaireJpaRepository extends JpaRepository<CommentaireJpaEntity, UUID> {

    /**
     * Trouve tous les commentaires d'un compte rendu
     */
    List<CommentaireJpaEntity> findByCompteRenduIdOrderByCreatedAtAsc(UUID compteRenduId);

    /**
     * Trouve tous les commentaires d'un auteur
     */
    List<CommentaireJpaEntity> findByAuteurIdOrderByCreatedAtDesc(UUID auteurId);

    /**
     * Compte le nombre de commentaires d'un compte rendu
     */
    long countByCompteRenduId(UUID compteRenduId);
}
