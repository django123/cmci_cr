package com.cmci.cr.application.dto.command;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Command pour créer un nouveau Compte Rendu
 */
@Value
@Builder
public class CreateCRCommand {

    @NotNull(message = "L'utilisateur est obligatoire")
    UUID utilisateurId;

    @NotNull(message = "La date est obligatoire")
    @PastOrPresent(message = "La date ne peut pas être dans le futur")
    LocalDate date;

    @NotNull(message = "Le RDQD est obligatoire")
    @Pattern(regexp = "^\\d+/\\d+$", message = "Format RDQD invalide (attendu: accompli/attendu, ex: 1/1)")
    String rdqd;

    @NotNull(message = "La durée de prière seule est obligatoire")
    String priereSeule; // Format: "HH:mm" ou durée ISO

    String priereCouple; // Format: "HH:mm" ou durée ISO

    String priereAvecEnfants; // Format: "HH:mm" ou durée ISO

    @NotNull(message = "Le nombre de chapitres de lecture biblique est obligatoire")
    @PositiveOrZero(message = "Le nombre de chapitres doit être positif ou zéro")
    Integer lectureBiblique;

    String livreBiblique;

    @PositiveOrZero(message = "Le nombre de pages doit être positif ou zéro")
    Integer litteraturePages;

    @PositiveOrZero(message = "Le total de pages doit être positif ou zéro")
    Integer litteratureTotal;

    String litteratureTitre;

    @PositiveOrZero(message = "Le nombre de prières avec d'autres doit être positif ou zéro")
    Integer priereAutres;

    Boolean confession;
    Boolean jeune;
    String typeJeune;

    @PositiveOrZero(message = "Le nombre de personnes évangélisées doit être positif ou zéro")
    Integer evangelisation;

    Boolean offrande;
    String notes;
}
