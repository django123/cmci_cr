package com.cmci.cr.application.usecase;

import com.cmci.cr.application.dto.CRResponse;
import com.cmci.cr.application.dto.CreateCRCommand;
import com.cmci.cr.domain.model.CompteRendu;
import com.cmci.cr.domain.repository.CompteRenduRepository;
import com.cmci.cr.domain.valueobject.RDQD;
import com.cmci.cr.domain.valueobject.StatutCR;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour CreateCRUseCase
 */
@ExtendWith(MockitoExtension.class)
class CreateCRUseCaseTest {

    @Mock
    private CompteRenduRepository compteRenduRepository;

    private CreateCRUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new CreateCRUseCase(compteRenduRepository);
    }

    @Test
    void shouldCreateCRSuccessfully() {
        // Given
        UUID utilisateurId = UUID.randomUUID();
        LocalDate date = LocalDate.now();

        CreateCRCommand command = CreateCRCommand.builder()
                .utilisateurId(utilisateurId)
                .date(date)
                .rdqd("1/1")
                .priereSeule("01:30")
                .lectureBiblique(5)
                .livreBiblique("Psaumes")
                .build();

        // Mock repository behavior
        when(compteRenduRepository.existsByUtilisateurIdAndDate(utilisateurId, date))
                .thenReturn(false);

        CompteRendu savedCR = CompteRendu.builder()
                .id(UUID.randomUUID())
                .utilisateurId(utilisateurId)
                .date(date)
                .rdqd(RDQD.of(1, 1))
                .priereSeule(Duration.ofMinutes(90))
                .lectureBiblique(5)
                .livreBiblique("Psaumes")
                .statut(StatutCR.SOUMIS)
                .vuParFd(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(compteRenduRepository.save(any(CompteRendu.class)))
                .thenReturn(savedCR);

        // When
        CRResponse response = useCase.execute(command);

        // Then
        assertNotNull(response);
        assertEquals(utilisateurId, response.getUtilisateurId());
        assertEquals(date, response.getDate());
        assertEquals("1/1", response.getRdqd());
        assertEquals("01:30", response.getPriereSeule());
        assertEquals(5, response.getLectureBiblique());
        assertEquals("Psaumes", response.getLivreBiblique());
        assertEquals("SOUMIS", response.getStatut());
        assertFalse(response.getVuParFd());

        // Verify interactions
        verify(compteRenduRepository).existsByUtilisateurIdAndDate(utilisateurId, date);
        verify(compteRenduRepository).save(any(CompteRendu.class));
    }

    @Test
    void shouldThrowExceptionWhenCRAlreadyExistsForDate() {
        // Given
        UUID utilisateurId = UUID.randomUUID();
        LocalDate date = LocalDate.now();

        CreateCRCommand command = CreateCRCommand.builder()
                .utilisateurId(utilisateurId)
                .date(date)
                .rdqd("1/1")
                .priereSeule("01:00")
                .lectureBiblique(3)
                .build();

        when(compteRenduRepository.existsByUtilisateurIdAndDate(utilisateurId, date))
                .thenReturn(true);

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> useCase.execute(command)
        );

        assertTrue(exception.getMessage().contains("existe déjà"));
        verify(compteRenduRepository, never()).save(any());
    }

    @Test
    void shouldSaveCRWithAllOptionalFields() {
        // Given
        UUID utilisateurId = UUID.randomUUID();
        LocalDate date = LocalDate.now();

        CreateCRCommand command = CreateCRCommand.builder()
                .utilisateurId(utilisateurId)
                .date(date)
                .rdqd("1/1")
                .priereSeule("02:00")
                .lectureBiblique(10)
                .livreBiblique("Jean")
                .litteraturePages(25)
                .litteratureTotal(100)
                .litteratureTitre("Le pèlerin")
                .priereAutres(2)
                .confession(true)
                .jeune(true)
                .typeJeune("Complet")
                .evangelisation(3)
                .offrande(true)
                .notes("Excellente journée spirituelle")
                .build();

        when(compteRenduRepository.existsByUtilisateurIdAndDate(any(), any()))
                .thenReturn(false);

        ArgumentCaptor<CompteRendu> crCaptor = ArgumentCaptor.forClass(CompteRendu.class);
        when(compteRenduRepository.save(crCaptor.capture()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        CRResponse response = useCase.execute(command);

        // Then
        CompteRendu capturedCR = crCaptor.getValue();
        assertNotNull(capturedCR);
        assertEquals(25, capturedCR.getLitteraturePages());
        assertEquals(100, capturedCR.getLitteratureTotal());
        assertEquals("Le pèlerin", capturedCR.getLitteratureTitre());
        assertEquals(2, capturedCR.getPriereAutres());
        assertTrue(capturedCR.getConfession());
        assertTrue(capturedCR.getJeune());
        assertEquals("Complet", capturedCR.getTypeJeune());
        assertEquals(3, capturedCR.getEvangelisation());
        assertTrue(capturedCR.getOffrande());
        assertEquals("Excellente journée spirituelle", capturedCR.getNotes());
    }

    @Test
    void shouldHandleDurationInISOFormat() {
        // Given
        UUID utilisateurId = UUID.randomUUID();

        CreateCRCommand command = CreateCRCommand.builder()
                .utilisateurId(utilisateurId)
                .date(LocalDate.now())
                .rdqd("1/1")
                .priereSeule("PT1H30M") // ISO format
                .lectureBiblique(5)
                .build();

        when(compteRenduRepository.existsByUtilisateurIdAndDate(any(), any()))
                .thenReturn(false);

        ArgumentCaptor<CompteRendu> crCaptor = ArgumentCaptor.forClass(CompteRendu.class);
        when(compteRenduRepository.save(crCaptor.capture()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        CRResponse response = useCase.execute(command);

        // Then
        CompteRendu capturedCR = crCaptor.getValue();
        assertEquals(Duration.ofMinutes(90), capturedCR.getPriereSeule());
    }

    @Test
    void shouldThrowExceptionForInvalidDurationFormat() {
        // Given
        CreateCRCommand command = CreateCRCommand.builder()
                .utilisateurId(UUID.randomUUID())
                .date(LocalDate.now())
                .rdqd("1/1")
                .priereSeule("invalid_format")
                .lectureBiblique(5)
                .build();

        when(compteRenduRepository.existsByUtilisateurIdAndDate(any(), any()))
                .thenReturn(false);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> useCase.execute(command));
    }

    @Test
    void shouldSetDefaultValuesForOptionalBooleans() {
        // Given
        UUID utilisateurId = UUID.randomUUID();

        CreateCRCommand command = CreateCRCommand.builder()
                .utilisateurId(utilisateurId)
                .date(LocalDate.now())
                .rdqd("1/1")
                .priereSeule("01:00")
                .lectureBiblique(3)
                // confession, jeune, offrande not set
                .build();

        when(compteRenduRepository.existsByUtilisateurIdAndDate(any(), any()))
                .thenReturn(false);

        ArgumentCaptor<CompteRendu> crCaptor = ArgumentCaptor.forClass(CompteRendu.class);
        when(compteRenduRepository.save(crCaptor.capture()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        useCase.execute(command);

        // Then
        CompteRendu capturedCR = crCaptor.getValue();
        assertFalse(capturedCR.getConfession());
        assertFalse(capturedCR.getJeune());
        assertFalse(capturedCR.getOffrande());
        assertEquals(0, capturedCR.getPriereAutres());
        assertEquals(0, capturedCR.getEvangelisation());
    }
}
