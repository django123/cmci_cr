package com.cmci.cr.application.usecase;

import com.cmci.cr.application.dto.response.CRResponse;
import com.cmci.cr.domain.model.CompteRendu;
import com.cmci.cr.domain.repository.CompteRenduRepository;
import lombok.RequiredArgsConstructor;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Use Case: Consulter les Comptes Rendus
 */
@RequiredArgsConstructor
public class GetCRUseCase {

    private final CompteRenduRepository compteRenduRepository;

    /**
     * Récupère un CR par son ID
     */
    public CRResponse getById(UUID id) {
        CompteRendu cr = compteRenduRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Compte rendu non trouvé avec l'ID: " + id
                ));

        return mapToResponse(cr);
    }

    /**
     * Récupère tous les CR d'un utilisateur
     */
    public List<CRResponse> getByUtilisateurId(UUID utilisateurId) {
        return compteRenduRepository.findByUtilisateurId(utilisateurId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Récupère les CR d'un utilisateur entre deux dates
     */
    public List<CRResponse> getByUtilisateurIdAndDateRange(
            UUID utilisateurId,
            LocalDate startDate,
            LocalDate endDate
    ) {
        return compteRenduRepository.findByUtilisateurIdAndDateBetween(
                        utilisateurId, startDate, endDate
                )
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Récupère le CR d'un utilisateur pour une date spécifique
     */
    public CRResponse getByUtilisateurIdAndDate(UUID utilisateurId, LocalDate date) {
        CompteRendu cr = compteRenduRepository.findByUtilisateurIdAndDate(utilisateurId, date)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Aucun compte rendu trouvé pour l'utilisateur " + utilisateurId +
                        " à la date " + date
                ));

        return mapToResponse(cr);
    }

    /**
     * Récupère les CR non vus d'un utilisateur
     */
    public List<CRResponse> getUnviewedByUtilisateurId(UUID utilisateurId) {
        return compteRenduRepository.findByUtilisateurIdAndVuParFdFalse(utilisateurId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Mappe un CompteRendu vers CRResponse
     */
    private CRResponse mapToResponse(CompteRendu cr) {
        return CRResponse.builder()
                .id(cr.getId())
                .utilisateurId(cr.getUtilisateurId())
                .date(cr.getDate())
                .rdqd(cr.getRdqd().toString())
                .priereSeule(formatDuration(cr.getPriereSeule()))
                .lectureBiblique(cr.getLectureBiblique())
                .livreBiblique(cr.getLivreBiblique())
                .litteraturePages(cr.getLitteraturePages())
                .litteratureTotal(cr.getLitteratureTotal())
                .litteratureTitre(cr.getLitteratureTitre())
                .priereAutres(cr.getPriereAutres())
                .confession(cr.getConfession())
                .jeune(cr.getJeune())
                .typeJeune(cr.getTypeJeune())
                .evangelisation(cr.getEvangelisation())
                .offrande(cr.getOffrande())
                .notes(cr.getNotes())
                .statut(cr.getStatut().name())
                .vuParFd(cr.getVuParFd())
                .createdAt(cr.getCreatedAt())
                .updatedAt(cr.getUpdatedAt())
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
