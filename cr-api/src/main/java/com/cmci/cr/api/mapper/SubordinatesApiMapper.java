package com.cmci.cr.api.mapper;

import com.cmci.cr.api.dto.response.SubordinateCRApiResponse;
import com.cmci.cr.api.dto.response.SubordinateWithCRsApiResponse;
import com.cmci.cr.application.dto.response.SubordinateCRResponse;
import com.cmci.cr.application.dto.response.SubordinateWithCRsResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper entre les DTOs API et les DTOs Application pour les subordonnés et leurs CR
 */
@Component
public class SubordinatesApiMapper {

    /**
     * Convertit SubordinateWithCRsResponse (application) en SubordinateWithCRsApiResponse (API)
     */
    public SubordinateWithCRsApiResponse toApiResponse(SubordinateWithCRsResponse appResponse) {
        return SubordinateWithCRsApiResponse.builder()
                .utilisateurId(appResponse.getUtilisateurId())
                .nom(appResponse.getNom())
                .prenom(appResponse.getPrenom())
                .nomComplet(appResponse.getNomComplet())
                .email(appResponse.getEmail())
                .role(appResponse.getRole())
                .roleDisplayName(appResponse.getRoleDisplayName())
                .avatarUrl(appResponse.getAvatarUrl())
                .lastCRDate(appResponse.getLastCRDate())
                .daysSinceLastCR(appResponse.getDaysSinceLastCR())
                .regularityRate(appResponse.getRegularityRate())
                .totalCRs(appResponse.getTotalCRs())
                .alertLevel(appResponse.getAlertLevel())
                .hasAlert(appResponse.getHasAlert())
                .compteRendus(toCRApiResponses(appResponse.getCompteRendus()))
                .build();
    }

    /**
     * Convertit une liste de SubordinateCRResponse en SubordinateCRApiResponse
     */
    private List<SubordinateCRApiResponse> toCRApiResponses(List<SubordinateCRResponse> appResponses) {
        if (appResponses == null) {
            return List.of();
        }
        return appResponses.stream()
                .map(this::toCRApiResponse)
                .collect(Collectors.toList());
    }

    /**
     * Convertit SubordinateCRResponse (application) en SubordinateCRApiResponse (API)
     */
    private SubordinateCRApiResponse toCRApiResponse(SubordinateCRResponse appResponse) {
        return SubordinateCRApiResponse.builder()
                .id(appResponse.getId())
                .date(appResponse.getDate())
                .rdqd(appResponse.getRdqd())
                .priereSeule(appResponse.getPriereSeule())
                .lectureBiblique(appResponse.getLectureBiblique())
                .statut(appResponse.getStatut())
                .vuParFd(appResponse.getVuParFd())
                .createdAt(appResponse.getCreatedAt())
                .build();
    }

    /**
     * Convertit une liste de SubordinateWithCRsResponse en SubordinateWithCRsApiResponse
     */
    public List<SubordinateWithCRsApiResponse> toApiResponses(List<SubordinateWithCRsResponse> appResponses) {
        return appResponses.stream()
                .map(this::toApiResponse)
                .collect(Collectors.toList());
    }
}
