package com.cmci.cr.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO Response pour un Compte Rendu
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompteRenduResponse {

    private UUID id;
    private UUID utilisateurId;
    private LocalDate date;
    private String rdqd;
    private String priereSeule;
    private Integer lectureBiblique;
    private String livreBiblique;
    private Integer litteraturePages;
    private Integer litteratureTotal;
    private String litteratureTitre;
    private Integer priereAutres;
    private Boolean confession;
    private Boolean jeune;
    private String typeJeune;
    private Integer evangelisation;
    private Boolean offrande;
    private String notes;
    private String statut;
    private Boolean vuParFd;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
