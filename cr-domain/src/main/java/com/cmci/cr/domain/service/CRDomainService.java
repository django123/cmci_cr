package com.cmci.cr.domain.service;

import com.cmci.cr.domain.model.CompteRendu;
import com.cmci.cr.domain.model.Utilisateur;
import com.cmci.cr.domain.repository.CompteRenduRepository;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

/**
 * Service métier pour la gestion des Comptes Rendus
 * Contient la logique métier complexe liée aux CR
 */
@RequiredArgsConstructor
public class CRDomainService {

    private final CompteRenduRepository compteRenduRepository;

    /**
     * Vérifie si un utilisateur peut créer un CR pour une date donnée
     * Règle: Un seul CR par jour par utilisateur
     */
    public boolean canCreateCR(UUID utilisateurId, LocalDate date) {
        if (date.isAfter(LocalDate.now())) {
            return false; // Pas de CR dans le futur
        }
        return !compteRenduRepository.existsByUtilisateurIdAndDate(utilisateurId, date);
    }

    /**
     * Vérifie si un utilisateur peut modifier un CR
     * Règle: Modification possible dans les 7 jours ou si statut = BROUILLON
     */
    public boolean canModifyCR(CompteRendu compteRendu) {
        return compteRendu.isModifiable();
    }

    /**
     * Vérifie si un utilisateur peut voir un CR spécifique
     */
    public boolean canViewCR(Utilisateur viewer, CompteRendu compteRendu) {
        // L'utilisateur peut toujours voir ses propres CR
        if (viewer.getId().equals(compteRendu.getUtilisateurId())) {
            return true;
        }

        // Les FD peuvent voir les CR de leurs disciples
        if (viewer.getRole().name().equals("FD")) {
            return compteRenduRepository.findById(compteRendu.getId())
                    .map(cr -> {
                        // Vérifier si le viewer est le FD du propriétaire du CR
                        // Cette logique nécessiterait l'accès à UtilisateurRepository
                        return true; // À implémenter avec le contexte complet
                    })
                    .orElse(false);
        }

        // Les admins peuvent tout voir
        return viewer.getRole().name().equals("ADMIN");
    }

    /**
     * Calcule le taux de régularité d'un utilisateur sur une période
     * Retourne le pourcentage de jours avec CR soumis
     */
    public double calculateRegularityRate(UUID utilisateurId, LocalDate startDate, LocalDate endDate) {
        long totalDays = startDate.until(endDate).getDays() + 1;
        long crCount = compteRenduRepository.countByUtilisateurIdAndDateBetween(
                utilisateurId, startDate, endDate
        );

        if (totalDays == 0) {
            return 0.0;
        }

        return (double) crCount / totalDays * 100.0;
    }

    /**
     * Trouve le CR d'un utilisateur pour une date spécifique
     */
    public Optional<CompteRendu> findCRForDate(UUID utilisateurId, LocalDate date) {
        return compteRenduRepository.findByUtilisateurIdAndDate(utilisateurId, date);
    }

    /**
     * Vérifie si un utilisateur a soumis son CR aujourd'hui
     */
    public boolean hasSubmittedCRToday(UUID utilisateurId) {
        return compteRenduRepository.existsByUtilisateurIdAndDate(
                utilisateurId,
                LocalDate.now()
        );
    }

    /**
     * Compte le nombre de jours consécutifs avec CR soumis
     * (pour gamification - séries)
     */
    public int countConsecutiveDays(UUID utilisateurId) {
        LocalDate currentDate = LocalDate.now();
        int consecutiveDays = 0;

        while (true) {
            if (compteRenduRepository.existsByUtilisateurIdAndDate(utilisateurId, currentDate)) {
                consecutiveDays++;
                currentDate = currentDate.minusDays(1);
            } else {
                break;
            }
        }

        return consecutiveDays;
    }
}
