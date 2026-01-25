package com.cmci.cr.application.usecase;

import com.cmci.cr.application.dto.response.CRResponse;
import com.cmci.cr.application.dto.command.CreateCRCommand;
import com.cmci.cr.domain.model.CompteRendu;
import com.cmci.cr.domain.repository.CompteRenduRepository;
import com.cmci.cr.domain.valueobject.RDQD;
import com.cmci.cr.domain.valueobject.StatutCR;
import lombok.RequiredArgsConstructor;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Use Case: Créer un nouveau Compte Rendu
 */
@RequiredArgsConstructor
public class CreateCRUseCase {

    private final CompteRenduRepository compteRenduRepository;

    /**
     * Exécute le use case de création d'un CR
     *
     * @param command Commande de création
     * @return Le CR créé
     * @throws IllegalArgumentException si un CR existe déjà pour cette date
     */
    public CRResponse execute(CreateCRCommand command) {
        // Vérifier qu'aucun CR n'existe déjà pour cette date
        if (compteRenduRepository.existsByUtilisateurIdAndDate(
                command.getUtilisateurId(),
                command.getDate())) {
            throw new IllegalArgumentException(
                    "Un compte rendu existe déjà pour la date " + command.getDate()
            );
        }

        // Créer le CR
        CompteRendu compteRendu = CompteRendu.builder()
                .id(UUID.randomUUID())
                .utilisateurId(command.getUtilisateurId())
                .date(command.getDate())
                .rdqd(RDQD.fromString(command.getRdqd()))
                .priereSeule(parseDuration(command.getPriereSeule()))
                .lectureBiblique(command.getLectureBiblique())
                .livreBiblique(command.getLivreBiblique())
                .litteraturePages(command.getLitteraturePages())
                .litteratureTotal(command.getLitteratureTotal())
                .litteratureTitre(command.getLitteratureTitre())
                .priereAutres(command.getPriereAutres() != null ? command.getPriereAutres() : 0)
                .confession(command.getConfession() != null ? command.getConfession() : false)
                .jeune(command.getJeune() != null ? command.getJeune() : false)
                .typeJeune(command.getTypeJeune())
                .evangelisation(command.getEvangelisation() != null ? command.getEvangelisation() : 0)
                .offrande(command.getOffrande() != null ? command.getOffrande() : false)
                .notes(command.getNotes())
                .statut(StatutCR.SOUMIS)
                .vuParFd(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // Valider le CR
        compteRendu.validate();

        // Sauvegarder
        CompteRendu saved = compteRenduRepository.save(compteRendu);

        // Mapper vers le DTO de réponse
        return mapToResponse(saved);
    }

    /**
     * Parse une durée au format "HH:mm" ou ISO
     */
    private Duration parseDuration(String durationStr) {
        try {
            // Essayer le format HH:mm
            if (durationStr.contains(":")) {
                String[] parts = durationStr.split(":");
                int hours = Integer.parseInt(parts[0]);
                int minutes = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;
                return Duration.ofHours(hours).plusMinutes(minutes);
            }
            // Sinon essayer le format ISO
            return Duration.parse(durationStr);
        } catch (Exception e) {
            throw new IllegalArgumentException("Format de durée invalide: " + durationStr, e);
        }
    }

    /**
     * Mappe un CompteRendu vers CRResponse
     */
    private CRResponse mapToResponse(CompteRendu cr) {
        return CRResponse.builder()
                .id(cr.getId())
                .utilisateurId(cr.getUtilisateurId())
                .date(cr.getDate())
                .rdqd(cr.getRdqd() != null ? cr.getRdqd().toString() : "0/1")
                .priereSeule(formatDuration(cr.getPriereSeule()))
                .lectureBiblique(cr.getLectureBiblique() != null ? cr.getLectureBiblique() : 0)
                .livreBiblique(cr.getLivreBiblique())
                .litteraturePages(cr.getLitteraturePages())
                .litteratureTotal(cr.getLitteratureTotal())
                .litteratureTitre(cr.getLitteratureTitre())
                .priereAutres(cr.getPriereAutres() != null ? cr.getPriereAutres() : 0)
                .confession(cr.getConfession() != null ? cr.getConfession() : false)
                .jeune(cr.getJeune() != null ? cr.getJeune() : false)
                .typeJeune(cr.getTypeJeune())
                .evangelisation(cr.getEvangelisation() != null ? cr.getEvangelisation() : 0)
                .offrande(cr.getOffrande() != null ? cr.getOffrande() : false)
                .notes(cr.getNotes())
                .statut(cr.getStatut() != null ? cr.getStatut().name() : "BROUILLON")
                .vuParFd(cr.getVuParFd() != null ? cr.getVuParFd() : false)
                .createdAt(cr.getCreatedAt())
                .updatedAt(cr.getUpdatedAt())
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
