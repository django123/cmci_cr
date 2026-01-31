package com.cmci.cr.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO API Response pour un CR d'un subordonné
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubordinateCRApiResponse {
    private UUID id;
    private LocalDate date;
    private String rdqd;
    private String priereSeule;
    private Integer lectureBiblique;
    private String statut;
    private Boolean vuParFd;
    private LocalDateTime createdAt;
}
