package com.cmci.cr.api.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO Request pour la création d'un Compte Rendu
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCompteRenduRequest {

    @NotNull(message = "La date est obligatoire")
    @PastOrPresent(message = "La date ne peut pas être dans le futur")
    private LocalDate date;

    @NotNull(message = "Le RDQD est obligatoire")
    @Pattern(regexp = "\\d+/\\d+", message = "Le format RDQD doit être 'accompli/attendu' (ex: 5/7)")
    private String rdqd;

    @NotNull(message = "Le temps de prière seule est obligatoire")
    @Min(value = 0, message = "Le temps de prière doit être positif")
    private Integer priereSeuleMinutes;

    @Min(value = 0, message = "Le temps de prière en couple doit être positif")
    private Integer priereCoupleMinutes;

    @Min(value = 0, message = "Le temps de prière avec enfants doit être positif")
    private Integer priereAvecEnfantsMinutes;

    @Min(value = 0, message = "Le nombre de prières avec d'autres doit être positif")
    private Integer priereAutres;

    @NotNull(message = "Le nombre de chapitres lus est obligatoire")
    @Min(value = 0, message = "Le nombre de chapitres doit être positif")
    private Integer lectureBiblique;

    private String livreBiblique;

    @Min(value = 0, message = "Le nombre de pages doit être positif")
    private Integer litteraturePages;

    @Min(value = 0, message = "Le nombre total de pages doit être positif")
    private Integer litteratureTotal;

    private String litteratureTitre;

    private Boolean confession;

    private Boolean jeune;

    private String typeJeune;

    @Min(value = 0, message = "Le nombre d'évangélisations doit être positif")
    private Integer evangelisation;

    private Boolean offrande;

    @Size(max = 1000, message = "Les notes ne peuvent pas dépasser 1000 caractères")
    private String notes;
}
