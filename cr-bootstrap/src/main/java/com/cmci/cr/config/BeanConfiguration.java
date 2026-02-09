package com.cmci.cr.config;

import com.cmci.cr.application.usecase.*;
import com.cmci.cr.domain.event.DomainEventPublisher;
import com.cmci.cr.domain.port.IdentityProviderPort;
import com.cmci.cr.domain.repository.CommentaireRepository;
import com.cmci.cr.domain.repository.CompteRenduRepository;
import com.cmci.cr.domain.repository.EgliseLocaleRepository;
import com.cmci.cr.domain.repository.EgliseMaisonRepository;
import com.cmci.cr.domain.repository.RegionRepository;
import com.cmci.cr.domain.repository.UtilisateurRepository;
import com.cmci.cr.domain.repository.ZoneRepository;
import com.cmci.cr.domain.service.CRDomainService;
import com.cmci.cr.domain.service.StatisticsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration des beans Use Cases
 * Wire les dépendances entre les couches
 */
@Configuration
public class BeanConfiguration {

    // ===== Domain Services =====

    @Bean
    public CRDomainService crDomainService(
            CompteRenduRepository compteRenduRepository) {
        return new CRDomainService(compteRenduRepository);
    }

    @Bean
    public StatisticsService statisticsService(
            CompteRenduRepository compteRenduRepository) {
        return new StatisticsService(compteRenduRepository);
    }

    // ===== Use Cases =====

    @Bean
    public CreateCRUseCase createCRUseCase(
            CompteRenduRepository compteRenduRepository) {
        return new CreateCRUseCase(compteRenduRepository);
    }

    @Bean
    public UpdateCRUseCase updateCRUseCase(
            CompteRenduRepository compteRenduRepository,
            CRDomainService crDomainService) {
        return new UpdateCRUseCase(compteRenduRepository, crDomainService);
    }

    @Bean
    public GetCRUseCase getCRUseCase(CompteRenduRepository compteRenduRepository) {
        return new GetCRUseCase(compteRenduRepository);
    }

    @Bean
    public DeleteCRUseCase deleteCRUseCase(
            CompteRenduRepository compteRenduRepository,
            CRDomainService crDomainService) {
        return new DeleteCRUseCase(compteRenduRepository, crDomainService);
    }

    @Bean
    public ValidateCRUseCase validateCRUseCase(
            CompteRenduRepository compteRenduRepository) {
        return new ValidateCRUseCase(compteRenduRepository);
    }

    @Bean
    public MarkCRAsViewedUseCase markCRAsViewedUseCase(
            CompteRenduRepository compteRenduRepository) {
        return new MarkCRAsViewedUseCase(compteRenduRepository);
    }

    @Bean
    public AddCommentaireUseCase addCommentaireUseCase(
            CommentaireRepository commentaireRepository,
            CompteRenduRepository compteRenduRepository) {
        return new AddCommentaireUseCase(commentaireRepository, compteRenduRepository);
    }

    @Bean
    public GetCommentairesUseCase getCommentairesUseCase(
            CommentaireRepository commentaireRepository) {
        return new GetCommentairesUseCase(commentaireRepository);
    }

    @Bean
    public ViewDisciplesCRUseCase viewDisciplesCRUseCase(
            UtilisateurRepository utilisateurRepository,
            CompteRenduRepository compteRenduRepository,
            CRDomainService crDomainService) {
        return new ViewDisciplesCRUseCase(utilisateurRepository, compteRenduRepository, crDomainService);
    }

    @Bean
    public GetSubordinatesCRUseCase getSubordinatesCRUseCase(
            UtilisateurRepository utilisateurRepository,
            CompteRenduRepository compteRenduRepository,
            EgliseMaisonRepository egliseMaisonRepository,
            EgliseLocaleRepository egliseLocaleRepository) {
        return new GetSubordinatesCRUseCase(
                utilisateurRepository, compteRenduRepository, egliseMaisonRepository, egliseLocaleRepository);
    }

