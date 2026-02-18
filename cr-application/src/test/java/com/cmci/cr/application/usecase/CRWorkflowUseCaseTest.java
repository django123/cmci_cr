package com.cmci.cr.application.usecase;

import com.cmci.cr.application.dto.command.CreateCRCommand;
import com.cmci.cr.application.dto.response.CRResponse;
import com.cmci.cr.domain.model.CompteRendu;
import com.cmci.cr.domain.repository.CompteRenduRepository;
import com.cmci.cr.domain.valueobject.RDQD;
import com.cmci.cr.domain.valueobject.StatutCR;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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

@ExtendWith(MockitoExtension.class)
@DisplayName("CR Workflow - Cycle de vie complet des Comptes Rendus")
class CRWorkflowUseCaseTest {

    @Mock
    private CompteRenduRepository compteRenduRepository;

    private CreateCRUseCase createCRUseCase;
    private ValidateCRUseCase validateCRUseCase;

    private UUID utilisateurId;
    private UUID fdId;

    @BeforeEach
    void setUp() {
        createCRUseCase = new CreateCRUseCase(compteRenduRepository);
        validateCRUseCase = new ValidateCRUseCase(compteRenduRepository);
        utilisateurId = UUID.randomUUID();
        fdId = UUID.randomUUID();
    }

    // ============ CRÉATION CR ============

    @Nested
    @DisplayName("Création de Comptes Rendus")
    class CreationCR {

