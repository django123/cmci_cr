package com.cmci.cr.infrastructure.cache;

/**
 * Constantes pour les noms de cache Redis
 */
public final class CacheNames {

    private CacheNames() {
        // Classe utilitaire, constructeur privé
    }

    /**
     * Cache pour les utilisateurs
     * TTL : 2 heures
     */
    public static final String UTILISATEURS = "utilisateurs";

    /**
     * Cache pour les comptes rendus
     * TTL : 30 minutes
     */
    public static final String COMPTES_RENDUS = "comptes-rendus";

    /**
     * Cache pour les commentaires
     * TTL : 15 minutes
     */
    public static final String COMMENTAIRES = "commentaires";

    /**
     * Cache pour les statistiques
     * TTL : 5 minutes
     */
    public static final String STATISTIQUES = "statistiques";

    /**
     * Cache pour les référentiels (régions, zones, églises)
     * TTL : 24 heures
     */
    public static final String REFERENTIELS = "referentiels";
}
