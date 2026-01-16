package com.cmci.cr.domain.model;

import com.cmci.cr.domain.valueobject.Role;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour l'entité Utilisateur
 */
class UtilisateurTest {

    @Test
    void shouldCreateValidUtilisateur() {
        // Given
        UUID id = UUID.randomUUID();
        String email = "john.doe@cmci.org";
        String nom = "Doe";
        String prenom = "John";
        Role role = Role.FIDELE;

        // When
        Utilisateur utilisateur = Utilisateur.builder()
                .id(id)
                .email(email)
                .nom(nom)
                .prenom(prenom)
                .role(role)
                .statut(Utilisateur.StatutUtilisateur.ACTIF)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // Then
        assertNotNull(utilisateur);
        assertEquals(id, utilisateur.getId());
        assertEquals(email, utilisateur.getEmail());
        assertEquals(nom, utilisateur.getNom());
        assertEquals(prenom, utilisateur.getPrenom());
        assertEquals(role, utilisateur.getRole());
    }

    @Test
    void shouldValidateSuccessfully() {
        // Given
        Utilisateur utilisateur = createValidUtilisateur();

        // When & Then
        assertDoesNotThrow(() -> utilisateur.validate());
    }

    @Test
    void shouldThrowExceptionForInvalidEmail() {
        // Given
        Utilisateur utilisateur = createValidUtilisateur().withEmail("invalid-email");

        // When & Then
        assertThrows(IllegalStateException.class, () -> utilisateur.validate());
    }

    @Test
    void shouldThrowExceptionForNullEmail() {
        // Given
        Utilisateur utilisateur = createValidUtilisateur().withEmail(null);

        // When & Then
        assertThrows(IllegalStateException.class, () -> utilisateur.validate());
    }

    @Test
    void shouldThrowExceptionForEmptyNom() {
        // Given
        Utilisateur utilisateur = createValidUtilisateur().withNom("");

        // When & Then
        assertThrows(IllegalStateException.class, () -> utilisateur.validate());
    }

    @Test
    void shouldThrowExceptionForNullNom() {
        // Given
        Utilisateur utilisateur = createValidUtilisateur().withNom(null);

        // When & Then
        assertThrows(IllegalStateException.class, () -> utilisateur.validate());
    }

    @Test
    void shouldThrowExceptionForEmptyPrenom() {
        // Given
        Utilisateur utilisateur = createValidUtilisateur().withPrenom("   ");

        // When & Then
        assertThrows(IllegalStateException.class, () -> utilisateur.validate());
    }

    @Test
    void shouldThrowExceptionForNullPrenom() {
        // Given
        Utilisateur utilisateur = createValidUtilisateur().withPrenom(null);

        // When & Then
        assertThrows(IllegalStateException.class, () -> utilisateur.validate());
    }

    @Test
    void shouldThrowExceptionForNullRole() {
        // Given
        Utilisateur utilisateur = createValidUtilisateur().withRole(null);

        // When & Then
        assertThrows(IllegalStateException.class, () -> utilisateur.validate());
    }

    @Test
    void shouldReturnNomComplet() {
        // Given
        Utilisateur utilisateur = createValidUtilisateur()
                .withPrenom("John")
                .withNom("Doe");

        // When
        String nomComplet = utilisateur.getNomComplet();

        // Then
        assertEquals("John Doe", nomComplet);
    }

    @Test
    void actifUtilisateurIsActif() {
        // Given
        Utilisateur utilisateur = createValidUtilisateur()
                .withStatut(Utilisateur.StatutUtilisateur.ACTIF);

        // When
        boolean isActif = utilisateur.isActif();

        // Then
        assertTrue(isActif);
    }

    @Test
    void inactifUtilisateurIsNotActif() {
        // Given
        Utilisateur utilisateur = createValidUtilisateur()
                .withStatut(Utilisateur.StatutUtilisateur.INACTIF);

        // When
        boolean isActif = utilisateur.isActif();

        // Then
        assertFalse(isActif);
    }