        @Test
        @DisplayName("Doit créer un CR quotidien avec toutes les données spirituelles")
        void shouldCreateFullCR() {
            // Given
            CreateCRCommand command = CreateCRCommand.builder()
                    .utilisateurId(utilisateurId)
                    .date(LocalDate.now())
                    .rdqd("1/1")
                    .priereSeule("01:30")
                    .lectureBiblique(3)
                    .livreBiblique("Matthieu")
                    .litteraturePages(10)
                    .litteratureTotal(200)
                    .litteratureTitre("La Pratique de la Vie Chrétienne")
                    .priereAutres(2)
                    .confession(true)
                    .jeune(true)
                    .typeJeune("Jeûne sec")
                    .evangelisation(1)
                    .offrande(true)
                    .notes("Journée bénie, temps de méditation profonde")
                    .build();

            when(compteRenduRepository.existsByUtilisateurIdAndDate(utilisateurId, LocalDate.now()))
                    .thenReturn(false);
            when(compteRenduRepository.save(any(CompteRendu.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            // When
            CRResponse response = createCRUseCase.execute(command);

            // Then
            assertNotNull(response);
            assertEquals(utilisateurId, response.getUtilisateurId());
            assertEquals(LocalDate.now(), response.getDate());
            assertEquals("1/1", response.getRdqd());
            assertEquals("01:30", response.getPriereSeule());
            assertEquals(3, response.getLectureBiblique());
            assertEquals("Matthieu", response.getLivreBiblique());
            assertEquals(10, response.getLitteraturePages());
            assertEquals("La Pratique de la Vie Chrétienne", response.getLitteratureTitre());
            assertEquals(2, response.getPriereAutres());
            assertTrue(response.getConfession());
            assertTrue(response.getJeune());
            assertEquals("Jeûne sec", response.getTypeJeune());
            assertEquals(1, response.getEvangelisation());
            assertTrue(response.getOffrande());
            assertEquals("SOUMIS", response.getStatut()); // CR créé = auto-soumis
            assertFalse(response.getVuParFd());

            // Vérifier que le CR est bien sauvegardé
            ArgumentCaptor<CompteRendu> captor = ArgumentCaptor.forClass(CompteRendu.class);
            verify(compteRenduRepository).save(captor.capture());
            CompteRendu saved = captor.getValue();
            assertEquals(StatutCR.SOUMIS, saved.getStatut());
        }

        @Test
        @DisplayName("Doit créer un CR minimal (champs obligatoires uniquement)")
        void shouldCreateMinimalCR() {
            // Given
            CreateCRCommand command = CreateCRCommand.builder()
                    .utilisateurId(utilisateurId)
                    .date(LocalDate.now())
                    .rdqd("0/1")
                    .priereSeule("00:15")
                    .lectureBiblique(1)
                    .build();

            when(compteRenduRepository.existsByUtilisateurIdAndDate(any(), any())).thenReturn(false);
            when(compteRenduRepository.save(any(CompteRendu.class))).thenAnswer(inv -> inv.getArgument(0));

            // When
            CRResponse response = createCRUseCase.execute(command);

            // Then
            assertNotNull(response);
            assertEquals("0/1", response.getRdqd());
            assertEquals("00:15", response.getPriereSeule());
            assertEquals(1, response.getLectureBiblique());
            assertFalse(response.getConfession());
            assertFalse(response.getJeune());
            assertEquals(0, response.getEvangelisation());
            assertFalse(response.getOffrande());
        }

        @Test
        @DisplayName("Doit refuser un CR pour une date déjà utilisée")
        void shouldRejectDuplicateDateCR() {
            // Given
            CreateCRCommand command = CreateCRCommand.builder()
                    .utilisateurId(utilisateurId)
                    .date(LocalDate.now())
                    .rdqd("1/1")
                    .priereSeule("01:00")
                    .lectureBiblique(2)
                    .build();

            when(compteRenduRepository.existsByUtilisateurIdAndDate(utilisateurId, LocalDate.now()))
                    .thenReturn(true);

            // When & Then
            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> createCRUseCase.execute(command)
            );
            assertTrue(ex.getMessage().contains("existe déjà"));
        }
    }

    // ============ VALIDATION CR ============

    @Nested
    @DisplayName("Validation de Comptes Rendus (FD/Leader/Pasteur)")
    class ValidationCR {

        @Test
        @DisplayName("Doit valider un CR soumis")
        void shouldValidateSubmittedCR() {
            // Given
            UUID crId = UUID.randomUUID();
            CompteRendu crSoumis = CompteRendu.builder()
                    .id(crId)
                    .utilisateurId(utilisateurId)
                    .date(LocalDate.now())
                    .rdqd(RDQD.fromString("1/1"))
                    .priereSeule(Duration.ofMinutes(90))
                    .lectureBiblique(3)
                    .statut(StatutCR.SOUMIS)
                    .vuParFd(false)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            when(compteRenduRepository.findById(crId)).thenReturn(Optional.of(crSoumis));
            when(compteRenduRepository.save(any(CompteRendu.class))).thenAnswer(inv -> inv.getArgument(0));

            // When
            CRResponse response = validateCRUseCase.execute(crId, fdId);

            // Then
            assertEquals("VALIDE", response.getStatut());
            assertTrue(response.getVuParFd());

            ArgumentCaptor<CompteRendu> captor = ArgumentCaptor.forClass(CompteRendu.class);
            verify(compteRenduRepository).save(captor.capture());
            assertEquals(StatutCR.VALIDE, captor.getValue().getStatut());
        }

        @Test
        @DisplayName("Doit refuser de valider un CR en brouillon")
        void shouldRejectValidatingDraftCR() {
            // Given
            UUID crId = UUID.randomUUID();
            CompteRendu crBrouillon = CompteRendu.builder()
                    .id(crId)
                    .utilisateurId(utilisateurId)
                    .date(LocalDate.now())
                    .rdqd(RDQD.fromString("1/1"))
                    .priereSeule(Duration.ofMinutes(30))
                    .lectureBiblique(1)
                    .statut(StatutCR.BROUILLON)
                    .vuParFd(false)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            when(compteRenduRepository.findById(crId)).thenReturn(Optional.of(crBrouillon));

            // When & Then
            IllegalStateException ex = assertThrows(
                    IllegalStateException.class,
                    () -> validateCRUseCase.execute(crId, fdId)
            );
            assertTrue(ex.getMessage().contains("SOUMIS"));
        }

        @Test
        @DisplayName("Doit refuser de valider un CR déjà validé")
        void shouldRejectValidatingAlreadyValidatedCR() {
            // Given
            UUID crId = UUID.randomUUID();
            CompteRendu crValide = CompteRendu.builder()
                    .id(crId)
                    .utilisateurId(utilisateurId)
                    .date(LocalDate.now())
                    .rdqd(RDQD.fromString("1/1"))
                    .priereSeule(Duration.ofMinutes(60))
                    .lectureBiblique(2)
                    .statut(StatutCR.VALIDE)
                    .vuParFd(true)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            when(compteRenduRepository.findById(crId)).thenReturn(Optional.of(crValide));

            // When & Then
            assertThrows(IllegalStateException.class, () -> validateCRUseCase.execute(crId, fdId));
        }

        @Test
        @DisplayName("Doit échouer si le CR n'existe pas")
        void shouldFailIfCRNotFound() {
            // Given
            UUID unknownCrId = UUID.randomUUID();
            when(compteRenduRepository.findById(unknownCrId)).thenReturn(Optional.empty());

            // When & Then
            assertThrows(IllegalArgumentException.class,
                    () -> validateCRUseCase.execute(unknownCrId, fdId));
        }
    }

    // ============ WORKFLOW COMPLET ============

    @Nested
    @DisplayName("Workflow complet: Créer -> Soumettre -> Valider")
    class WorkflowComplet {

        @Test
        @DisplayName("Doit exécuter le workflow complet du CR")
        void shouldExecuteFullWorkflow() {
            // 1. Créer le CR
            CreateCRCommand createCommand = CreateCRCommand.builder()
                    .utilisateurId(utilisateurId)
                    .date(LocalDate.now().minusDays(1))
                    .rdqd("1/1")
                    .priereSeule("02:00")
                    .lectureBiblique(5)
                    .livreBiblique("Jean")
                    .confession(true)
                    .evangelisation(3)
                    .offrande(true)
                    .notes("Très bonne journée spirituelle")
                    .build();

            when(compteRenduRepository.existsByUtilisateurIdAndDate(any(), any())).thenReturn(false);
            when(compteRenduRepository.save(any(CompteRendu.class))).thenAnswer(inv -> inv.getArgument(0));

            CRResponse created = createCRUseCase.execute(createCommand);
            assertEquals("SOUMIS", created.getStatut()); // Auto-soumis à la création

            // 2. Valider le CR par le FD
            CompteRendu crSoumis = CompteRendu.builder()
                    .id(created.getId())
                    .utilisateurId(utilisateurId)
                    .date(created.getDate())
                    .rdqd(RDQD.fromString("1/1"))
                    .priereSeule(Duration.ofHours(2))
                    .lectureBiblique(5)
                    .statut(StatutCR.SOUMIS)
                    .vuParFd(false)
                    .createdAt(created.getCreatedAt())
                    .updatedAt(created.getUpdatedAt())
                    .build();

            when(compteRenduRepository.findById(created.getId())).thenReturn(Optional.of(crSoumis));

            CRResponse validated = validateCRUseCase.execute(created.getId(), fdId);

            // Then - CR validé
            assertEquals("VALIDE", validated.getStatut());
            assertTrue(validated.getVuParFd());
        }
    }
}
