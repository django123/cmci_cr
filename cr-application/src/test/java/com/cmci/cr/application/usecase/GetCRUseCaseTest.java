package com.cmci.cr.application.usecase;

import com.cmci.cr.application.dto.CRResponse;
import com.cmci.cr.domain.model.CompteRendu;
import com.cmci.cr.domain.repository.CompteRenduRepository;
import com.cmci.cr.domain.valueobject.RDQD;
import com.cmci.cr.domain.valueobject.StatutCR;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour GetCRUseCase
 */
@ExtendWith(MockitoExtension.class)
class GetCRUseCaseTest {

    @Mock
    private CompteRenduRepository compteRenduRepository;

    private GetCRUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new GetCRUseCase(compteRenduRepository);
    }

    @Test
    void shouldGetCRById() {
        // Given
        UUID crId = UUID.randomUUID();
        CompteRendu cr = createTestCR(crId);

        when(compteRenduRepository.findById(crId))
                .thenReturn(Optional.of(cr));

        // When
        CRResponse response = useCase.getById(crId);

        // Then
        assertNotNull(response);
        assertEquals(crId, response.getId());
        verify(compteRenduRepository).findById(crId);
    }

    @Test
    void shouldThrowExceptionWhenCRNotFoundById() {
        // Given
        UUID crId = UUID.randomUUID();

        when(compteRenduRepository.findById(crId))
                .thenReturn(Optional.empty());

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> useCase.getById(crId));
    }

    @Test
    void shouldGetCRsByUtilisateurId() {
        // Given
        UUID utilisateurId = UUID.randomUUID();
        List<CompteRendu> crs = Arrays.asList(
                createTestCR(UUID.randomUUID()),
                createTestCR(UUID.randomUUID())
        );

        when(compteRenduRepository.findByUtilisateurId(utilisateurId))
                .thenReturn(crs);

        // When
        List<CRResponse> responses = useCase.getByUtilisateurId(utilisateurId);

        // Then
        assertEquals(2, responses.size());
        verify(compteRenduRepository).findByUtilisateurId(utilisateurId);
    }

    @Test
    void shouldGetCRsByDateRange() {
        // Given
        UUID utilisateurId = UUID.randomUUID();
        LocalDate startDate = LocalDate.now().minusDays(7);
        LocalDate endDate = LocalDate.now();

        List<CompteRendu> crs = Arrays.asList(createTestCR(UUID.randomUUID()));

        when(compteRenduRepository.findByUtilisateurIdAndDateBetween(utilisateurId, startDate, endDate))
                .thenReturn(crs);

        // When
        List<CRResponse> responses = useCase.getByUtilisateurIdAndDateRange(utilisateurId, startDate, endDate);

        // Then
        assertEquals(1, responses.size());
        verify(compteRenduRepository).findByUtilisateurIdAndDateBetween(utilisateurId, startDate, endDate);
    }

    @Test
    void shouldGetCRByUtilisateurIdAndDate() {
        // Given
        UUID utilisateurId = UUID.randomUUID();
        LocalDate date = LocalDate.now();
        CompteRendu cr = createTestCR(UUID.randomUUID());

        when(compteRenduRepository.findByUtilisateurIdAndDate(utilisateurId, date))
                .thenReturn(Optional.of(cr));

        // When
        CRResponse response = useCase.getByUtilisateurIdAndDate(utilisateurId, date);

        // Then
        assertNotNull(response);
        verify(compteRenduRepository).findByUtilisateurIdAndDate(utilisateurId, date);
    }

    @Test
    void shouldGetUnviewedCRs() {
        // Given
        UUID utilisateurId = UUID.randomUUID();
        List<CompteRendu> crs = Arrays.asList(createTestCR(UUID.randomUUID()));

        when(compteRenduRepository.findByUtilisateurIdAndVuParFdFalse(utilisateurId))
                .thenReturn(crs);

        // When
        List<CRResponse> responses = useCase.getUnviewedByUtilisateurId(utilisateurId);

        // Then
        assertEquals(1, responses.size());
        verify(compteRenduRepository).findByUtilisateurIdAndVuParFdFalse(utilisateurId);
    }

    // Helper
    private CompteRendu createTestCR(UUID id) {
        return CompteRendu.builder()
                .id(id)
                .utilisateurId(UUID.randomUUID())
                .date(LocalDate.now())
                .rdqd(RDQD.of(1, 1))
                .priereSeule(Duration.ofMinutes(60))
                .lectureBiblique(5)
                .statut(StatutCR.SOUMIS)
                .vuParFd(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
