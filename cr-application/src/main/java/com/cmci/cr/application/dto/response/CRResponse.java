package com.cmci.cr.application.dto.response;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO de réponse pour un Compte Rendu
 */
@Value
@Builder
public class CRResponse {
    UUID id;
    UUID utilisateurId;
    LocalDate date;
    String rdqd;
    String priereSeule;
    Integer lectureBiblique;
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
    String statut;
    Boolean vuParFd;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