    @Bean
    public GetSubordinatesStatisticsUseCase getSubordinatesStatisticsUseCase(
            UtilisateurRepository utilisateurRepository,
            CompteRenduRepository compteRenduRepository,
            EgliseMaisonRepository egliseMaisonRepository,
            EgliseLocaleRepository egliseLocaleRepository,
            StatisticsService statisticsService) {
        return new GetSubordinatesStatisticsUseCase(
                utilisateurRepository, compteRenduRepository, egliseMaisonRepository,
                egliseLocaleRepository, statisticsService);
    }

    @Bean
    public GetPersonalStatisticsUseCase getPersonalStatisticsUseCase(
            StatisticsService statisticsService) {
        return new GetPersonalStatisticsUseCase(statisticsService);
    }

    @Bean
    public GetGroupStatisticsUseCase getGroupStatisticsUseCase(
            UtilisateurRepository utilisateurRepository,
            CompteRenduRepository compteRenduRepository,
            StatisticsService statisticsService) {
        return new GetGroupStatisticsUseCase(
                utilisateurRepository, compteRenduRepository, statisticsService);
    }

    // ===== Utilisateur Use Cases =====

    @Bean
    public CreateUtilisateurUseCase createUtilisateurUseCase(
            UtilisateurRepository utilisateurRepository) {
        return new CreateUtilisateurUseCase(utilisateurRepository);
    }

    @Bean
    public GetUtilisateurUseCase getUtilisateurUseCase(
            UtilisateurRepository utilisateurRepository) {
        return new GetUtilisateurUseCase(utilisateurRepository);
    }

    @Bean
    public UpdateUtilisateurUseCase updateUtilisateurUseCase(
            UtilisateurRepository utilisateurRepository) {
        return new UpdateUtilisateurUseCase(utilisateurRepository);
    }

    @Bean
    public AssignFDUseCase assignFDUseCase(
            UtilisateurRepository utilisateurRepository) {
        return new AssignFDUseCase(utilisateurRepository);
    }

    @Bean
    public ChangeRoleUseCase changeRoleUseCase(
            UtilisateurRepository utilisateurRepository) {
        return new ChangeRoleUseCase(utilisateurRepository);
    }

    // ===== User Administration Use Case (Keycloak) =====

    @Bean
    public UserAdministrationUseCase userAdministrationUseCase(
            IdentityProviderPort identityProviderPort) {
        return new UserAdministrationUseCase(identityProviderPort);
    }

    // ===== Region Use Cases =====

    @Bean
    public CreateRegionUseCase createRegionUseCase(
            RegionRepository regionRepository,
            ZoneRepository zoneRepository) {
        return new CreateRegionUseCase(regionRepository, zoneRepository);
    }

    @Bean
    public GetRegionUseCase getRegionUseCase(
            RegionRepository regionRepository,
            ZoneRepository zoneRepository) {
        return new GetRegionUseCase(regionRepository, zoneRepository);
    }

    @Bean
    public UpdateRegionUseCase updateRegionUseCase(
            RegionRepository regionRepository,
            ZoneRepository zoneRepository) {
        return new UpdateRegionUseCase(regionRepository, zoneRepository);
    }

    @Bean
    public DeleteRegionUseCase deleteRegionUseCase(
            RegionRepository regionRepository,
            ZoneRepository zoneRepository) {
        return new DeleteRegionUseCase(regionRepository, zoneRepository);
    }

    // ===== Zone Use Cases =====

    @Bean
    public CreateZoneUseCase createZoneUseCase(
            ZoneRepository zoneRepository,
            RegionRepository regionRepository,
            EgliseLocaleRepository egliseLocaleRepository) {
        return new CreateZoneUseCase(zoneRepository, regionRepository, egliseLocaleRepository);
    }

    @Bean
    public GetZoneUseCase getZoneUseCase(
            ZoneRepository zoneRepository,
            RegionRepository regionRepository,
            EgliseLocaleRepository egliseLocaleRepository) {
        return new GetZoneUseCase(zoneRepository, regionRepository, egliseLocaleRepository);
    }

