package com.cmci.cr.application.usecase;

import com.cmci.cr.application.dto.CRResponse;
import com.cmci.cr.application.dto.UpdateCRCommand;
import com.cmci.cr.domain.model.CompteRendu;
import com.cmci.cr.domain.repository.CompteRenduRepository;
import com.cmci.cr.domain.service.CRDomainService;
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
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour UpdateCRUseCase
 */
@ExtendWith(MockitoExtension.class)
class UpdateCRUseCaseTest {

    @Mock
    private CompteRenduRepository compteRenduRepository;

    @Mock
    private CRDomainService crDomainService;

    private UpdateCRUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new UpdateCRUseCase(compteRenduRepository, crDomainService);
    }

    @Test
    void shouldUpdateCRSuccessfully() {
        // Given
        UUID crId = UUID.randomUUID();
        UUID utilisateurId = UUID.randomUUID();

        CompteRendu existingCR = createTestCR(crId, utilisateurId);

        UpdateCRCommand command = UpdateCRCommand.builder()
                .id(crId)
                .utilisateurId(utilisateurId)
                .rdqd("1/1")
                .priereSeule("02:00")
                .lectureBiblique(10)
                .build();

        when(compteRenduRepository.findById(crId))
                .thenReturn(Optional.of(existingCR));
        when(crDomainService.canModifyCR(existingCR))
                .thenReturn(true);
        when(compteRenduRepository.save(any(CompteRendu.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        CRResponse response = useCase.execute(command);

        // Then
        assertNotNull(response);
        assertEquals("1/1", response.getRdqd());
        assertEquals("02:00", response.getPriereSeule());
        assertEquals(10, response.getLectureBiblique());

        verify(compteRenduRepository).findById(crId);
        verify(crDomainService).canModifyCR(existingCR);
        verify(compteRenduRepository).save(any(CompteRendu.class));
    }

    @Test
    void shouldThrowExceptionWhenCRNotFound() {
        // Given
        UUID crId = UUID.randomUUID();

        UpdateCRCommand command = UpdateCRCommand.builder()
                .id(crId)
                .utilisateurId(UUID.randomUUID())
                .rdqd("1/1")
                .priereSeule("01:00")
                .lectureBiblique(5)
                .build();

        when(compteRenduRepository.findById(crId))
                .thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> useCase.execute(command)
        );

        assertTrue(exception.getMessage().contains("non trouvé"));
        verify(compteRenduRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenUserIsNotOwner() {
        // Given
        UUID crId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        UUID differentUserId = UUID.randomUUID();

        CompteRendu existingCR = createTestCR(crId, ownerId);

        UpdateCRCommand command = UpdateCRCommand.builder()
                .id(crId)
                .utilisateurId(differentUserId) // Different user
                .rdqd("1/1")
                .priereSeule("01:00")
                .lectureBiblique(5)
                .build();

        when(compteRenduRepository.findById(crId))
                .thenReturn(Optional.of(existingCR));

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> useCase.execute(command)
        );

        assertTrue(exception.getMessage().contains("pas autorisé"));
        verify(compteRenduRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenCRIsNotModifiable() {
        // Given
        UUID crId = UUID.randomUUID();
        UUID utilisateurId = UUID.randomUUID();

        CompteRendu existingCR = createTestCR(crId, utilisateurId);

        UpdateCRCommand command = UpdateCRCommand.builder()
                .id(crId)
                .utilisateurId(utilisateurId)
                .rdqd("1/1")
                .priereSeule("01:00")
                .lectureBiblique(5)
                .build();

        when(compteRenduRepository.findById(crId))
                .thenReturn(Optional.of(existingCR));
        when(crDomainService.canModifyCR(existingCR))
                .thenReturn(false);

        // When & Then
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> useCase.execute(command)
        );

        assertTrue(exception.getMessage().contains("ne peut plus être modifié"));
        verify(compteRenduRepository, never()).save(any());
    }

    @Test
    void shouldOnlyUpdateProvidedFields() {
        // Given
        UUID crId = UUID.randomUUID();
        UUID utilisateurId = UUID.randomUUID();

        CompteRendu existingCR = createTestCR(crId, utilisateurId);

        // Command with only some fields
        UpdateCRCommand command = UpdateCRCommand.builder()
                .id(crId)
                .utilisateurId(utilisateurId)
                .notes("Nouvelles notes") // Only update notes
                .build();

        when(compteRenduRepository.findById(crId))
                .thenReturn(Optional.of(existingCR));
        when(crDomainService.canModifyCR(existingCR))
                .thenReturn(true);
        when(compteRenduRepository.save(any(CompteRendu.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        CRResponse response = useCase.execute(command);

        // Then
        assertNotNull(response);
        // Original values preserved
        assertEquals("1/1", response.getRdqd());
        assertEquals(5, response.getLectureBiblique());
    }

    // Helper method
    private CompteRendu createTestCR(UUID id, UUID utilisateurId) {
        return CompteRendu.builder()
                .id(id)
                .utilisateurId(utilisateurId)
                .date(LocalDate.now())
                .rdqd(RDQD.of(1, 1))
                .priereSeule(Duration.ofMinutes(60))
                .lectureBiblique(5)
                .statut(StatutCR.BROUILLON) // Modifiable
                .vuParFd(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
