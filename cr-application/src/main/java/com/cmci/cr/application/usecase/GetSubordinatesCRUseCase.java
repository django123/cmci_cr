package com.cmci.cr.application.usecase;

import com.cmci.cr.application.dto.response.SubordinateCRResponse;
import com.cmci.cr.application.dto.response.SubordinateWithCRsResponse;
import com.cmci.cr.domain.model.CompteRendu;
import com.cmci.cr.domain.model.EgliseLocale;
import com.cmci.cr.domain.model.EgliseMaison;
import com.cmci.cr.domain.model.Utilisateur;
import com.cmci.cr.domain.repository.CompteRenduRepository;
import com.cmci.cr.domain.repository.EgliseLocaleRepository;
import com.cmci.cr.domain.repository.EgliseMaisonRepository;
import com.cmci.cr.domain.repository.UtilisateurRepository;
import com.cmci.cr.domain.valueobject.Role;
import lombok.RequiredArgsConstructor;

import java.time.Duration;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Use Case: Voir les CR des subordonnés (Disciples, FD, etc.)
 *
 * Permet à un responsable de consulter les CR de ses subordonnés selon la hiérarchie:
 * - FD → ses disciples directs (fdId)
 * - Leader → tous les membres de son église de maison
 * - Pasteur → tous les membres de toutes les églises de maison de son église locale
 */
@RequiredArgsConstructor
public class GetSubordinatesCRUseCase {

    private final UtilisateurRepository utilisateurRepository;
    private final CompteRenduRepository compteRenduRepository;
    private final EgliseMaisonRepository egliseMaisonRepository;
    private final EgliseLocaleRepository egliseLocaleRepository;

    /**
     * Récupère la liste des subordonnés avec leurs CR sur une période donnée
     *
     * @param responsableId ID du responsable (FD, Leader ou Pasteur)
     * @param startDate Date de début
     * @param endDate Date de fin
     * @return Liste des subordonnés avec leurs CR
     */
    public List<SubordinateWithCRsResponse> execute(UUID responsableId, LocalDate startDate, LocalDate endDate) {
        Utilisateur responsable = utilisateurRepository.findById(responsableId)
                .orElseThrow(() -> new NoSuchElementException("Responsable non trouvé: " + responsableId));

        // Récupérer les subordonnés selon le rôle
        List<Utilisateur> subordinates = getSubordinates(responsable);

        if (subordinates.isEmpty()) {
            return List.of();
        }

        // Récupérer les IDs des subordonnés
        List<UUID> subordinateIds = subordinates.stream()
                .map(Utilisateur::getId)
                .collect(Collectors.toList());

        // Récupérer tous les CR des subordonnés sur la période
        List<CompteRendu> allCRs = compteRenduRepository.findByUtilisateurIdInAndDateBetween(
                subordinateIds, startDate, endDate);

        // Grouper les CR par utilisateur
        Map<UUID, List<CompteRendu>> crsByUser = allCRs.stream()
                .collect(Collectors.groupingBy(CompteRendu::getUtilisateurId));

        // Construire la réponse
        return subordinates.stream()
                .map(subordinate -> buildSubordinateResponse(
                        subordinate,
                        crsByUser.getOrDefault(subordinate.getId(), List.of()),
                        startDate,
                        endDate))
                .sorted(Comparator.comparing(SubordinateWithCRsResponse::getNomComplet))
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
     * FD: récupère ses disciples directs (ceux qui ont fdId = responsable.id)
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
        // Trouver les églises de maison dont ce leader est responsable
        List<EgliseMaison> eglises = egliseMaisonRepository.findByLeaderId(leader.getId());

        if (eglises.isEmpty()) {
            // Si pas d'église assignée, utiliser l'église de maison du leader
            if (leader.getEgliseMaisonId() != null) {
                return utilisateurRepository.findByEgliseMaisonId(leader.getEgliseMaisonId())
                        .stream()
                        .filter(u -> u.getStatut() == Utilisateur.StatutUtilisateur.ACTIF)
                        .filter(u -> !u.getId().equals(leader.getId())) // Exclure le leader lui-même
                        .collect(Collectors.toList());
            }
            return List.of();
        }

        // Récupérer les IDs des églises
        List<UUID> egliseIds = eglises.stream()
                .map(EgliseMaison::getId)
                .collect(Collectors.toList());

        // Récupérer tous les utilisateurs de ces églises
        return utilisateurRepository.findByEgliseMaisonIdIn(egliseIds)
                .stream()
                .filter(u -> u.getStatut() == Utilisateur.StatutUtilisateur.ACTIF)
                .filter(u -> !u.getId().equals(leader.getId())) // Exclure le leader lui-même
                .collect(Collectors.toList());
    }

