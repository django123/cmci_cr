package com.cmci.cr.api.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO Request pour la mise à jour d'un Compte Rendu
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCompteRenduRequest {

    @Pattern(regexp = "\\d+/\\d+", message = "Le format RDQD doit être 'accompli/attendu' (ex: 5/7)")
    private String rdqd;

    @Min(value = 0, message = "Le temps de prière doit être positif")
    private Integer priereSeuleMinutes;

    @Min(value = 0, message = "Le temps de prière en couple doit être positif")
    private Integer priereCoupleMinutes;

    @Min(value = 0, message = "Le temps de prière avec enfants doit être positif")
    private Integer priereAvecEnfantsMinutes;

    @Min(value = 0, message = "Le temps d'étude doit être positif")
    private Integer tempsEtudeParoleMinutes;

    @Min(value = 0, message = "Le nombre de contacts doit être positif")
    private Integer nombreContactsUtiles;

    @Min(value = 0, message = "Le nombre d'invitations doit être positif")
    private Integer invitationsCulte;

    @DecimalMin(value = "0.0", inclusive = true, message = "L'offrande doit être positive")
    private BigDecimal offrande;

    @Min(value = 0, message = "Le nombre d'évangélisations doit être positif")
    private Integer evangelisations;

    @Size(max = 1000, message = "Le commentaire ne peut pas dépasser 1000 caractères")
    private String commentaire;
}
