package com.cmci.cr.domain.valueobject;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour le Value Object RDQD
 */
class RDQDTest {

    @Test
    void shouldCreateValidRDQDFromString() {
        // Given
        String rdqdString = "1/1";

        // When
        RDQD rdqd = RDQD.fromString(rdqdString);

        // Then
        assertNotNull(rdqd);
        assertEquals(1, rdqd.getAccompli());
        assertEquals(1, rdqd.getAttendu());
        assertEquals("1/1", rdqd.toString());
    }

    @Test
    void shouldCreateRDQDDirectly() {
        // When
        RDQD rdqd = RDQD.of(1, 1);

        // Then
        assertNotNull(rdqd);
        assertEquals(1, rdqd.getAccompli());
        assertEquals(1, rdqd.getAttendu());
    }

    @Test
    void shouldDetectCompleteRDQD() {
        // Given
        RDQD complete = RDQD.of(1, 1);
        RDQD incomplete = RDQD.of(0, 1);

        // Then
        assertTrue(complete.isComplete());
        assertFalse(incomplete.isComplete());
    }

    @Test
    void shouldCalculateCompletionPercentage() {
        // Given
        RDQD rdqd1 = RDQD.of(1, 1);
        RDQD rdqd2 = RDQD.of(0, 1);

        // Then
        assertEquals(100.0, rdqd1.getCompletionPercentage(), 0.01);
        assertEquals(0.0, rdqd2.getCompletionPercentage(), 0.01);
    }

    @Test
    void shouldThrowExceptionForInvalidFormat() {
        // Given
        String invalidFormat = "invalid";

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> RDQD.fromString(invalidFormat));
    }

    @Test
    void shouldThrowExceptionForNullInput() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> RDQD.fromString(null));
    }

    @Test
    void shouldThrowExceptionForEmptyInput() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> RDQD.fromString(""));
    }

    @Test
    void shouldThrowExceptionForNegativeAccompli() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> RDQD.of(-1, 1));
    }

    @Test
    void shouldThrowExceptionForZeroAttendu() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> RDQD.of(1, 0));
    }

    @Test
    void shouldThrowExceptionForNegativeAttendu() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> RDQD.of(1, -1));
    }

    @Test
    void shouldThrowExceptionWhenAccompliExceedsAttendu() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> RDQD.of(2, 1));
    }

    @Test
    void shouldHandlePartialCompletion() {
        // Given
        RDQD rdqd = RDQD.fromString("1/2");

        // Then
        assertEquals(1, rdqd.getAccompli());
        assertEquals(2, rdqd.getAttendu());
        assertEquals(50.0, rdqd.getCompletionPercentage(), 0.01);
        assertFalse(rdqd.isComplete());
    }

    @Test
    void shouldTrimWhitespaceInString() {
        // Given
        String rdqdWithSpaces = "  1/1  ";

        // When
        RDQD rdqd = RDQD.fromString(rdqdWithSpaces);

        // Then
        assertEquals(1, rdqd.getAccompli());
        assertEquals(1, rdqd.getAttendu());
    }
}
