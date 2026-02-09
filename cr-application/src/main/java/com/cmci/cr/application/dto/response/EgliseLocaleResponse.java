package com.cmci.cr.application.dto.response;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.UUID;

@Value
@Builder
public class EgliseLocaleResponse {
    UUID id;
    String nom;
    UUID zoneId;
    String zoneNom;
    String adresse;
    UUID pasteurId;
    String pasteurNom;
    long nombreEglisesMaison;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
