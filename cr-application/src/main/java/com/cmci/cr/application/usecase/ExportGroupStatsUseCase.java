package com.cmci.cr.application.usecase;

import com.cmci.cr.application.dto.response.ExportResponse;
import com.cmci.cr.application.dto.response.GroupStatisticsResponse;
import com.cmci.cr.application.service.ExportService;
import com.cmci.cr.domain.model.Utilisateur;
import com.cmci.cr.domain.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Use Case: Exporter les statistiques de groupe en PDF ou Excel (US4.4)
 */
@RequiredArgsConstructor
public class ExportGroupStatsUseCase {

    private final GetGroupStatisticsUseCase getGroupStatisticsUseCase;
    private final UtilisateurRepository utilisateurRepository;
    private final ExportService exportService;

    public ExportResponse execute(UUID fdId, LocalDate startDate, LocalDate endDate, String format) {
        // Récupérer les statistiques de groupe
        GroupStatisticsResponse stats = getGroupStatisticsUseCase.execute(fdId, startDate, endDate);

        // Récupérer le nom du FD/Leader pour le rapport
        Utilisateur fd = utilisateurRepository.findById(fdId)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouv\u00e9 : " + fdId));
        String groupName = "Groupe de " + fd.getPrenom() + " " + fd.getNom();

        // Générer le fichier selon le format
        String period = startDate + "_" + endDate;

        if ("excel".equalsIgnoreCase(format)) {
            byte[] content = exportService.exportGroupStatsToExcel(stats, groupName, startDate, endDate);
            return ExportResponse.builder()
                    .content(content)
                    .filename("statistiques_groupe_" + period + ".xlsx")
                    .contentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                    .build();
        } else {
            byte[] content = exportService.exportGroupStatsToPdf(stats, groupName, startDate, endDate);
            return ExportResponse.builder()
                    .content(content)
                    .filename("statistiques_groupe_" + period + ".pdf")
                    .contentType("application/pdf")
                    .build();
        }
    }
}
