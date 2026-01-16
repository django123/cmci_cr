package com.cmci.cr.application.usecase;

import com.cmci.cr.application.dto.response.GroupStatisticsResponse;
import com.cmci.cr.domain.model.Utilisateur;
import com.cmci.cr.domain.repository.CompteRenduRepository;
import com.cmci.cr.domain.repository.UtilisateurRepository;
import com.cmci.cr.domain.service.StatisticsService;
import lombok.RequiredArgsConstructor;

import java.time.Duration;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Use Case: Obtenir les statistiques de groupe (US4.2 - FD/Leader)
 */
@RequiredArgsConstructor
public class GetGroupStatisticsUseCase {

    private final UtilisateurRepository utilisateurRepository;
    private final CompteRenduRepository compteRenduRepository;
    private final StatisticsService statisticsService;

    /**
     * Exécute le use case de récupération des statistiques de groupe
     *
     * @param fdId ID du FD/Leader
     * @param startDate Date de début
     * @param endDate Date de fin
     * @return Statistiques de groupe
     */
    public GroupStatisticsResponse execute(UUID fdId, LocalDate startDate, LocalDate endDate) {
        // Récupérer tous les disciples du FD
        List<Utilisateur> disciples = utilisateurRepository.findByFdId(fdId);

        List<UUID> disciplesIds = disciples.stream()
                .map(Utilisateur::getId)
                .collect(Collectors.toList());

        // Calculer les statistiques via le domain service
        StatisticsService.GroupStatistics stats = statisticsService.calculateGroupStatistics(
                disciplesIds,
                startDate,
                endDate
        );

        // Calculer les membres en difficulté
        long membresAvecAlerte = disciples.stream()
                .filter(d -> {
                    LocalDate dernierCR = compteRenduRepository
                            .findByUtilisateurIdAndDateBetween(d.getId(), startDate, LocalDate.now())
                            .stream()
                            .map(cr -> cr.getDate())
                            .max(LocalDate::compareTo)
                            .orElse(null);

                    if (dernierCR == null) return true;

                    long joursSansCR = ChronoUnit.DAYS.between(dernierCR, LocalDate.now());
                    return joursSansCR >= 3;
                })
                .count();

        long membresInactifs = disciples.stream()
                .filter(d -> {
                    LocalDate dernierCR = compteRenduRepository
                            .findByUtilisateurIdAndDateBetween(d.getId(), startDate, LocalDate.now())
                            .stream()
                            .map(cr -> cr.getDate())
                            .max(LocalDate::compareTo)
                            .orElse(null);

                    if (dernierCR == null) return true;

                    long joursSansCR = ChronoUnit.DAYS.between(dernierCR, LocalDate.now());
                    return joursSansCR >= 7;
                })
                .count();

        // Mapper vers le DTO de réponse
        return GroupStatisticsResponse.builder()
                .startDate(startDate)
                .endDate(endDate)
                .nombreMembres(stats.getNombreMembres())
                .nombreCRsAujourdhui(stats.getNombreCRsAujourdhui())
                .tauxSoumissionJour(stats.getTauxSoumissionJour())
                .totalCRsPeriode(stats.getTotalCRsPeriode())
                .tauxRegulariteGroupe(stats.getTauxRegulariteGroupe())
                .dureeTotalePriere(formatDuration(stats.getDureeTotalePriere()))
                .moyennePriereParMembre(formatDuration(stats.getMoyennePriereParMembre()))
                .membresAvecAlerte(membresAvecAlerte)
                .membresInactifs(membresInactifs)
                .meilleurDisciple(null) // TODO: Implémenter calcul du meilleur disciple
                .meilleurTaux(null)
                .build();
    }

    /**
     * Formate une durée au format "HH:mm"
     */
    private String formatDuration(Duration duration) {
        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();
        return String.format("%02d:%02d", hours, minutes);
    }
}
