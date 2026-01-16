package com.cmci.cr.api.controller;

import com.cmci.cr.api.dto.request.CreateCompteRenduRequest;
import com.cmci.cr.api.dto.request.UpdateCompteRenduRequest;
import com.cmci.cr.api.mapper.CompteRenduApiMapper;
import com.cmci.cr.application.dto.response.CRResponse;
import com.cmci.cr.application.usecase.*;
import com.cmci.cr.infrastructure.security.SecurityContextService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests unitaires pour CompteRenduController
 */
@WebMvcTest(CompteRenduController.class)
class CompteRenduControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CreateCRUseCase createCRUseCase;

    @MockBean
    private UpdateCRUseCase updateCRUseCase;

    @MockBean
    private GetCRUseCase getCRUseCase;

    @MockBean
    private DeleteCRUseCase deleteCRUseCase;

    @MockBean
    private ValidateCRUseCase validateCRUseCase;

    @MockBean
    private MarkAsViewedUseCase markAsViewedUseCase;

    @MockBean
    private CompteRenduApiMapper mapper;

    @MockBean
    private SecurityContextService securityContextService;

    private UUID testUserId;
    private UUID testCRId;
    private CRResponse testCRResponse;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testCRId = UUID.randomUUID();

        testCRResponse = CRResponse.builder()
                .id(testCRId)
                .utilisateurId(testUserId)
                .date(LocalDate.now())
                .rdqd("5/7")
                .priereSeule(Duration.ofMinutes(30))
                .priereCouple(Duration.ofMinutes(15))
                .priereAvecEnfants(Duration.ofMinutes(10))
                .tempsEtudeParole(Duration.ofMinutes(45))
                .nombreContactsUtiles(3)
                .invitationsCulte(2)
                .offrande(BigDecimal.valueOf(5000))
                .evangelisations(1)
                .commentaire("Test commentaire")
                .statut("BROUILLON")
                .vuParFd(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // Mock SecurityContextService
        when(securityContextService.getCurrentUserId()).thenReturn(Optional.of(testUserId));
    }

    @Test
    @DisplayName("POST /api/v1/cr - Devrait créer un compte rendu")
    @WithMockUser(roles = "FIDELE")
    void shouldCreateCompteRendu() throws Exception {
        // Given
        CreateCompteRenduRequest request = CreateCompteRenduRequest.builder()
                .date(LocalDate.now())
                .rdqd("5/7")
                .priereSeuleMinutes(30)
                .priereCoupleMinutes(15)
                .priereAvecEnfantsMinutes(10)
                .tempsEtudeParoleMinutes(45)
                .nombreContactsUtiles(3)
                .invitationsCulte(2)
                .offrande(BigDecimal.valueOf(5000))
                .evangelisations(1)
                .commentaire("Test")
                .build();

        when(createCRUseCase.execute(any())).thenReturn(testCRResponse);
        when(mapper.toApiResponse(any())).thenReturn(null); // Simplified for test

        // When & Then
        mockMvc.perform(post("/api/v1/cr")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("POST /api/v1/cr - Devrait rejeter une requête invalide")
    @WithMockUser(roles = "FIDELE")
    void shouldRejectInvalidRequest() throws Exception {
        // Given - Request without required fields
        CreateCompteRenduRequest request = CreateCompteRenduRequest.builder()
                .build();

        // When & Then
        mockMvc.perform(post("/api/v1/cr")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/v1/cr/{id} - Devrait récupérer un compte rendu")
    @WithMockUser(roles = "FIDELE")
    void shouldGetCompteRendu() throws Exception {
        // Given
        when(getCRUseCase.execute(testCRId)).thenReturn(Optional.of(testCRResponse));
        when(mapper.toApiResponse(testCRResponse)).thenReturn(null); // Simplified

        // When & Then
        mockMvc.perform(get("/api/v1/cr/{id}", testCRId))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/v1/cr/{id} - Devrait retourner 404 si non trouvé")
    @WithMockUser(roles = "FIDELE")
    void shouldReturn404WhenNotFound() throws Exception {
        // Given
        when(getCRUseCase.execute(testCRId)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/v1/cr/{id}", testCRId))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /api/v1/cr/{id} - Devrait mettre à jour un compte rendu")
    @WithMockUser(roles = "FIDELE")
    void shouldUpdateCompteRendu() throws Exception {
        // Given
        UpdateCompteRenduRequest request = UpdateCompteRenduRequest.builder()
                .commentaire("Updated comment")
                .build();

        when(updateCRUseCase.execute(any())).thenReturn(testCRResponse);
        when(mapper.toApiResponse(any())).thenReturn(null);

        // When & Then
        mockMvc.perform(put("/api/v1/cr/{id}", testCRId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /api/v1/cr/{id} - Devrait supprimer un compte rendu")
    @WithMockUser(roles = "FIDELE")
    void shouldDeleteCompteRendu() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/v1/cr/{id}", testCRId)
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("GET /api/v1/cr/user/{utilisateurId} - Devrait récupérer les CR d'un utilisateur")
    @WithMockUser(roles = "FIDELE")
    void shouldGetUserCompteRendus() throws Exception {
        // Given
        when(getCRUseCase.getByUtilisateur(testUserId)).thenReturn(List.of(testCRResponse));
        when(mapper.toApiResponse(any())).thenReturn(null);

        // When & Then
        mockMvc.perform(get("/api/v1/cr/user/{utilisateurId}", testUserId))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /api/v1/cr/{id}/validate - Devrait valider un compte rendu")
    @WithMockUser(roles = "FD")
    void shouldValidateCompteRendu() throws Exception {
        // Given
        when(validateCRUseCase.execute(eq(testCRId), any())).thenReturn(testCRResponse);
        when(mapper.toApiResponse(any())).thenReturn(null);

        // When & Then
        mockMvc.perform(post("/api/v1/cr/{id}/validate", testCRId)
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /api/v1/cr/{id}/mark-viewed - Devrait marquer comme vu")
    @WithMockUser(roles = "FD")
    void shouldMarkAsViewed() throws Exception {
        // Given
        when(markAsViewedUseCase.execute(eq(testCRId), any())).thenReturn(testCRResponse);
        when(mapper.toApiResponse(any())).thenReturn(null);

        // When & Then
        mockMvc.perform(post("/api/v1/cr/{id}/mark-viewed", testCRId)
                        .with(csrf()))
                .andExpect(status().isOk());
    }
}
