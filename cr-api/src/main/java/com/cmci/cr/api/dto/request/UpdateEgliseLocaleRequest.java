package com.cmci.cr.api.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateEgliseLocaleRequest {

    private String nom;

    private UUID zoneId;

    private String adresse;

    private UUID pasteurId;
}
