package com.cmci.cr.api.controller;

import com.cmci.cr.api.mapper.StatisticsApiMapper;
import com.cmci.cr.application.dto.response.PersonalStatisticsResponse;
import com.cmci.cr.application.usecase.GetPersonalStatisticsUseCase;
import com.cmci.cr.infrastructure.security.SecurityContextService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests unitaires pour StatisticsController
 */
@WebMvcTest(StatisticsController.class)
class StatisticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GetPersonalStatisticsUseCase getPersonalStatisticsUseCase;

    @MockBean
    private StatisticsApiMapper mapper;

    @MockBean
    private SecurityContextService securityContextService;

    private UUID testUserId;
    private PersonalStatisticsResponse testStatisticsResponse;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();

        testStatisticsResponse = PersonalStatisticsResponse.builder()
                .startDate(LocalDate.now().minusDays(7))
                .endDate(LocalDate.now())
                .totalCRSoumis(5)
                .totalCRValides(4)
                .tauxCompletion(71.43)
                .totalRDQDAccomplis(25)
                .totalRDQDAttendus(35)
                .moyenneRDQD(5.0)
                .totalPriereSeule(Duration.ofMinutes(150))
                .totalPriereCouple(Duration.ofMinutes(75))
                .totalPriereAvecEnfants(Duration.ofMinutes(50))
                .totalTempsEtudeParole(Duration.ofMinutes(225))
                .totalContactsUtiles(15)
                .totalInvitationsCulte(10)
                .totalOffrandes(BigDecimal.valueOf(25000))
                .totalEvangelisations(5)
                .build();

        when(securityContextService.getCurrentUserId()).thenReturn(Optional.of(testUserId));
    }

    @Test
    @DisplayName("GET /api/v1/statistics/personal - Devrait récupérer les statistiques personnelles")
    @WithMockUser(roles = "FIDELE")
    void shouldGetPersonalStatistics() throws Exception {
        // Given
        LocalDate startDate = LocalDate.now().minusDays(7);
        LocalDate endDate = LocalDate.now();

        when(getPersonalStatisticsUseCase.execute(eq(testUserId), any(), any()))
                .thenReturn(testStatisticsResponse);
        when(mapper.toApiResponse(any())).thenReturn(null);

        // When & Then
        mockMvc.perform(get("/api/v1/statistics/personal")
                        .param("startDate", startDate.toString())
                        .param("endDate", endDate.toString()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/v1/statistics/personal - Devrait rejeter les dates manquantes")
    @WithMockUser(roles = "FIDELE")
    void shouldRejectMissingDates() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/statistics/personal"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/v1/statistics/user/{utilisateurId} - Devrait récupérer les stats d'un utilisateur")
    @WithMockUser(roles = "FD")
    void shouldGetUserStatistics() throws Exception {
        // Given
        LocalDate startDate = LocalDate.now().minusDays(30);
        LocalDate endDate = LocalDate.now();

        when(getPersonalStatisticsUseCase.execute(eq(testUserId), any(), any()))
                .thenReturn(testStatisticsResponse);
        when(mapper.toApiResponse(any())).thenReturn(null);

        // When & Then
        mockMvc.perform(get("/api/v1/statistics/user/{utilisateurId}", testUserId)
                        .param("startDate", startDate.toString())
                        .param("endDate", endDate.toString()))
                .andExpect(status().isOk());
    }
}
