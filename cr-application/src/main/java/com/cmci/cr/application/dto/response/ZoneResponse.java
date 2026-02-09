package com.cmci.cr.application.dto.response;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.UUID;

@Value
@Builder
public class ZoneResponse {
    UUID id;
    String nom;
    UUID regionId;
    String regionNom;
    long nombreEglisesLocales;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
