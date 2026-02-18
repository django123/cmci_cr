package com.cmci.cr.application.usecase;

import com.cmci.cr.application.dto.command.AssignFDCommand;
import com.cmci.cr.application.dto.response.UtilisateurResponse;
import com.cmci.cr.domain.model.Utilisateur;
import com.cmci.cr.domain.repository.UtilisateurRepository;
import com.cmci.cr.domain.valueobject.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AssignFDUseCase - Assignation de FD aux disciples")
class AssignFDUseCaseTest {

    @Mock
    private UtilisateurRepository utilisateurRepository;

    private AssignFDUseCase useCase;

    private Utilisateur fidele;
    private Utilisateur fd;
    private Utilisateur leader;
    private Utilisateur pasteur;

    @BeforeEach
    void setUp() {
        useCase = new AssignFDUseCase(utilisateurRepository);

        fidele = Utilisateur.builder()
                .id(UUID.randomUUID())
                .email("fidele@cmci.org")
                .nom("Fidele")
                .prenom("Jean")
                .role(Role.FIDELE)
                .statut(Utilisateur.StatutUtilisateur.ACTIF)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        fd = Utilisateur.builder()
                .id(UUID.randomUUID())
                .email("fd@cmci.org")
                .nom("FD")
                .prenom("Pierre")
                .role(Role.FD)
                .statut(Utilisateur.StatutUtilisateur.ACTIF)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        leader = Utilisateur.builder()
                .id(UUID.randomUUID())
                .email("leader@cmci.org")
                .nom("Leader")
                .prenom("André")
                .role(Role.LEADER)
                .statut(Utilisateur.StatutUtilisateur.ACTIF)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        pasteur = Utilisateur.builder()
                .id(UUID.randomUUID())
                .email("pasteur@cmci.org")
                .nom("Pasteur")
                .prenom("David")
                .role(Role.PASTEUR)
                .statut(Utilisateur.StatutUtilisateur.ACTIF)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Nested
    @DisplayName("Assignation réussie")
    class AssignationReussie {

        @Test
        @DisplayName("Doit assigner un FD à un fidèle")
        void shouldAssignFDToFidele() {
            // Given
            AssignFDCommand command = AssignFDCommand.builder()
                    .discipleId(fidele.getId())
                    .fdId(fd.getId())
                    .build();

            when(utilisateurRepository.findById(fidele.getId())).thenReturn(Optional.of(fidele));
            when(utilisateurRepository.findById(fd.getId())).thenReturn(Optional.of(fd));
            when(utilisateurRepository.save(any(Utilisateur.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            UtilisateurResponse response = useCase.execute(command);

            // Then
            assertNotNull(response);
            assertEquals(fd.getId(), response.getFdId());
            assertEquals("Pierre FD", response.getFdNom());
            verify(utilisateurRepository).save(any(Utilisateur.class));
        }

        @Test
        @DisplayName("Doit permettre à un Leader d'être FD")
        void shouldAllowLeaderAsFD() {
            // Given
            AssignFDCommand command = AssignFDCommand.builder()
                    .discipleId(fidele.getId())
                    .fdId(leader.getId())
                    .build();

            when(utilisateurRepository.findById(fidele.getId())).thenReturn(Optional.of(fidele));
            when(utilisateurRepository.findById(leader.getId())).thenReturn(Optional.of(leader));
            when(utilisateurRepository.save(any(Utilisateur.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            UtilisateurResponse response = useCase.execute(command);

            // Then
            assertEquals(leader.getId(), response.getFdId());
        }

        @Test
        @DisplayName("Doit permettre à un Pasteur d'être FD")
        void shouldAllowPasteurAsFD() {
            // Given
            AssignFDCommand command = AssignFDCommand.builder()
                    .discipleId(fidele.getId())
                    .fdId(pasteur.getId())
                    .build();

            when(utilisateurRepository.findById(fidele.getId())).thenReturn(Optional.of(fidele));
            when(utilisateurRepository.findById(pasteur.getId())).thenReturn(Optional.of(pasteur));
            when(utilisateurRepository.save(any(Utilisateur.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            UtilisateurResponse response = useCase.execute(command);

            // Then
            assertEquals(pasteur.getId(), response.getFdId());
        }
    }

    @Nested
    @DisplayName("Désassignation")
    class Desassignation {

        @Test
        @DisplayName("Doit retirer le FD d'un fidèle (fdId = null)")
        void shouldRemoveFDFromFidele() {
            // Given
            Utilisateur fideleAvecFD = fidele.withFdId(fd.getId());

            AssignFDCommand command = AssignFDCommand.builder()
                    .discipleId(fideleAvecFD.getId())
                    .fdId(null) // désassigner
                    .build();

            when(utilisateurRepository.findById(fideleAvecFD.getId())).thenReturn(Optional.of(fideleAvecFD));
            when(utilisateurRepository.save(any(Utilisateur.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            UtilisateurResponse response = useCase.execute(command);

            // Then
            assertNull(response.getFdId());
            assertNull(response.getFdNom());
        }
    }

    @Nested
    @DisplayName("Cas d'erreurs")
    class CasErreurs {

        @Test
        @DisplayName("Doit échouer si le disciple n'existe pas")
        void shouldFailIfDiscipleNotFound() {
            // Given
            UUID unknownId = UUID.randomUUID();
            AssignFDCommand command = AssignFDCommand.builder()
                    .discipleId(unknownId)
                    .fdId(fd.getId())
                    .build();

            when(utilisateurRepository.findById(unknownId)).thenReturn(Optional.empty());

            // When & Then
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> useCase.execute(command)
            );
            assertTrue(exception.getMessage().contains("Disciple non trouvé"));
        }

        @Test
        @DisplayName("Doit échouer si le FD n'existe pas")
        void shouldFailIfFDNotFound() {
            // Given
            UUID unknownFdId = UUID.randomUUID();
            AssignFDCommand command = AssignFDCommand.builder()
                    .discipleId(fidele.getId())
                    .fdId(unknownFdId)
                    .build();

            when(utilisateurRepository.findById(fidele.getId())).thenReturn(Optional.of(fidele));
            when(utilisateurRepository.findById(unknownFdId)).thenReturn(Optional.empty());

            // When & Then
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> useCase.execute(command)
            );
            assertTrue(exception.getMessage().contains("FD non trouvé"));
        }

        @Test
        @DisplayName("Doit échouer si le FD est un simple fidèle")
        void shouldFailIfFDIsFidele() {
            // Given
            Utilisateur autreFidele = Utilisateur.builder()
                    .id(UUID.randomUUID())
                    .email("autre@cmci.org")
                    .nom("Autre")
                    .prenom("Fidele")
                    .role(Role.FIDELE)
                    .statut(Utilisateur.StatutUtilisateur.ACTIF)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            AssignFDCommand command = AssignFDCommand.builder()
                    .discipleId(fidele.getId())
                    .fdId(autreFidele.getId())
                    .build();

            when(utilisateurRepository.findById(fidele.getId())).thenReturn(Optional.of(fidele));
            when(utilisateurRepository.findById(autreFidele.getId())).thenReturn(Optional.of(autreFidele));

            // When & Then
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> useCase.execute(command)
            );
            assertTrue(exception.getMessage().contains("n'a pas le rôle de FD"));
        }
    }
}
