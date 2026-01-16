package com.cmci.cr.domain.valueobject;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour le Value Object StatutCR
 */
class StatutCRTest {

    @Test
    void shouldHaveCorrectDisplayNames() {
        // Then
        assertEquals("Brouillon", StatutCR.BROUILLON.getDisplayName());
        assertEquals("Soumis", StatutCR.SOUMIS.getDisplayName());
        assertEquals("Validé", StatutCR.VALIDE.getDisplayName());
    }

    @Test
    void brouillonIsModifiable() {
        // When
        boolean isModifiable = StatutCR.BROUILLON.isModifiable();

        // Then
        assertTrue(isModifiable);
    }

    @Test
    void soumisIsNotModifiable() {
        // When
        boolean isModifiable = StatutCR.SOUMIS.isModifiable();

        // Then
        assertFalse(isModifiable);
    }

    @Test
    void valideIsNotModifiable() {
        // When
        boolean isModifiable = StatutCR.VALIDE.isModifiable();

        // Then
        assertFalse(isModifiable);
    }

    @Test
    void brouillonCannotBeValidated() {
        // When
        boolean canBeValidated = StatutCR.BROUILLON.canBeValidated();

        // Then
        assertFalse(canBeValidated);
    }

    @Test
    void soumisCanBeValidated() {
        // When
        boolean canBeValidated = StatutCR.SOUMIS.canBeValidated();

        // Then
        assertTrue(canBeValidated);
    }

    @Test
    void valideCannotBeValidated() {
        // When
        boolean canBeValidated = StatutCR.VALIDE.canBeValidated();

        // Then
        assertFalse(canBeValidated);
    }

    @Test
    void shouldHaveThreeStatuses() {
        // When
        StatutCR[] statuts = StatutCR.values();

        // Then
        assertEquals(3, statuts.length);
    }

    @Test
    void shouldBeAbleToGetStatutByName() {
        // When
        StatutCR statut = StatutCR.valueOf("BROUILLON");

        // Then
        assertEquals(StatutCR.BROUILLON, statut);
    }
}
