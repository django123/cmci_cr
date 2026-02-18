package com.cmci.cr.application.usecase;

import com.cmci.cr.application.dto.command.CreateUtilisateurCommand;
import com.cmci.cr.application.dto.response.UtilisateurResponse;
import com.cmci.cr.domain.model.Utilisateur;
import com.cmci.cr.domain.repository.UtilisateurRepository;
import com.cmci.cr.domain.valueobject.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreateUtilisateurUseCase - Création d'utilisateurs avec différents rôles")
class CreateUtilisateurUseCaseTest {

    @Mock
    private UtilisateurRepository utilisateurRepository;

    private CreateUtilisateurUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new CreateUtilisateurUseCase(utilisateurRepository);
    }

    @Nested
    @DisplayName("Création de fidèles")
    class CreationFidele {

        @Test
        @DisplayName("Doit créer un fidèle avec succès")
        void shouldCreateFidele() {
            // Given
            CreateUtilisateurCommand command = CreateUtilisateurCommand.builder()
                    .email("jean.dupont@cmci.org")
                    .nom("Dupont")
                    .prenom("Jean")
                    .role("FIDELE")
                    .telephone("+237600000001")
                    .dateNaissance(LocalDate.of(1990, 5, 15))
                    .dateBapteme(LocalDate.of(2015, 3, 20))
                    .build();

            when(utilisateurRepository.existsByEmail("jean.dupont@cmci.org")).thenReturn(false);
            when(utilisateurRepository.save(any(Utilisateur.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            UtilisateurResponse response = useCase.execute(command);

            // Then
            assertNotNull(response);
            assertEquals("Dupont", response.getNom());
            assertEquals("Jean", response.getPrenom());
            assertEquals("Jean Dupont", response.getNomComplet());
            assertEquals("FIDELE", response.getRole());
            assertEquals("ACTIF", response.getStatut());
            assertNotNull(response.getId());
            assertNotNull(response.getCreatedAt());

            ArgumentCaptor<Utilisateur> captor = ArgumentCaptor.forClass(Utilisateur.class);
            verify(utilisateurRepository).save(captor.capture());
            Utilisateur saved = captor.getValue();
            assertEquals(Role.FIDELE, saved.getRole());
        }

        @Test
        @DisplayName("Doit refuser un email dupliqué")
        void shouldRejectDuplicateEmail() {
            // Given
            CreateUtilisateurCommand command = CreateUtilisateurCommand.builder()
                    .email("existant@cmci.org")
                    .nom("Dupont")
                    .prenom("Jean")
                    .role("FIDELE")
                    .build();

            when(utilisateurRepository.existsByEmail("existant@cmci.org")).thenReturn(true);

            // When & Then
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> useCase.execute(command)
            );
            assertTrue(exception.getMessage().contains("existe déjà"));
            verify(utilisateurRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Création de FD (Faiseur de Disciples)")
    class CreationFD {

        @Test
        @DisplayName("Doit créer un FD avec succès")
        void shouldCreateFD() {
            // Given
            CreateUtilisateurCommand command = CreateUtilisateurCommand.builder()
                    .email("fd.martin@cmci.org")
                    .nom("Martin")
                    .prenom("Pierre")
                    .role("FD")
                    .telephone("+237600000002")
                    .build();

            when(utilisateurRepository.existsByEmail(any())).thenReturn(false);
            when(utilisateurRepository.save(any(Utilisateur.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            UtilisateurResponse response = useCase.execute(command);

            // Then
            assertEquals("FD", response.getRole());
            assertEquals("Pierre Martin", response.getNomComplet());
        }
    }

    @Nested
    @DisplayName("Création de Leaders")
    class CreationLeader {

        @Test
        @DisplayName("Doit créer un Leader avec église de maison")
        void shouldCreateLeaderWithEgliseMaison() {
            // Given
            UUID egliseMaisonId = UUID.randomUUID();
            CreateUtilisateurCommand command = CreateUtilisateurCommand.builder()
                    .email("leader.paul@cmci.org")
                    .nom("Paul")
                    .prenom("André")
                    .role("LEADER")
                    .egliseMaisonId(egliseMaisonId)
                    .build();

            when(utilisateurRepository.existsByEmail(any())).thenReturn(false);
            when(utilisateurRepository.save(any(Utilisateur.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            UtilisateurResponse response = useCase.execute(command);

            // Then
            assertEquals("LEADER", response.getRole());
            assertEquals(egliseMaisonId, response.getEgliseMaisonId());
        }
    }

    @Nested
    @DisplayName("Création de Pasteurs")
    class CreationPasteur {

        @Test
        @DisplayName("Doit créer un Pasteur avec succès")
        void shouldCreatePasteur() {
            // Given
            CreateUtilisateurCommand command = CreateUtilisateurCommand.builder()
                    .email("pasteur.samuel@cmci.org")
                    .nom("Samuel")
                    .prenom("David")
                    .role("PASTEUR")
                    .build();

            when(utilisateurRepository.existsByEmail(any())).thenReturn(false);
            when(utilisateurRepository.save(any(Utilisateur.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            UtilisateurResponse response = useCase.execute(command);

            // Then
            assertEquals("PASTEUR", response.getRole());
            assertEquals("David Samuel", response.getNomComplet());
        }
    }

    @Nested
    @DisplayName("Création d'Administrateurs")
    class CreationAdmin {

        @Test
        @DisplayName("Doit créer un Admin avec succès")
        void shouldCreateAdmin() {
            // Given
            CreateUtilisateurCommand command = CreateUtilisateurCommand.builder()
                    .email("admin@cmci.org")
                    .nom("Admin")
                    .prenom("Super")
                    .role("ADMIN")
                    .build();

            when(utilisateurRepository.existsByEmail(any())).thenReturn(false);
            when(utilisateurRepository.save(any(Utilisateur.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            UtilisateurResponse response = useCase.execute(command);

            // Then
            assertEquals("ADMIN", response.getRole());
        }
    }

    @Nested
    @DisplayName("Validations")
    class Validations {

        @Test
        @DisplayName("Doit refuser un email invalide")
        void shouldRejectInvalidEmail() {
            // Given
            CreateUtilisateurCommand command = CreateUtilisateurCommand.builder()
                    .email("invalid-email")
                    .nom("Dupont")
                    .prenom("Jean")
                    .role("FIDELE")
                    .build();

            when(utilisateurRepository.existsByEmail(any())).thenReturn(false);

            // When & Then
            assertThrows(IllegalStateException.class, () -> useCase.execute(command));
        }

        @Test
        @DisplayName("Doit refuser un nom vide")
        void shouldRejectEmptyNom() {
            // Given
            CreateUtilisateurCommand command = CreateUtilisateurCommand.builder()
                    .email("test@cmci.org")
                    .nom("")
                    .prenom("Jean")
                    .role("FIDELE")
                    .build();

            when(utilisateurRepository.existsByEmail(any())).thenReturn(false);

            // When & Then
            assertThrows(IllegalStateException.class, () -> useCase.execute(command));
        }
    }
}
