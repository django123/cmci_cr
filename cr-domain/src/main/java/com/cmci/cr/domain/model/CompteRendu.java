package com.cmci.cr.domain.model;

import com.cmci.cr.domain.valueobject.RDQD;
import com.cmci.cr.domain.valueobject.StatutCR;
import lombok.Builder;
import lombok.Value;
import lombok.With;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entité Compte Rendu (CR) - Aggregate Root
 * Représente un compte rendu spirituel quotidien d'un fidèle
 */
@Value
@Builder
@With
public class CompteRendu {
    UUID id;
    UUID utilisateurId;
    LocalDate date;

    // Champs obligatoires
    RDQD rdqd;
    Duration priereSeule;
    Integer lectureBiblique;

    // Champs optionnels
    String livreBiblique;
    Integer litteraturePages;
    Integer litteratureTotal;
    String litteratureTitre;
    Integer priereAutres;
    Boolean confession;
    Boolean jeune;
    String typeJeune;
    Integer evangelisation;
    Boolean offrande;
    String notes;

    // Métadonnées
    StatutCR statut;
    Boolean vuParFd;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;

    /**
     * Validation métier: un CR doit avoir les champs obligatoires
     */
    public void validate() {
        if (utilisateurId == null) {
            throw new IllegalStateException("Le CR doit être associé à un utilisateur");
        }
        if (date == null) {
            throw new IllegalStateException("Le CR doit avoir une date");
        }
        if (date.isAfter(LocalDate.now())) {
            throw new IllegalStateException("Le CR ne peut pas être dans le futur");
        }
        if (rdqd == null) {
            throw new IllegalStateException("Le RDQD est obligatoire");
        }
        if (priereSeule == null || priereSeule.isNegative()) {
            throw new IllegalStateException("La durée de prière seule est obligatoire et doit être positive");
        }
        if (lectureBiblique == null || lectureBiblique < 0) {
            throw new IllegalStateException("Le nombre de chapitres de lecture biblique est obligatoire et doit être positif");
        }
    }

    /**
     * Vérifie si le CR peut être modifié
     */
    public boolean isModifiable() {
        return statut == StatutCR.BROUILLON ||
                (statut == StatutCR.SOUMIS && createdAt.isAfter(LocalDateTime.now().minusDays(7)));
    }

    /**
     * Marque le CR comme vu par le FD
     */
    public CompteRendu marquerCommeVu() {
        if (statut != StatutCR.SOUMIS) {
            throw new IllegalStateException("Seuls les CR soumis peuvent être marqués comme vus");
        }
        return this.withVuParFd(true)
                .withUpdatedAt(LocalDateTime.now());
    }

    /**
     * Valide le CR (changement de statut vers VALIDE)
     */
    public CompteRendu valider() {
        if (statut != StatutCR.SOUMIS) {
            throw new IllegalStateException("Seuls les CR soumis peuvent être validés");
        }
        return this.withStatut(StatutCR.VALIDE)
                .withVuParFd(true)
                .withUpdatedAt(LocalDateTime.now());
    }

    /**
     * Soumet le CR (changement de statut de BROUILLON vers SOUMIS)
     */
    public CompteRendu soumettre() {
        if (statut != StatutCR.BROUILLON) {
            throw new IllegalStateException("Seuls les CR en brouillon peuvent être soumis");
        }
        validate();
        return this.withStatut(StatutCR.SOUMIS)
                .withUpdatedAt(LocalDateTime.now());
    }
}
