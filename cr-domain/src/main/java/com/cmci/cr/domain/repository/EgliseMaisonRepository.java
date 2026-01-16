package com.cmci.cr.domain.repository;

import com.cmci.cr.domain.model.EgliseMaison;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Port (interface) pour la persistence des Églises de Maison
 * Implémenté dans le module infrastructure
 */
public interface EgliseMaisonRepository {

    /**
     * Sauvegarde une église de maison
     */
    EgliseMaison save(EgliseMaison egliseMaison);

    /**
     * Trouve une église de maison par son ID
     */
    Optional<EgliseMaison> findById(UUID id);

    /**
     * Trouve toutes les églises de maison d'une église locale
     */
    List<EgliseMaison> findByEgliseLocaleId(UUID egliseLocaleId);

    /**
     * Trouve toutes les églises de maison dirigées par un leader
     */
    List<EgliseMaison> findByLeaderId(UUID leaderId);

    /**
     * Trouve toutes les églises de maison
     */
    List<EgliseMaison> findAll();

    /**
     * Supprime une église de maison
     */
    void deleteById(UUID id);

    /**
     * Compte le nombre d'églises de maison dans une église locale
     */
    long countByEgliseLocaleId(UUID egliseLocaleId);

    /**
     * Trouve les églises de maison sans leader
     */
    List<EgliseMaison> findByLeaderIdIsNull();
}