    @Test
    void shouldDetectWhenHasFD() {
        // Given
        Utilisateur utilisateurWithFD = createValidUtilisateur().withFdId(UUID.randomUUID());
        Utilisateur utilisateurWithoutFD = createValidUtilisateur().withFdId(null);

        // Then
        assertTrue(utilisateurWithFD.hasFD());
        assertFalse(utilisateurWithoutFD.hasFD());
    }

    @Test
    void fideleCanOnlyViewOwnCR() {
        // Given
        Utilisateur fidele = createValidUtilisateur().withRole(Role.FIDELE);
        Utilisateur autreFidele = createValidUtilisateur().withRole(Role.FIDELE);

        // Then
        assertTrue(fidele.canViewCROf(fidele)); // Ses propres CR
        assertFalse(fidele.canViewCROf(autreFidele)); // Pas les CR d'un autre
    }

    @Test
    void fdCanViewDisciplesCR() {
        // Given
        UUID fdId = UUID.randomUUID();
        Utilisateur fd = createValidUtilisateur()
                .withId(fdId)
                .withRole(Role.FD);
        Utilisateur disciple = createValidUtilisateur()
                .withRole(Role.FIDELE)
                .withFdId(fdId);

        // Then
        assertTrue(fd.canViewCROf(disciple));
        assertTrue(fd.canViewCROf(fd)); // Ses propres CR
    }

    @Test
    void fdCannotViewNonDisciplesCR() {
        // Given
        UUID fdId = UUID.randomUUID();
        Utilisateur fd = createValidUtilisateur()
                .withId(fdId)
                .withRole(Role.FD);
        Utilisateur autreFidele = createValidUtilisateur()
                .withRole(Role.FIDELE)
                .withFdId(UUID.randomUUID()); // Autre FD

        // Then
        assertFalse(fd.canViewCROf(autreFidele));
    }

    @Test
    void adminCanViewAllCR() {
        // Given
        Utilisateur admin = createValidUtilisateur().withRole(Role.ADMIN);
        Utilisateur fidele = createValidUtilisateur().withRole(Role.FIDELE);
        Utilisateur fd = createValidUtilisateur().withRole(Role.FD);

        // Then
        assertTrue(admin.canViewCROf(fidele));
        assertTrue(admin.canViewCROf(fd));
        assertTrue(admin.canViewCROf(admin));
    }

    @Test
    void fideleCannotCommentAnyCR() {
        // Given
        Utilisateur fidele = createValidUtilisateur().withRole(Role.FIDELE);
        Utilisateur autreFidele = createValidUtilisateur().withRole(Role.FIDELE);

        // Then
        assertFalse(fidele.canCommentCROf(autreFidele));
        assertFalse(fidele.canCommentCROf(fidele));
    }

    @Test
    void fdCanCommentDisciplesCR() {
        // Given
        UUID fdId = UUID.randomUUID();
        Utilisateur fd = createValidUtilisateur()
                .withId(fdId)
                .withRole(Role.FD);
        Utilisateur disciple = createValidUtilisateur()
                .withRole(Role.FIDELE)
                .withFdId(fdId);

        // Then
        assertTrue(fd.canCommentCROf(disciple));
    }

    @Test
    void shouldDetectSameEgliseMaison() {
        // Given
        UUID egliseMaisonId = UUID.randomUUID();
        Utilisateur utilisateur1 = createValidUtilisateur()
                .withEgliseMaisonId(egliseMaisonId)
                .withRole(Role.LEADER);
        Utilisateur utilisateur2 = createValidUtilisateur()
                .withEgliseMaisonId(egliseMaisonId)
                .withRole(Role.FIDELE);
        Utilisateur utilisateur3 = createValidUtilisateur()
                .withEgliseMaisonId(UUID.randomUUID())
                .withRole(Role.FIDELE);

        // Then
        assertTrue(utilisateur1.canViewCROf(utilisateur2)); // Même église
        assertFalse(utilisateur1.canViewCROf(utilisateur3)); // Église différente
    }

    // Helper method
    private Utilisateur createValidUtilisateur() {
        return Utilisateur.builder()
                .id(UUID.randomUUID())
                .email("john.doe@cmci.org")
                .nom("Doe")
                .prenom("John")
                .role(Role.FIDELE)
                .statut(Utilisateur.StatutUtilisateur.ACTIF)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
