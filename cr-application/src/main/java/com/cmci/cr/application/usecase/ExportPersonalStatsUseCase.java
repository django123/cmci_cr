package com.cmci.cr.application.usecase;

import com.cmci.cr.application.dto.response.ExportResponse;
import com.cmci.cr.application.dto.response.PersonalStatisticsResponse;
import com.cmci.cr.application.service.ExportService;
import com.cmci.cr.domain.model.Utilisateur;
import com.cmci.cr.domain.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Use Case: Exporter les statistiques personnelles en PDF ou Excel (US4.4)
 */
@RequiredArgsConstructor
public class ExportPersonalStatsUseCase {

    private final GetPersonalStatisticsUseCase getPersonalStatisticsUseCase;
    private final UtilisateurRepository utilisateurRepository;
    private final ExportService exportService;

    public ExportResponse execute(UUID utilisateurId, LocalDate startDate, LocalDate endDate, String format) {
        // Récupérer les statistiques
        PersonalStatisticsResponse stats = getPersonalStatisticsUseCase.execute(utilisateurId, startDate, endDate);

        // Récupérer le nom de l'utilisateur
        Utilisateur utilisateur = utilisateurRepository.findById(utilisateurId)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouv\u00e9 : " + utilisateurId));
        String userName = utilisateur.getPrenom() + " " + utilisateur.getNom();

        // Générer le fichier selon le format
        String period = startDate + "_" + endDate;

        if ("excel".equalsIgnoreCase(format)) {
            byte[] content = exportService.exportPersonalStatsToExcel(stats, userName, startDate, endDate);
            return ExportResponse.builder()
                    .content(content)
                    .filename("statistiques_personnelles_" + period + ".xlsx")
                    .contentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                    .build();
        } else {
            byte[] content = exportService.exportPersonalStatsToPdf(stats, userName, startDate, endDate);
            return ExportResponse.builder()
                    .content(content)
                    .filename("statistiques_personnelles_" + period + ".pdf")
                    .contentType("application/pdf")
                    .build();
        }
    }
}
