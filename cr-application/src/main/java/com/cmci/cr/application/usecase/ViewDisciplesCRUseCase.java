package com.cmci.cr.application.usecase;

import com.cmci.cr.application.dto.response.DiscipleWithCRStatusResponse;
import com.cmci.cr.domain.model.Utilisateur;
import com.cmci.cr.domain.repository.CompteRenduRepository;
import com.cmci.cr.domain.repository.UtilisateurRepository;
import com.cmci.cr.domain.service.CRDomainService;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Use Case: Voir les CR de mes disciples (US3.1)
 * Permet à un FD de consulter l'état des CR de ses disciples
 */
@RequiredArgsConstructor
public class ViewDisciplesCRUseCase {

    private final UtilisateurRepository utilisateurRepository;
    private final CompteRenduRepository compteRenduRepository;
    private final CRDomainService crDomainService;

    /**
     * Récupère la liste des disciples avec leur statut de CR
     *
     * @param fdId ID du FD
     * @return Liste des disciples avec leur statut
     */
    public List<DiscipleWithCRStatusResponse> execute(UUID fdId) {
        // Récupérer tous les disciples du FD
        List<Utilisateur> disciples = utilisateurRepository.findByFdId(fdId);

        // Pour chaque disciple, calculer son statut de CR
        return disciples.stream()
                .map(this::buildDiscipleStatus)
                .collect(Collectors.toList());
    }

    /**
     * Construit le statut CR d'un disciple
     */
    private DiscipleWithCRStatusResponse buildDiscipleStatus(Utilisateur disciple) {
        LocalDate today = LocalDate.now();
        LocalDate thirtyDaysAgo = today.minusDays(30);

        // Vérifier si CR soumis aujourd'hui
        boolean crAujourdhui = compteRenduRepository.existsByUtilisateurIdAndDate(
                disciple.getId(),
                today
        );

        // Trouver la date du dernier CR
        List<LocalDate> recentDates = compteRenduRepository
                .findByUtilisateurIdAndDateBetween(disciple.getId(), thirtyDaysAgo, today)
                .stream()
                .map(cr -> cr.getDate())
                .sorted((d1, d2) -> d2.compareTo(d1)) // Tri décroissant
                .collect(Collectors.toList());

        LocalDate dernierCRDate = recentDates.isEmpty() ? null : recentDates.get(0);

        // Calculer jours depuis dernier CR
        Integer joursDepuisDernierCR = dernierCRDate != null
                ? (int) ChronoUnit.DAYS.between(dernierCRDate, today)
                : null;

        // Calculer taux de régularité sur 30 jours
        double tauxRegularite = crDomainService.calculateRegularityRate(
                disciple.getId(),
                thirtyDaysAgo,
                today
        );

        // Déterminer niveau d'alerte
        String niveauAlerte = "NONE";
        boolean alerte = false;

        if (joursDepuisDernierCR != null) {
            if (joursDepuisDernierCR >= 7) {
                niveauAlerte = "CRITICAL";
                alerte = true;
            } else if (joursDepuisDernierCR >= 3) {
                niveauAlerte = "WARNING";
                alerte = true;
            }
        }

        return DiscipleWithCRStatusResponse.builder()
                .discipleId(disciple.getId())
                .nom(disciple.getNom())
                .prenom(disciple.getPrenom())
                .nomComplet(disciple.getNomComplet())
                .email(disciple.getEmail())
                .avatarUrl(disciple.getAvatarUrl())
                .dernierCRDate(dernierCRDate)
                .crAujourdhui(crAujourdhui)
                .joursDepuisDernierCR(joursDepuisDernierCR)
                .tauxRegularite30j(tauxRegularite)
                .alerte(alerte)
                .niveauAlerte(niveauAlerte)
                .build();
    }
}
