package com.cmci.cr.api.controller;

import com.cmci.cr.api.dto.request.AddCommentaireRequest;
import com.cmci.cr.api.mapper.CommentaireApiMapper;
import com.cmci.cr.application.dto.response.CommentaireResponse;
import com.cmci.cr.application.usecase.AddCommentaireUseCase;
import com.cmci.cr.application.usecase.GetCommentairesUseCase;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests unitaires pour CommentaireController
 */
@WebMvcTest(CommentaireController.class)
class CommentaireControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AddCommentaireUseCase addCommentaireUseCase;

    @MockBean
    private GetCommentairesUseCase getCommentairesUseCase;

    @MockBean
    private CommentaireApiMapper mapper;

    @MockBean
    private SecurityContextService securityContextService;

    private UUID testCompteRenduId;
    private UUID testAuteurId;
    private CommentaireResponse testCommentaireResponse;

    @BeforeEach
    void setUp() {
        testCompteRenduId = UUID.randomUUID();
        testAuteurId = UUID.randomUUID();

        testCommentaireResponse = CommentaireResponse.builder()
                .id(UUID.randomUUID())
                .compteRenduId(testCompteRenduId)
                .auteurId(testAuteurId)
                .auteurNom("Doe")
                .auteurPrenom("John")
                .contenu("Test commentaire")
                .createdAt(LocalDateTime.now())
                .build();

        when(securityContextService.getCurrentUserId()).thenReturn(Optional.of(testAuteurId));
    }

    @Test
    @DisplayName("POST /api/v1/cr/{compteRenduId}/commentaires - Devrait ajouter un commentaire")
    @WithMockUser(roles = "FD")
    void shouldAddCommentaire() throws Exception {
        // Given
        AddCommentaireRequest request = AddCommentaireRequest.builder()
                .contenu("Super compte rendu, continue!")
                .build();

        when(addCommentaireUseCase.execute(any())).thenReturn(testCommentaireResponse);
        when(mapper.toApiResponse(any())).thenReturn(null);

        // When & Then
        mockMvc.perform(post("/api/v1/cr/{compteRenduId}/commentaires", testCompteRenduId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("POST /api/v1/cr/{compteRenduId}/commentaires - Devrait rejeter un commentaire vide")
    @WithMockUser(roles = "FD")
    void shouldRejectEmptyComment() throws Exception {
        // Given
        AddCommentaireRequest request = AddCommentaireRequest.builder()
                .contenu("")
                .build();

        // When & Then
        mockMvc.perform(post("/api/v1/cr/{compteRenduId}/commentaires", testCompteRenduId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/v1/cr/{compteRenduId}/commentaires - Devrait récupérer les commentaires")
    @WithMockUser(roles = "FIDELE")
    void shouldGetCommentaires() throws Exception {
        // Given
        when(getCommentairesUseCase.execute(testCompteRenduId))
                .thenReturn(List.of(testCommentaireResponse));
        when(mapper.toApiResponse(any())).thenReturn(null);

        // When & Then
        mockMvc.perform(get("/api/v1/cr/{compteRenduId}/commentaires", testCompteRenduId))
                .andExpect(status().isOk());
    }
}
