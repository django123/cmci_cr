package com.cmci.cr.domain.service;

import com.cmci.cr.domain.model.CompteRendu;
import com.cmci.cr.domain.repository.CompteRenduRepository;
import com.cmci.cr.domain.valueobject.StatutCR;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Service métier pour les statistiques des Comptes Rendus
 */
@RequiredArgsConstructor
public class StatisticsService {

    private final CompteRenduRepository compteRenduRepository;

    /**
     * Calcule les statistiques personnelles d'un utilisateur sur une période
     */
    public PersonalStatistics calculatePersonalStatistics(UUID utilisateurId, LocalDate startDate, LocalDate endDate) {
        List<CompteRendu> crs = compteRenduRepository.findByUtilisateurIdAndDateBetween(
                utilisateurId, startDate, endDate
        );

        long totalCRs = crs.size();
        long rdqdComplete = crs.stream()
                .filter(cr -> cr.getRdqd().isComplete())
                .count();

        Duration totalPriere = crs.stream()
                .map(CompteRendu::getPriereSeule)
                .reduce(Duration.ZERO, Duration::plus);

        int totalChapitres = crs.stream()
                .mapToInt(CompteRendu::getLectureBiblique)
                .sum();

        int totalEvangelisation = crs.stream()
                .mapToInt(cr -> cr.getEvangelisation() != null ? cr.getEvangelisation() : 0)
                .sum();

        long totalConfessions = crs.stream()
                .filter(cr -> Boolean.TRUE.equals(cr.getConfession()))
                .count();

        long totalJeunes = crs.stream()
                .filter(cr -> Boolean.TRUE.equals(cr.getJeune()))
                .count();

        long totalDays = startDate.until(endDate).getDays() + 1;
        double tauxRegularite = totalDays > 0 ? (double) totalCRs / totalDays * 100.0 : 0.0;

        return PersonalStatistics.builder()
                .nombreTotalCRs(totalCRs)
                .rdqdCompletCount(rdqdComplete)
                .tauxRDQD(totalCRs > 0 ? (double) rdqdComplete / totalCRs * 100.0 : 0.0)
                .dureeTotalePriere(totalPriere)
                .dureeMoyennePriere(totalCRs > 0 ? totalPriere.dividedBy(totalCRs) : Duration.ZERO)
                .totalChapitresLus(totalChapitres)
                .moyenneChapitresParJour(totalCRs > 0 ? (double) totalChapitres / totalCRs : 0.0)
                .totalPersonnesEvangelisees(totalEvangelisation)
                .nombreConfessions(totalConfessions)
                .nombreJeunes(totalJeunes)
                .tauxRegularite(tauxRegularite)
                .build();
    }

    /**
     * Calcule les statistiques d'un groupe (pour FD/Leader)
     */
    public GroupStatistics calculateGroupStatistics(List<UUID> membresIds, LocalDate startDate, LocalDate endDate) {
        long totalMembers = membresIds.size();
        long membersWithCRToday = 0;
        Duration totalPriere = Duration.ZERO;
        int totalCRs = 0;

        for (UUID membreId : membresIds) {
            List<CompteRendu> crs = compteRenduRepository.findByUtilisateurIdAndDateBetween(
                    membreId, startDate, endDate
            );

            totalCRs += crs.size();

            // Vérifier si a soumis aujourd'hui
            if (compteRenduRepository.existsByUtilisateurIdAndDate(membreId, LocalDate.now())) {
                membersWithCRToday++;
            }

            // Additionner la durée de prière
            totalPriere = totalPriere.plus(
                    crs.stream()
                            .map(CompteRendu::getPriereSeule)
                            .reduce(Duration.ZERO, Duration::plus)
            );
        }

        double tauxSoumissionJour = totalMembers > 0 ? (double) membersWithCRToday / totalMembers * 100.0 : 0.0;

        long totalDays = startDate.until(endDate).getDays() + 1;
        double crParMembreParJour = (totalMembers > 0 && totalDays > 0)
                ? (double) totalCRs / (totalMembers * totalDays)
                : 0.0;

        return GroupStatistics.builder()
                .nombreMembres(totalMembers)
                .nombreCRsAujourdhui(membersWithCRToday)
                .tauxSoumissionJour(tauxSoumissionJour)
                .totalCRsPeriode(totalCRs)
                .dureeTotalePriere(totalPriere)
                .moyennePriereParMembre(totalMembers > 0 ? totalPriere.dividedBy((int) totalMembers) : Duration.ZERO)
                .tauxRegulariteGroupe(crParMembreParJour * 100.0)
                .build();
    }

    /**
     * DTO pour les statistiques personnelles
     */
    @Value
    @Builder
    public static class PersonalStatistics {
        long nombreTotalCRs;
        long rdqdCompletCount;
        double tauxRDQD;
        Duration dureeTotalePriere;
        Duration dureeMoyennePriere;
        int totalChapitresLus;
        double moyenneChapitresParJour;
        int totalPersonnesEvangelisees;
        long nombreConfessions;
        long nombreJeunes;
        double tauxRegularite;
    }

    /**
     * DTO pour les statistiques de groupe
     */
    @Value
    @Builder
    public static class GroupStatistics {
        long nombreMembres;
        long nombreCRsAujourdhui;
        double tauxSoumissionJour;
        long totalCRsPeriode;
        Duration dureeTotalePriere;
        Duration moyennePriereParMembre;
        double tauxRegulariteGroupe;
    }
}
