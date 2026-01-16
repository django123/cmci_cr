package com.cmci.cr.domain.repository;

import com.cmci.cr.domain.model.Commentaire;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Port (interface) pour la persistence des Commentaires
 * Implémenté dans le module infrastructure
 */
public interface CommentaireRepository {

    /**
     * Sauvegarde un commentaire
     */
    Commentaire save(Commentaire commentaire);

    /**
     * Trouve un commentaire par son ID
     */
    Optional<Commentaire> findById(UUID id);

    /**
     * Trouve tous les commentaires d'un compte rendu
     */
    List<Commentaire> findByCompteRenduId(UUID compteRenduId);

    /**
     * Trouve tous les commentaires d'un auteur
     */
    List<Commentaire> findByAuteurId(UUID auteurId);

    /**
     * Supprime un commentaire
     */
    void deleteById(UUID id);

    /**
     * Compte le nombre de commentaires d'un compte rendu
     */
    long countByCompteRenduId(UUID compteRenduId);
}
