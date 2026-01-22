package com.cmci.cr.application.dto.command;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Builder;
import lombok.Value;

import java.util.UUID;

/**
 * Command pour modifier un Compte Rendu existant
 */
@Value
@Builder
public class UpdateCRCommand {

    @NotNull(message = "L'ID du compte rendu est obligatoire")
    UUID id;

    @NotNull(message = "L'utilisateur est obligatoire")
    UUID utilisateurId;

    @Pattern(regexp = "^\\d+/\\d+$", message = "Format RDQD invalide (attendu: accompli/attendu, ex: 1/1)")
    String rdqd;

    String priereSeule; // Format: "HH:mm" ou durée ISO

    String priereCouple; // Format: "HH:mm" ou durée ISO

    String priereAvecEnfants; // Format: "HH:mm" ou durée ISO

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
