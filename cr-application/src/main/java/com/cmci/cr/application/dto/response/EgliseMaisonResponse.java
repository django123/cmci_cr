package com.cmci.cr.application.dto.response;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.UUID;

@Value
@Builder
public class EgliseMaisonResponse {
    UUID id;
    String nom;
    UUID egliseLocaleId;
    String egliseLocaleNom;
    UUID leaderId;
    String leaderNom;
    String adresse;
    long nombreFideles;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