    /**
     * Pasteur: récupère tous les membres de toutes les églises de maison de son église locale
     */
    private List<Utilisateur> getPasteurSubordinates(Utilisateur pasteur) {
        // Trouver les églises locales dont ce pasteur est responsable
        List<EgliseLocale> eglisesLocales = egliseLocaleRepository.findByPasteurId(pasteur.getId());

        if (eglisesLocales.isEmpty()) {
            return List.of();
        }

        // Récupérer toutes les églises de maison de ces églises locales
        List<UUID> egliseMaisonIds = new ArrayList<>();
        for (EgliseLocale egliseLocale : eglisesLocales) {
            List<EgliseMaison> eglisesMaison = egliseMaisonRepository.findByEgliseLocaleId(egliseLocale.getId());
            eglisesMaison.forEach(em -> egliseMaisonIds.add(em.getId()));
        }

        if (egliseMaisonIds.isEmpty()) {
            return List.of();
        }

        // Récupérer tous les utilisateurs de ces églises de maison
        return utilisateurRepository.findByEgliseMaisonIdIn(egliseMaisonIds)
                .stream()
                .filter(u -> u.getStatut() == Utilisateur.StatutUtilisateur.ACTIF)
                .filter(u -> !u.getId().equals(pasteur.getId())) // Exclure le pasteur lui-même
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
     * Construit la réponse pour un subordonné avec ses CR
     */
    private SubordinateWithCRsResponse buildSubordinateResponse(
            Utilisateur subordinate,
            List<CompteRendu> crs,
            LocalDate startDate,
            LocalDate endDate) {

        // Calculer le nombre de jours sur la période
        long totalDays = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1;

        // Calculer le taux de régularité
        double regularityRate = crs.isEmpty() ? 0.0 : (crs.size() * 100.0 / totalDays);

        // Trouver la date du dernier CR
        LocalDate lastCRDate = crs.stream()
                .map(CompteRendu::getDate)
                .max(LocalDate::compareTo)
                .orElse(null);

        // Calculer le nombre de jours depuis le dernier CR
        Integer daysSinceLastCR = lastCRDate != null
                ? (int) java.time.temporal.ChronoUnit.DAYS.between(lastCRDate, LocalDate.now())
                : null;

        // Déterminer le niveau d'alerte
        String alertLevel = determineAlertLevel(daysSinceLastCR);

        // Convertir les CR en réponses
        List<SubordinateCRResponse> crResponses = crs.stream()
                .sorted(Comparator.comparing(CompteRendu::getDate).reversed())
                .map(this::toCRResponse)
                .collect(Collectors.toList());

        return SubordinateWithCRsResponse.builder()
                .utilisateurId(subordinate.getId())
                .nom(subordinate.getNom())
                .prenom(subordinate.getPrenom())
                .nomComplet(subordinate.getNomComplet())
                .email(subordinate.getEmail())
                .role(subordinate.getRole().name())
                .roleDisplayName(subordinate.getRole().getDisplayName())
                .avatarUrl(subordinate.getAvatarUrl())
                .lastCRDate(lastCRDate)
                .daysSinceLastCR(daysSinceLastCR)
                .regularityRate(Math.round(regularityRate * 100.0) / 100.0)
                .totalCRs(crs.size())
                .alertLevel(alertLevel)
                .hasAlert(!"NONE".equals(alertLevel))
                .compteRendus(crResponses)
                .build();
    }

    private String determineAlertLevel(Integer daysSinceLastCR) {
        if (daysSinceLastCR == null) {
            return "CRITICAL"; // Jamais de CR
        }
        if (daysSinceLastCR >= 7) {
            return "CRITICAL";
        }
        if (daysSinceLastCR >= 3) {
            return "WARNING";
        }
        return "NONE";
    }

    private SubordinateCRResponse toCRResponse(CompteRendu cr) {
        return SubordinateCRResponse.builder()
                .id(cr.getId())
                .date(cr.getDate())
                .rdqd(cr.getRdqd() != null ? cr.getRdqd().toString() : null)
                .priereSeule(formatDuration(cr.getPriereSeule()))
                .lectureBiblique(cr.getLectureBiblique())
                .statut(cr.getStatut().name())
                .vuParFd(cr.getVuParFd())
                .createdAt(cr.getCreatedAt())
                .build();
    }

    /**
     * Formate une durée au format "HH:mm"
     */
    private String formatDuration(Duration duration) {
        if (duration == null) {
            return "00:00";
        }
        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();
        return String.format("%02d:%02d", hours, minutes);
    }
}
