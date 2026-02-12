package com.cmci.cr.application.dto.response;

import lombok.Builder;
import lombok.Value;

/**
 * DTO de réponse pour l'export de statistiques (US4.4)
 */
@Value
@Builder
public class ExportResponse {
    byte[] content;
    String filename;
    String contentType;
}
