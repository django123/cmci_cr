package com.cmci.cr.domain.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour l'entité Commentaire
 */
class CommentaireTest {

    @Test
    void shouldCreateValidCommentaire() {
        // Given
        UUID id = UUID.randomUUID();
        UUID compteRenduId = UUID.randomUUID();
        UUID auteurId = UUID.randomUUID();
        String contenu = "Excellent CR, continuez ainsi!";

        // When
        Commentaire commentaire = Commentaire.builder()
                .id(id)
                .compteRenduId(compteRenduId)
                .auteurId(auteurId)
                .contenu(contenu)
                .createdAt(LocalDateTime.now())
                .build();

        // Then
        assertNotNull(commentaire);
        assertEquals(id, commentaire.getId());
        assertEquals(compteRenduId, commentaire.getCompteRenduId());
        assertEquals(auteurId, commentaire.getAuteurId());
        assertEquals(contenu, commentaire.getContenu());
    }

    @Test
    void shouldValidateSuccessfully() {
        // Given
        Commentaire commentaire = createValidCommentaire();

        // When & Then
        assertDoesNotThrow(() -> commentaire.validate());
    }

    @Test
    void shouldThrowExceptionWhenCompteRenduIdIsNull() {
        // Given
        Commentaire commentaire = createValidCommentaire().withCompteRenduId(null);

        // When & Then
        assertThrows(IllegalStateException.class, () -> commentaire.validate());
    }

    @Test
    void shouldThrowExceptionWhenAuteurIdIsNull() {
        // Given
        Commentaire commentaire = createValidCommentaire().withAuteurId(null);

        // When & Then
        assertThrows(IllegalStateException.class, () -> commentaire.validate());
    }

    @Test
    void shouldThrowExceptionWhenContenuIsNull() {
        // Given
        Commentaire commentaire = createValidCommentaire().withContenu(null);

        // When & Then
        assertThrows(IllegalStateException.class, () -> commentaire.validate());
    }

    @Test
    void shouldThrowExceptionWhenContenuIsEmpty() {
        // Given
        Commentaire commentaire = createValidCommentaire().withContenu("");

        // When & Then
        assertThrows(IllegalStateException.class, () -> commentaire.validate());
    }

    @Test
    void shouldThrowExceptionWhenContenuIsOnlyWhitespace() {
        // Given
        Commentaire commentaire = createValidCommentaire().withContenu("   ");

        // When & Then
        assertThrows(IllegalStateException.class, () -> commentaire.validate());
    }

    @Test
    void shouldThrowExceptionWhenContenuExceeds5000Characters() {
        // Given
        String longContenu = "a".repeat(5001);
        Commentaire commentaire = createValidCommentaire().withContenu(longContenu);

        // When & Then
        assertThrows(IllegalStateException.class, () -> commentaire.validate());
    }

    @Test
    void shouldAcceptContenuWith5000Characters() {
        // Given
        String maxContenu = "a".repeat(5000);
        Commentaire commentaire = createValidCommentaire().withContenu(maxContenu);

        // When & Then
        assertDoesNotThrow(() -> commentaire.validate());
    }

    @Test
    void shouldDetectAuthor() {
        // Given
        UUID auteurId = UUID.randomUUID();
        Commentaire commentaire = createValidCommentaire().withAuteurId(auteurId);

        // Then
        assertTrue(commentaire.isAuthoredBy(auteurId));
        assertFalse(commentaire.isAuthoredBy(UUID.randomUUID()));
    }

    @Test
    void shouldReturnApercuForShortContent() {
        // Given
        String shortContenu = "Bon travail!";
        Commentaire commentaire = createValidCommentaire().withContenu(shortContenu);

        // When
        String apercu = commentaire.getApercu();

        // Then
        assertEquals(shortContenu, apercu);
    }

    @Test
    void shouldTruncateApercuForLongContent() {
        // Given
        String longContenu = "a".repeat(150);
        Commentaire commentaire = createValidCommentaire().withContenu(longContenu);

        // When
        String apercu = commentaire.getApercu();

        // Then
        assertEquals(100, apercu.length());
        assertTrue(apercu.endsWith("..."));
        assertEquals("a".repeat(97) + "...", apercu);
    }

    @Test
    void apercuShouldBeExactly100CharsForLongContent() {
        // Given
        String longContenu = "a".repeat(200);
        Commentaire commentaire = createValidCommentaire().withContenu(longContenu);

        // When
        String apercu = commentaire.getApercu();

        // Then
        assertEquals(100, apercu.length());
    }

    // Helper method
    private Commentaire createValidCommentaire() {
        return Commentaire.builder()
                .id(UUID.randomUUID())
                .compteRenduId(UUID.randomUUID())
                .auteurId(UUID.randomUUID())
                .contenu("Excellent travail spirituel!")
                .createdAt(LocalDateTime.now())
                .build();
    }
}
