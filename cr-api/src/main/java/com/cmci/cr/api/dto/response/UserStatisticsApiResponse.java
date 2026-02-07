package com.cmci.cr.api.dto.response;

import com.cmci.cr.application.usecase.UserAdministrationUseCase.UserStatistics;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;

/**
 * DTO de réponse API pour les statistiques utilisateurs
 */
@Value
@Builder
@Schema(description = "Statistiques des utilisateurs par rôle")
public class UserStatisticsApiResponse {

    @Schema(description = "Nombre total d'utilisateurs", example = "150")
    int totalUsers;

    @Schema(description = "Nombre de fidèles", example = "100")
    int fideles;

    @Schema(description = "Nombre de FD (Faiseurs de Disciples)", example = "30")
    int fds;

    @Schema(description = "Nombre de leaders", example = "15")
    int leaders;

    @Schema(description = "Nombre de pasteurs", example = "4")
    int pasteurs;

    @Schema(description = "Nombre d'administrateurs", example = "1")
    int admins;

    public static UserStatisticsApiResponse fromDomain(UserStatistics stats) {
        return UserStatisticsApiResponse.builder()
                .totalUsers(stats.totalUsers())
                .fideles(stats.fideles())
                .fds(stats.fds())
                .leaders(stats.leaders())
                .pasteurs(stats.pasteurs())
                .admins(stats.admins())
                .build();
    }
}
