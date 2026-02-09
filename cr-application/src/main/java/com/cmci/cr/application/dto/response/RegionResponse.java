package com.cmci.cr.application.dto.response;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.UUID;

@Value
@Builder
public class RegionResponse {
    UUID id;
    String nom;
    String code;
    long nombreZones;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
