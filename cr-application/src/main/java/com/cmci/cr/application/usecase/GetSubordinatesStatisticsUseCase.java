package com.cmci.cr.application.usecase;

import com.cmci.cr.application.dto.response.SubordinateStatisticsResponse;
import com.cmci.cr.domain.model.EgliseLocale;
import com.cmci.cr.domain.model.EgliseMaison;
import com.cmci.cr.domain.model.Utilisateur;
import com.cmci.cr.domain.repository.CompteRenduRepository;
import com.cmci.cr.domain.repository.EgliseLocaleRepository;
import com.cmci.cr.domain.repository.EgliseMaisonRepository;
import com.cmci.cr.domain.repository.UtilisateurRepository;
import com.cmci.cr.domain.service.StatisticsService;
import com.cmci.cr.domain.valueobject.Role;
import lombok.RequiredArgsConstructor;

import java.time.Duration;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Use Case: Voir les statistiques des subordonnés
 *
 * Permet à un responsable de consulter les statistiques de ses subordonnés selon la hiérarchie:
 * - FD → ses disciples directs (fdId)
 * - Leader → tous les membres de son église de maison
 * - Pasteur → tous les membres de toutes les églises de maison de son église locale
 */
@RequiredArgsConstructor
public class GetSubordinatesStatisticsUseCase {

    private final UtilisateurRepository utilisateurRepository;
    private final CompteRenduRepository compteRenduRepository;
    private final EgliseMaisonRepository egliseMaisonRepository;
    private final EgliseLocaleRepository egliseLocaleRepository;
    private final StatisticsService statisticsService;

    /**
     * Récupère les statistiques de tous les subordonnés sur une période donnée
     *
     * @param responsableId ID du responsable (FD, Leader ou Pasteur)
     * @param startDate Date de début
     * @param endDate Date de fin
     * @return Liste des subordonnés avec leurs statistiques
     */
    public List<SubordinateStatisticsResponse> execute(UUID responsableId, LocalDate startDate, LocalDate endDate) {
        Utilisateur responsable = utilisateurRepository.findById(responsableId)
                .orElseThrow(() -> new NoSuchElementException("Responsable non trouvé: " + responsableId));

        // Récupérer les subordonnés selon le rôle
        List<Utilisateur> subordinates = getSubordinates(responsable);

        if (subordinates.isEmpty()) {
            return List.of();
        }

        // Calculer les statistiques pour chaque subordonné
        return subordinates.stream()
                .map(subordinate -> buildStatisticsResponse(subordinate, startDate, endDate))
                .sorted(Comparator.comparing(SubordinateStatisticsResponse::getNomComplet))
                .collect(Collectors.toList());
    }

    /**
     * Récupère les subordonnés selon le rôle du responsable
     */
    private List<Utilisateur> getSubordinates(Utilisateur responsable) {
        return switch (responsable.getRole()) {
            case FD -> getFDSubordinates(responsable);
            case LEADER -> getLeaderSubordinates(responsable);
            case PASTEUR -> getPasteurSubordinates(responsable);
            case ADMIN -> getAllActiveUsers();
            default -> List.of();
        };
    }

    /**
     * FD: récupère ses disciples directs
     */
    private List<Utilisateur> getFDSubordinates(Utilisateur fd) {
        return utilisateurRepository.findByFdId(fd.getId())
                .stream()
                .filter(u -> u.getStatut() == Utilisateur.StatutUtilisateur.ACTIF)
                .collect(Collectors.toList());
    }

    /**
     * Leader: récupère tous les membres de son église de maison
     */
    private List<Utilisateur> getLeaderSubordinates(Utilisateur leader) {
        List<EgliseMaison> eglises = egliseMaisonRepository.findByLeaderId(leader.getId());

        if (eglises.isEmpty()) {
            if (leader.getEgliseMaisonId() != null) {
                return utilisateurRepository.findByEgliseMaisonId(leader.getEgliseMaisonId())
                        .stream()
                        .filter(u -> u.getStatut() == Utilisateur.StatutUtilisateur.ACTIF)
                        .filter(u -> !u.getId().equals(leader.getId()))
                        .collect(Collectors.toList());
            }
            return List.of();
        }

        List<UUID> egliseIds = eglises.stream()
                .map(EgliseMaison::getId)
                .collect(Collectors.toList());

        return utilisateurRepository.findByEgliseMaisonIdIn(egliseIds)
                .stream()
                .filter(u -> u.getStatut() == Utilisateur.StatutUtilisateur.ACTIF)
                .filter(u -> !u.getId().equals(leader.getId()))
                .collect(Collectors.toList());
    }

