package com.cmci.cr.domain.repository;

import com.cmci.cr.domain.model.CompteRendu;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Port (interface) pour la persistence des Comptes Rendus
 * Implémenté dans le module infrastructure
 */
public interface CompteRenduRepository {

    /**
     * Sauvegarde un Compte Rendu
     */
    CompteRendu save(CompteRendu compteRendu);

    /**
     * Trouve un CR par son ID
     */
    Optional<CompteRendu> findById(UUID id);

    /**
     * Trouve un CR par utilisateur et date
     */
    Optional<CompteRendu> findByUtilisateurIdAndDate(UUID utilisateurId, LocalDate date);

    /**
     * Trouve tous les CR d'un utilisateur
     */
    List<CompteRendu> findByUtilisateurId(UUID utilisateurId);

    /**
     * Trouve les CR d'un utilisateur entre deux dates
     */
    List<CompteRendu> findByUtilisateurIdAndDateBetween(
            UUID utilisateurId,
            LocalDate startDate,
            LocalDate endDate
    );

    /**
     * Trouve les CR non vus d'un utilisateur
     */
    List<CompteRendu> findByUtilisateurIdAndVuParFdFalse(UUID utilisateurId);

    /**
     * Supprime un CR (soft delete)
     */
    void deleteById(UUID id);

    /**
     * Vérifie si un CR existe pour une date donnée
     */
    boolean existsByUtilisateurIdAndDate(UUID utilisateurId, LocalDate date);

    /**
     * Compte le nombre de CR d'un utilisateur sur une période
     */
    long countByUtilisateurIdAndDateBetween(
            UUID utilisateurId,
            LocalDate startDate,
            LocalDate endDate
    );
}
