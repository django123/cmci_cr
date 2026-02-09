package com.cmci.cr.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.cmci.cr.domain.repository.CommentaireRepository;
import com.cmci.cr.domain.repository.CompteRenduRepository;
import com.cmci.cr.domain.repository.RegionRepository;
import com.cmci.cr.domain.repository.UtilisateurRepository;
import com.cmci.cr.domain.repository.ZoneRepository;
import com.cmci.cr.infrastructure.cache.CacheableCompteRenduRepositoryDecorator;
import com.cmci.cr.infrastructure.cache.CacheableUtilisateurRepositoryDecorator;
import com.cmci.cr.infrastructure.persistence.adapter.CommentaireRepositoryAdapter;
import com.cmci.cr.infrastructure.persistence.adapter.CompteRenduRepositoryAdapter;
import com.cmci.cr.infrastructure.persistence.adapter.RegionRepositoryAdapter;
import com.cmci.cr.infrastructure.persistence.adapter.UtilisateurRepositoryAdapter;
import com.cmci.cr.infrastructure.persistence.adapter.ZoneRepositoryAdapter;

/**
 * Configuration centrale de l'application
 * Assemble les différents modules et déclare les beans principaux
 */
@Configuration
public class ApplicationConfiguration {

    /**
     * Repository CompteRendu (cache désactivé temporairement)
     */
    @Bean
    @Primary
    public CompteRenduRepository compteRenduRepository(
            CompteRenduRepositoryAdapter adapter) {
        return adapter;
    }

    /**
     * Repository Utilisateur (cache désactivé temporairement)
     */
    @Bean
    @Primary
    public UtilisateurRepository utilisateurRepository(
            UtilisateurRepositoryAdapter adapter) {
        return adapter;
    }

    /**
     * Repository Commentaire (pas de cache pour les commentaires)
     */
    @Bean
    @Primary
    public CommentaireRepository commentaireRepository(
            CommentaireRepositoryAdapter adapter) {
        return adapter;
    }

    /**
     * Repository Region
     */
    @Bean
    @Primary
    public RegionRepository regionRepository(
            RegionRepositoryAdapter adapter) {
        return adapter;
    }

    /**
     * Repository Zone
     */
    @Bean
    @Primary
    public ZoneRepository zoneRepository(
            ZoneRepositoryAdapter adapter) {
        return adapter;
    }
}
