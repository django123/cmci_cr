package com.cmci.cr.infrastructure.persistence.mapper;

import com.cmci.cr.domain.model.CompteRendu;
import com.cmci.cr.domain.valueobject.RDQD;
import com.cmci.cr.domain.valueobject.StatutCR;
import com.cmci.cr.infrastructure.persistence.entity.CompteRenduJpaEntity;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Mapper entre CompteRendu (domain) et CompteRenduJpaEntity (infrastructure)
 */
@Component
public class CompteRenduMapper {

    public CompteRenduJpaEntity toJpaEntity(CompteRendu domain) {
        if (domain == null) {
            return null;
        }

        return CompteRenduJpaEntity.builder()
                .id(domain.getId())
                .utilisateurId(domain.getUtilisateurId())
                .date(domain.getDate())
                .rdqd(domain.getRdqd() != null ? domain.getRdqd().toString() : null)
                .priereSeule(durationToString(domain.getPriereSeule()))
                .lectureBiblique(domain.getLectureBiblique())
                .livreBiblique(domain.getLivreBiblique())
                .litteraturePages(domain.getLitteraturePages())
                .litteratureTotal(domain.getLitteratureTotal())
                .litteratureTitre(domain.getLitteratureTitre())
                .priereAutres(domain.getPriereAutres())
                .confession(domain.getConfession())
                .jeune(domain.getJeune())
                .typeJeune(domain.getTypeJeune())
                .evangelisation(domain.getEvangelisation())
                .offrande(domain.getOffrande())
                .notes(domain.getNotes())
                .statut(CompteRenduJpaEntity.StatutCREnum.valueOf(domain.getStatut().name()))
                .vuParFd(domain.getVuParFd())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }

    public CompteRendu toDomain(CompteRenduJpaEntity jpa) {
        if (jpa == null) {
            return null;
        }

        return CompteRendu.builder()
                .id(jpa.getId())
                .utilisateurId(jpa.getUtilisateurId())
                .date(jpa.getDate())
                .rdqd(jpa.getRdqd() != null ? RDQD.fromString(jpa.getRdqd()) : null)
                .priereSeule(stringToDuration(jpa.getPriereSeule()))
                .lectureBiblique(jpa.getLectureBiblique())
                .livreBiblique(jpa.getLivreBiblique())
                .litteraturePages(jpa.getLitteraturePages())
                .litteratureTotal(jpa.getLitteratureTotal())
                .litteratureTitre(jpa.getLitteratureTitre())
                .priereAutres(jpa.getPriereAutres())
                .confession(jpa.getConfession())
                .jeune(jpa.getJeune())
                .typeJeune(jpa.getTypeJeune())
                .evangelisation(jpa.getEvangelisation())
                .offrande(jpa.getOffrande())
                .notes(jpa.getNotes())
                .statut(StatutCR.valueOf(jpa.getStatut().name()))
                .vuParFd(jpa.getVuParFd())
                .createdAt(jpa.getCreatedAt())
                .updatedAt(jpa.getUpdatedAt())
                .build();
    }

    /**
     * Convertit une Duration en String au format HH:mm:ss
     */
    private String durationToString(Duration duration) {
        if (duration == null) {
            return "00:00:00";
        }
        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();
        long seconds = duration.toSecondsPart();
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    /**
     * Convertit un String au format HH:mm:ss en Duration
     */
    private Duration stringToDuration(String str) {
        if (str == null || str.isEmpty()) {
            return Duration.ZERO;
        }
        // Format attendu: HH:mm:ss ou HH:mm
        String[] parts = str.split(":");
        if (parts.length >= 2) {
            long hours = Long.parseLong(parts[0]);
            long minutes = Long.parseLong(parts[1]);
            long seconds = parts.length > 2 ? Long.parseLong(parts[2]) : 0;
            return Duration.ofHours(hours).plusMinutes(minutes).plusSeconds(seconds);
        }
        return Duration.ZERO;
    }
}
