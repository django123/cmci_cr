package com.cmci.cr.domain.model;

import com.cmci.cr.domain.valueobject.RDQD;
import com.cmci.cr.domain.valueobject.StatutCR;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour l'entité CompteRendu
 */
class CompteRenduTest {

    @Test
    void shouldCreateValidCompteRendu() {
        // Given
        UUID id = UUID.randomUUID();
        UUID utilisateurId = UUID.randomUUID();
        LocalDate date = LocalDate.now();
        RDQD rdqd = RDQD.of(1, 1);
        Duration priereSeule = Duration.ofMinutes(60);
        Integer lectureBiblique = 5;

        // When
        CompteRendu cr = CompteRendu.builder()
                .id(id)
                .utilisateurId(utilisateurId)
                .date(date)
                .rdqd(rdqd)
                .priereSeule(priereSeule)
                .lectureBiblique(lectureBiblique)
                .statut(StatutCR.SOUMIS)
                .vuParFd(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // Then
        assertNotNull(cr);
        assertEquals(id, cr.getId());
        assertEquals(utilisateurId, cr.getUtilisateurId());
        assertEquals(date, cr.getDate());
        assertEquals(rdqd, cr.getRdqd());
        assertEquals(priereSeule, cr.getPriereSeule());
        assertEquals(lectureBiblique, cr.getLectureBiblique());
    }

    @Test
    void shouldValidateSuccessfully() {
        // Given
        CompteRendu cr = createValidCR();

        // When & Then
        assertDoesNotThrow(() -> cr.validate());
    }

    @Test
    void shouldThrowExceptionWhenUtilisateurIdIsNull() {
        // Given
        CompteRendu cr = createValidCR().withUtilisateurId(null);

        // When & Then
        assertThrows(IllegalStateException.class, () -> cr.validate());
    }

    @Test
    void shouldThrowExceptionWhenDateIsNull() {
        // Given
        CompteRendu cr = createValidCR().withDate(null);

        // When & Then
        assertThrows(IllegalStateException.class, () -> cr.validate());
    }

    @Test
    void shouldThrowExceptionWhenDateIsInFuture() {
        // Given
        CompteRendu cr = createValidCR().withDate(LocalDate.now().plusDays(1));

        // When & Then
        assertThrows(IllegalStateException.class, () -> cr.validate());
    }

    @Test
    void shouldThrowExceptionWhenRdqdIsNull() {
        // Given
        CompteRendu cr = createValidCR().withRdqd(null);

        // When & Then
        assertThrows(IllegalStateException.class, () -> cr.validate());
    }

    @Test
    void shouldThrowExceptionWhenPriereSeuleIsNull() {
        // Given
        CompteRendu cr = createValidCR().withPriereSeule(null);

        // When & Then
        assertThrows(IllegalStateException.class, () -> cr.validate());
    }

    @Test
    void shouldThrowExceptionWhenPriereSeuleIsNegative() {
        // Given
        CompteRendu cr = createValidCR().withPriereSeule(Duration.ofMinutes(-10));

        // When & Then
        assertThrows(IllegalStateException.class, () -> cr.validate());
    }

    @Test
    void shouldThrowExceptionWhenLectureBibliqueIsNull() {
        // Given
        CompteRendu cr = createValidCR().withLectureBiblique(null);

        // When & Then
        assertThrows(IllegalStateException.class, () -> cr.validate());
    }

    @Test
    void shouldThrowExceptionWhenLectureBibliqueIsNegative() {
        // Given
        CompteRendu cr = createValidCR().withLectureBiblique(-1);

        // When & Then
        assertThrows(IllegalStateException.class, () -> cr.validate());
    }

    @Test
    void brouillonCRIsModifiable() {
        // Given
        CompteRendu cr = createValidCR().withStatut(StatutCR.BROUILLON);

        // When
        boolean isModifiable = cr.isModifiable();

        // Then
        assertTrue(isModifiable);
    }

    @Test
    void soumisCRIsModifiableWithin7Days() {
        // Given
        CompteRendu cr = createValidCR()
                .withStatut(StatutCR.SOUMIS)
                .withCreatedAt(LocalDateTime.now().minusDays(5));

        // When
        boolean isModifiable = cr.isModifiable();

        // Then
        assertTrue(isModifiable);
    }

    @Test
    void soumisCRIsNotModifiableAfter7Days() {
        // Given
        CompteRendu cr = createValidCR()
                .withStatut(StatutCR.SOUMIS)
                .withCreatedAt(LocalDateTime.now().minusDays(8));

        // When
        boolean isModifiable = cr.isModifiable();

        // Then
        assertFalse(isModifiable);
    }

    @Test
    void valideCRIsNotModifiable() {
        // Given
        CompteRendu cr = createValidCR()
                .withStatut(StatutCR.VALIDE)
                .withCreatedAt(LocalDateTime.now());

        // When
        boolean isModifiable = cr.isModifiable();

        // Then
        assertFalse(isModifiable);
    }

    @Test
    void shouldMarquerCommeVu() {
        // Given
        CompteRendu cr = createValidCR().withStatut(StatutCR.SOUMIS).withVuParFd(false);

        // When
        CompteRendu updated = cr.marquerCommeVu();

        // Then
        assertTrue(updated.getVuParFd());
    }

    @Test
    void shouldThrowExceptionWhenMarquerCommeVuOnNonSoumisCR() {
        // Given
        CompteRendu cr = createValidCR().withStatut(StatutCR.BROUILLON);

        // When & Then
        assertThrows(IllegalStateException.class, () -> cr.marquerCommeVu());
    }

    @Test
    void shouldValiderCR() {
        // Given
        CompteRendu cr = createValidCR().withStatut(StatutCR.SOUMIS);

        // When
        CompteRendu validated = cr.valider();

        // Then
        assertEquals(StatutCR.VALIDE, validated.getStatut());
        assertTrue(validated.getVuParFd());
    }

    @Test
    void shouldThrowExceptionWhenValiderOnNonSoumisCR() {
        // Given
        CompteRendu cr = createValidCR().withStatut(StatutCR.BROUILLON);

        // When & Then
        assertThrows(IllegalStateException.class, () -> cr.valider());
    }

    @Test
    void shouldSoumettreCR() {
        // Given
        CompteRendu cr = createValidCR().withStatut(StatutCR.BROUILLON);

        // When
        CompteRendu soumis = cr.soumettre();

        // Then
        assertEquals(StatutCR.SOUMIS, soumis.getStatut());
    }

    @Test
    void shouldThrowExceptionWhenSoumettreOnNonBrouillonCR() {
        // Given
        CompteRendu cr = createValidCR().withStatut(StatutCR.SOUMIS);

        // When & Then
        assertThrows(IllegalStateException.class, () -> cr.soumettre());
    }

    // Helper method
    private CompteRendu createValidCR() {
        return CompteRendu.builder()
                .id(UUID.randomUUID())
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