    /**
     * Pasteur: récupère tous les membres de son église locale
     */
    private List<Utilisateur> getPasteurSubordinates(Utilisateur pasteur) {
        List<EgliseLocale> eglisesLocales = egliseLocaleRepository.findByPasteurId(pasteur.getId());

        if (eglisesLocales.isEmpty()) {
            return List.of();
        }

        List<UUID> egliseMaisonIds = new ArrayList<>();
        for (EgliseLocale egliseLocale : eglisesLocales) {
            List<EgliseMaison> eglisesMaison = egliseMaisonRepository.findByEgliseLocaleId(egliseLocale.getId());
            eglisesMaison.forEach(em -> egliseMaisonIds.add(em.getId()));
        }

        if (egliseMaisonIds.isEmpty()) {
            return List.of();
        }

        return utilisateurRepository.findByEgliseMaisonIdIn(egliseMaisonIds)
                .stream()
                .filter(u -> u.getStatut() == Utilisateur.StatutUtilisateur.ACTIF)
                .filter(u -> !u.getId().equals(pasteur.getId()))
                .collect(Collectors.toList());
    }

    /**
     * Admin: récupère tous les utilisateurs actifs
     */
    private List<Utilisateur> getAllActiveUsers() {
        List<Utilisateur> allUsers = new ArrayList<>();
        for (Role role : Role.values()) {
            allUsers.addAll(utilisateurRepository.findByRole(role));
        }
        return allUsers.stream()
                .filter(u -> u.getStatut() == Utilisateur.StatutUtilisateur.ACTIF)
                .collect(Collectors.toList());
    }

    /**
     * Construit la réponse de statistiques pour un subordonné
     */
    private SubordinateStatisticsResponse buildStatisticsResponse(
            Utilisateur subordinate,
            LocalDate startDate,
            LocalDate endDate) {

        // Calculer les statistiques via le service
        StatisticsService.PersonalStatistics stats = statisticsService.calculatePersonalStatistics(
                subordinate.getId(), startDate, endDate);

        // Calculer le dernier CR pour l'alerte
        long daysSinceLastCR = calculateDaysSinceLastCR(subordinate.getId());
        String alertLevel = determineAlertLevel(daysSinceLastCR);

        return SubordinateStatisticsResponse.builder()
                .utilisateurId(subordinate.getId())
                .nom(subordinate.getNom())
                .prenom(subordinate.getPrenom())
                .nomComplet(subordinate.getNomComplet())
                .email(subordinate.getEmail())
                .role(subordinate.getRole().name())
                .roleDisplayName(subordinate.getRole().getDisplayName())
                .avatarUrl(subordinate.getAvatarUrl())
                .startDate(startDate)
                .endDate(endDate)
                .nombreTotalCRs(stats.getNombreTotalCRs())
                .tauxRegularite(Math.round(stats.getTauxRegularite() * 100.0) / 100.0)
                .rdqdCompletCount(stats.getRdqdCompletCount())
                .tauxRDQD(Math.round(stats.getTauxRDQD() * 100.0) / 100.0)
                .dureeTotalePriere(formatDuration(stats.getDureeTotalePriere()))
                .dureeMoyennePriere(formatDuration(stats.getDureeMoyennePriere()))
                .totalChapitresLus(stats.getTotalChapitresLus())
                .moyenneChapitresParJour(Math.round(stats.getMoyenneChapitresParJour() * 100.0) / 100.0)
                .totalPersonnesEvangelisees(stats.getTotalPersonnesEvangelisees())
                .nombreConfessions(stats.getNombreConfessions())
                .nombreJeunes(stats.getNombreJeunes())
                .tendancePositive(stats.getTauxRegularite() >= 80.0)
                .alertLevel(alertLevel)
                .hasAlert(!"NONE".equals(alertLevel))
                .build();
    }

    /**
     * Calcule le nombre de jours depuis le dernier CR
     */
    private long calculateDaysSinceLastCR(UUID utilisateurId) {
        LocalDate today = LocalDate.now();
        LocalDate thirtyDaysAgo = today.minusDays(30);

        return compteRenduRepository.findByUtilisateurIdAndDateBetween(utilisateurId, thirtyDaysAgo, today)
                .stream()
                .map(cr -> cr.getDate())
                .max(LocalDate::compareTo)
                .map(lastDate -> ChronoUnit.DAYS.between(lastDate, today))
                .orElse(999L); // Si pas de CR, retourner une grande valeur
    }

    private String determineAlertLevel(long daysSinceLastCR) {
        if (daysSinceLastCR >= 7) {
            return "CRITICAL";
        }
        if (daysSinceLastCR >= 3) {
            return "WARNING";
        }
        return "NONE";
    }

    private String formatDuration(Duration duration) {
        if (duration == null) {
            return "00:00";
        }
        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();
        return String.format("%02d:%02d", hours, minutes);
    }
}
