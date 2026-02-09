package com.cmci.cr.api.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateRegionRequest {

    private String nom;

    @Size(max = 10, message = "Le code ne peut pas dépasser 10 caractères")
    private String code;
}