    @Bean
    public UpdateZoneUseCase updateZoneUseCase(
            ZoneRepository zoneRepository,
            RegionRepository regionRepository,
            EgliseLocaleRepository egliseLocaleRepository) {
        return new UpdateZoneUseCase(zoneRepository, regionRepository, egliseLocaleRepository);
    }

    @Bean
    public DeleteZoneUseCase deleteZoneUseCase(
            ZoneRepository zoneRepository,
            EgliseLocaleRepository egliseLocaleRepository) {
        return new DeleteZoneUseCase(zoneRepository, egliseLocaleRepository);
    }

    // ===== EgliseLocale Use Cases =====

    @Bean
    public CreateEgliseLocaleUseCase createEgliseLocaleUseCase(
            EgliseLocaleRepository egliseLocaleRepository,
            ZoneRepository zoneRepository,
            EgliseMaisonRepository egliseMaisonRepository,
            UtilisateurRepository utilisateurRepository) {
        return new CreateEgliseLocaleUseCase(
                egliseLocaleRepository, zoneRepository, egliseMaisonRepository, utilisateurRepository);
    }

    @Bean
    public GetEgliseLocaleUseCase getEgliseLocaleUseCase(
            EgliseLocaleRepository egliseLocaleRepository,
            ZoneRepository zoneRepository,
            EgliseMaisonRepository egliseMaisonRepository,
            UtilisateurRepository utilisateurRepository) {
        return new GetEgliseLocaleUseCase(
                egliseLocaleRepository, zoneRepository, egliseMaisonRepository, utilisateurRepository);
    }

    @Bean
    public UpdateEgliseLocaleUseCase updateEgliseLocaleUseCase(
            EgliseLocaleRepository egliseLocaleRepository,
            ZoneRepository zoneRepository,
            EgliseMaisonRepository egliseMaisonRepository,
            UtilisateurRepository utilisateurRepository) {
        return new UpdateEgliseLocaleUseCase(
                egliseLocaleRepository, zoneRepository, egliseMaisonRepository, utilisateurRepository);
    }

    @Bean
    public DeleteEgliseLocaleUseCase deleteEgliseLocaleUseCase(
            EgliseLocaleRepository egliseLocaleRepository,
            EgliseMaisonRepository egliseMaisonRepository) {
        return new DeleteEgliseLocaleUseCase(egliseLocaleRepository, egliseMaisonRepository);
    }

    // ===== EgliseMaison Use Cases =====

    @Bean
    public CreateEgliseMaisonUseCase createEgliseMaisonUseCase(
            EgliseMaisonRepository egliseMaisonRepository,
            EgliseLocaleRepository egliseLocaleRepository,
            UtilisateurRepository utilisateurRepository) {
        return new CreateEgliseMaisonUseCase(
                egliseMaisonRepository, egliseLocaleRepository, utilisateurRepository);
    }

    @Bean
    public GetEgliseMaisonUseCase getEgliseMaisonUseCase(
            EgliseMaisonRepository egliseMaisonRepository,
            EgliseLocaleRepository egliseLocaleRepository,
            UtilisateurRepository utilisateurRepository) {
        return new GetEgliseMaisonUseCase(
                egliseMaisonRepository, egliseLocaleRepository, utilisateurRepository);
    }

    @Bean
    public UpdateEgliseMaisonUseCase updateEgliseMaisonUseCase(
            EgliseMaisonRepository egliseMaisonRepository,
            EgliseLocaleRepository egliseLocaleRepository,
            UtilisateurRepository utilisateurRepository) {
        return new UpdateEgliseMaisonUseCase(
                egliseMaisonRepository, egliseLocaleRepository, utilisateurRepository);
    }

    @Bean
    public DeleteEgliseMaisonUseCase deleteEgliseMaisonUseCase(
            EgliseMaisonRepository egliseMaisonRepository,
            UtilisateurRepository utilisateurRepository) {
        return new DeleteEgliseMaisonUseCase(egliseMaisonRepository, utilisateurRepository);
    }
}
