package com.cmci.cr.api.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
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

    @NotNull(message = "Le temps d'étude de la Parole est obligatoire")
    @Min(value = 0, message = "Le temps d'étude doit être positif")
    private Integer tempsEtudeParoleMinutes;

    @NotNull(message = "Le nombre de contacts utiles est obligatoire")
    @Min(value = 0, message = "Le nombre de contacts doit être positif")
    private Integer nombreContactsUtiles;

    @NotNull(message = "Le nombre d'invitations au culte est obligatoire")
    @Min(value = 0, message = "Le nombre d'invitations doit être positif")
    private Integer invitationsCulte;

    @NotNull(message = "L'offrande est obligatoire")
    @DecimalMin(value = "0.0", inclusive = true, message = "L'offrande doit être positive")
    private BigDecimal offrande;

    @NotNull(message = "Le nombre d'évangélisations est obligatoire")
    @Min(value = 0, message = "Le nombre d'évangélisations doit être positif")
    private Integer evangelisations;

    @Size(max = 1000, message = "Le commentaire ne peut pas dépasser 1000 caractères")
    private String commentaire;
}
