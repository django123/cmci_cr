package com.cmci.cr.application.dto.response;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO de réponse pour un CR d'un subordonné
 */
@Value
@Builder
public class SubordinateCRResponse {
    UUID id;
    LocalDate date;
    String rdqd;
    String priereSeule;
    Integer lectureBiblique;
    String statut;
    Boolean vuParFd;
    LocalDateTime createdAt;
}
