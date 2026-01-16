package com.cmci.cr.domain.repository;

import com.cmci.cr.domain.model.EgliseLocale;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Port (interface) pour la persistence des Églises Locales
 * Implémenté dans le module infrastructure
 */
public interface EgliseLocaleRepository {

    /**
     * Sauvegarde une église locale
     */
    EgliseLocale save(EgliseLocale egliseLocale);

    /**
     * Trouve une église locale par son ID
     */
    Optional<EgliseLocale> findById(UUID id);

    /**
     * Trouve toutes les églises locales d'une zone
     */
    List<EgliseLocale> findByZoneId(UUID zoneId);

    /**
     * Trouve toutes les églises locales dirigées par un pasteur
     */
    List<EgliseLocale> findByPasteurId(UUID pasteurId);

    /**
     * Trouve toutes les églises locales
     */
    List<EgliseLocale> findAll();

    /**
     * Supprime une église locale
     */
    void deleteById(UUID id);

    /**
     * Compte le nombre d'églises locales dans une zone
     */
    long countByZoneId(UUID zoneId);

    /**
     * Trouve les églises locales sans pasteur
     */
    List<EgliseLocale> findByPasteurIdIsNull();
}
