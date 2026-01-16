package com.cmci.cr.domain.valueobject;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour le Value Object Role
 */
class RoleTest {

    @Test
    void shouldHaveCorrectHierarchyLevels() {
        // Then
        assertEquals(1, Role.FIDELE.getHierarchyLevel());
        assertEquals(2, Role.FD.getHierarchyLevel());
        assertEquals(3, Role.LEADER.getHierarchyLevel());
        assertEquals(4, Role.PASTEUR.getHierarchyLevel());
        assertEquals(5, Role.ADMIN.getHierarchyLevel());
    }

    @Test
    void shouldHaveCorrectDisplayNames() {
        // Then
        assertEquals("Fidèle", Role.FIDELE.getDisplayName());
        assertEquals("Faiseur de Disciples", Role.FD.getDisplayName());
        assertEquals("Leader", Role.LEADER.getDisplayName());
        assertEquals("Pasteur", Role.PASTEUR.getDisplayName());
        assertEquals("Administrateur", Role.ADMIN.getDisplayName());
    }

    @Test
    void fdCanSupervisesFidele() {
        // When
        boolean canSupervise = Role.FD.canSupervise(Role.FIDELE);

        // Then
        assertTrue(canSupervise);
    }

    @Test
    void fideleCannotSuperviseAnyone() {
        // Then
        assertFalse(Role.FIDELE.canSupervise(Role.FIDELE));
        assertFalse(Role.FIDELE.canSupervise(Role.FD));
        assertFalse(Role.FIDELE.canSupervise(Role.LEADER));
    }

    @Test
    void leaderCanSuperviseFdAndFidele() {
        // Then
        assertTrue(Role.LEADER.canSupervise(Role.FD));
        assertTrue(Role.LEADER.canSupervise(Role.FIDELE));
        assertFalse(Role.LEADER.canSupervise(Role.LEADER));
        assertFalse(Role.LEADER.canSupervise(Role.PASTEUR));
    }

    @Test
    void adminCanSuperviseEveryone() {
        // Then
        assertTrue(Role.ADMIN.canSupervise(Role.FIDELE));
        assertTrue(Role.ADMIN.canSupervise(Role.FD));
        assertTrue(Role.ADMIN.canSupervise(Role.LEADER));
        assertTrue(Role.ADMIN.canSupervise(Role.PASTEUR));
        assertFalse(Role.ADMIN.canSupervise(Role.ADMIN)); // Même niveau
    }

    @Test
    void fideleCanOnlyViewOwnCR() {
        // Then
        assertTrue(Role.FIDELE.canViewCROf(Role.FIDELE));
        assertFalse(Role.FIDELE.canViewCROf(Role.FD));
        assertFalse(Role.FIDELE.canViewCROf(Role.LEADER));
    }

    @Test
    void fdCanViewFideleAndOwnCR() {
        // Then
        assertTrue(Role.FD.canViewCROf(Role.FIDELE));
        assertTrue(Role.FD.canViewCROf(Role.FD));
        assertFalse(Role.FD.canViewCROf(Role.LEADER));
    }

    @Test
    void leaderCanViewFdFideleAndOwn() {
        // Then
        assertTrue(Role.LEADER.canViewCROf(Role.FIDELE));
        assertTrue(Role.LEADER.canViewCROf(Role.FD));
        assertTrue(Role.LEADER.canViewCROf(Role.LEADER));
        assertFalse(Role.LEADER.canViewCROf(Role.PASTEUR));
    }

    @Test
    void pasteurCanViewUpToLeaderLevel() {
        // Then
        assertTrue(Role.PASTEUR.canViewCROf(Role.FIDELE));
        assertTrue(Role.PASTEUR.canViewCROf(Role.FD));
        assertTrue(Role.PASTEUR.canViewCROf(Role.LEADER));
        assertTrue(Role.PASTEUR.canViewCROf(Role.PASTEUR));
        assertFalse(Role.PASTEUR.canViewCROf(Role.ADMIN));
    }

    @Test
    void adminCanViewAllCR() {
        // Then
        assertTrue(Role.ADMIN.canViewCROf(Role.FIDELE));
        assertTrue(Role.ADMIN.canViewCROf(Role.FD));
        assertTrue(Role.ADMIN.canViewCROf(Role.LEADER));
        assertTrue(Role.ADMIN.canViewCROf(Role.PASTEUR));
        assertTrue(Role.ADMIN.canViewCROf(Role.ADMIN));
    }
}
