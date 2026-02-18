package com.cmci.cr.application.usecase;

import com.cmci.cr.application.dto.command.CreateEgliseLocaleCommand;
import com.cmci.cr.application.dto.command.CreateEgliseMaisonCommand;
import com.cmci.cr.application.dto.command.CreateRegionCommand;
import com.cmci.cr.application.dto.command.CreateZoneCommand;
import com.cmci.cr.application.dto.response.EgliseLocaleResponse;
import com.cmci.cr.application.dto.response.EgliseMaisonResponse;
import com.cmci.cr.application.dto.response.RegionResponse;
import com.cmci.cr.application.dto.response.ZoneResponse;
import com.cmci.cr.domain.model.*;
import com.cmci.cr.domain.repository.*;
import com.cmci.cr.domain.valueobject.Role;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Geography Use Cases - CRUD Régions, Zones, Églises Locales, Églises de Maison")
class GeographyUseCasesTest {

    @Mock private RegionRepository regionRepository;
    @Mock private ZoneRepository zoneRepository;
    @Mock private EgliseLocaleRepository egliseLocaleRepository;
    @Mock private EgliseMaisonRepository egliseMaisonRepository;
    @Mock private UtilisateurRepository utilisateurRepository;

    private CreateRegionUseCase createRegionUseCase;
    private CreateZoneUseCase createZoneUseCase;
    private CreateEgliseLocaleUseCase createEgliseLocaleUseCase;
    private CreateEgliseMaisonUseCase createEgliseMaisonUseCase;

    @BeforeEach
    void setUp() {
        createRegionUseCase = new CreateRegionUseCase(regionRepository, zoneRepository);
        createZoneUseCase = new CreateZoneUseCase(zoneRepository, regionRepository, egliseLocaleRepository);
        createEgliseLocaleUseCase = new CreateEgliseLocaleUseCase(
                egliseLocaleRepository, zoneRepository, egliseMaisonRepository, utilisateurRepository);
        createEgliseMaisonUseCase = new CreateEgliseMaisonUseCase(
                egliseMaisonRepository, egliseLocaleRepository, utilisateurRepository);
    }

    // ============ RÉGION TESTS ============

    @Nested
    @DisplayName("CRUD Régions")
    class RegionTests {

        @Test
        @DisplayName("Doit créer une région avec succès")
        void shouldCreateRegion() {
            // Given
            CreateRegionCommand command = CreateRegionCommand.builder()
                    .nom("Afrique Centrale")
                    .code("AF-CENT")
                    .build();

            when(regionRepository.existsByCode("AF-CENT")).thenReturn(false);
            when(regionRepository.save(any(Region.class))).thenAnswer(inv -> inv.getArgument(0));
            when(zoneRepository.countByRegionId(any())).thenReturn(0L);

            // When
            RegionResponse response = createRegionUseCase.execute(command);

            // Then
            assertNotNull(response);
            assertEquals("Afrique Centrale", response.getNom());
            assertEquals("AF-CENT", response.getCode());
            assertEquals(0, response.getNombreZones());
            assertNotNull(response.getId());
            assertNotNull(response.getCreatedAt());
        }

        @Test
        @DisplayName("Doit refuser une région avec code dupliqué")
        void shouldRejectDuplicateRegionCode() {
            // Given
            CreateRegionCommand command = CreateRegionCommand.builder()
                    .nom("Duplicate")
                    .code("EXISTING")
                    .build();

            when(regionRepository.existsByCode("EXISTING")).thenReturn(true);

            // When & Then
            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> createRegionUseCase.execute(command)
            );
            assertTrue(ex.getMessage().contains("existe déjà"));
        }

        @Test
        @DisplayName("Doit refuser une région avec nom vide")
        void shouldRejectEmptyRegionName() {
            // Given
            CreateRegionCommand command = CreateRegionCommand.builder()
                    .nom("")
                    .code("CODE")
                    .build();

            when(regionRepository.existsByCode("CODE")).thenReturn(false);

            // When & Then
            assertThrows(IllegalStateException.class, () -> createRegionUseCase.execute(command));
        }
    }

    // ============ ZONE TESTS ============

    @Nested
    @DisplayName("CRUD Zones")
    class ZoneTests {

        private Region existingRegion;

        @BeforeEach
        void setUp() {
            existingRegion = Region.builder()
                    .id(UUID.randomUUID())
                    .nom("Afrique Centrale")
                    .code("AF-CENT")
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
        }

        @Test
        @DisplayName("Doit créer une zone dans une région existante")
        void shouldCreateZoneInExistingRegion() {
            // Given
            CreateZoneCommand command = CreateZoneCommand.builder()
                    .nom("Cameroun")
                    .regionId(existingRegion.getId())
                    .build();

            when(regionRepository.findById(existingRegion.getId())).thenReturn(Optional.of(existingRegion));
            when(zoneRepository.save(any(Zone.class))).thenAnswer(inv -> inv.getArgument(0));
            when(egliseLocaleRepository.countByZoneId(any())).thenReturn(0L);

            // When
            ZoneResponse response = createZoneUseCase.execute(command);

            // Then
            assertNotNull(response);
            assertEquals("Cameroun", response.getNom());
            assertEquals(existingRegion.getId(), response.getRegionId());
            assertEquals("Afrique Centrale", response.getRegionNom());
            assertEquals(0, response.getNombreEglisesLocales());
        }

        @Test
        @DisplayName("Doit échouer si la région n'existe pas")
        void shouldFailIfRegionNotFound() {
            // Given
            UUID unknownRegionId = UUID.randomUUID();
            CreateZoneCommand command = CreateZoneCommand.builder()
                    .nom("Zone Test")
                    .regionId(unknownRegionId)
                    .build();

            when(regionRepository.findById(unknownRegionId)).thenReturn(Optional.empty());

            // When & Then
            assertThrows(NoSuchElementException.class, () -> createZoneUseCase.execute(command));
        }
    }

    // ============ ÉGLISE LOCALE TESTS ============

    @Nested
    @DisplayName("CRUD Églises Locales")
    class EgliseLocaleTests {

        private Zone existingZone;
        private Utilisateur pasteur;

        @BeforeEach
        void setUp() {
            existingZone = Zone.builder()
                    .id(UUID.randomUUID())
                    .nom("Cameroun")
                    .regionId(UUID.randomUUID())
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

        @Test
        @DisplayName("Doit créer une église locale avec pasteur")
        void shouldCreateEgliseLocaleWithPasteur() {
            // Given
            CreateEgliseLocaleCommand command = CreateEgliseLocaleCommand.builder()
                    .nom("CMCI Douala Centre")
                    .zoneId(existingZone.getId())
                    .adresse("123 Rue de la Liberté, Douala")
                    .pasteurId(pasteur.getId())
                    .build();

            when(zoneRepository.findById(existingZone.getId())).thenReturn(Optional.of(existingZone));
            when(utilisateurRepository.findById(pasteur.getId())).thenReturn(Optional.of(pasteur));
            when(egliseLocaleRepository.save(any(EgliseLocale.class))).thenAnswer(inv -> inv.getArgument(0));
            when(egliseMaisonRepository.countByEgliseLocaleId(any())).thenReturn(0L);

            // When
            EgliseLocaleResponse response = createEgliseLocaleUseCase.execute(command);

            // Then
            assertNotNull(response);
            assertEquals("CMCI Douala Centre", response.getNom());
            assertEquals(existingZone.getId(), response.getZoneId());
            assertEquals("Cameroun", response.getZoneNom());
            assertEquals(pasteur.getId(), response.getPasteurId());
            assertEquals("David Pasteur", response.getPasteurNom());
            assertEquals("123 Rue de la Liberté, Douala", response.getAdresse());
        }

        @Test
        @DisplayName("Doit créer une église locale sans pasteur")
        void shouldCreateEgliseLocaleSansPasteur() {
            // Given
            CreateEgliseLocaleCommand command = CreateEgliseLocaleCommand.builder()
                    .nom("CMCI Yaoundé Nord")
                    .zoneId(existingZone.getId())
                    .adresse("Quartier Melen, Yaoundé")
                    .build();

            when(zoneRepository.findById(existingZone.getId())).thenReturn(Optional.of(existingZone));
            when(egliseLocaleRepository.save(any(EgliseLocale.class))).thenAnswer(inv -> inv.getArgument(0));
            when(egliseMaisonRepository.countByEgliseLocaleId(any())).thenReturn(0L);

            // When
            EgliseLocaleResponse response = createEgliseLocaleUseCase.execute(command);

            // Then
            assertNotNull(response);
            assertNull(response.getPasteurId());
            assertNull(response.getPasteurNom());
        }

        @Test
        @DisplayName("Doit échouer si la zone n'existe pas")
        void shouldFailIfZoneNotFound() {
            // Given
            UUID unknownZoneId = UUID.randomUUID();
            CreateEgliseLocaleCommand command = CreateEgliseLocaleCommand.builder()
                    .nom("Test")
                    .zoneId(unknownZoneId)
                    .build();

            when(zoneRepository.findById(unknownZoneId)).thenReturn(Optional.empty());

            // When & Then
            assertThrows(NoSuchElementException.class, () -> createEgliseLocaleUseCase.execute(command));
        }
    }

    // ============ ÉGLISE DE MAISON TESTS ============

    @Nested
    @DisplayName("CRUD Églises de Maison")
    class EgliseMaisonTests {

        private EgliseLocale existingEgliseLocale;
        private Utilisateur leader;

        @BeforeEach
        void setUp() {
            existingEgliseLocale = EgliseLocale.builder()
                    .id(UUID.randomUUID())
                    .nom("CMCI Douala Centre")
                    .zoneId(UUID.randomUUID())
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            leader = Utilisateur.builder()
                    .id(UUID.randomUUID())
                    .email("leader@cmci.org")
                    .nom("LeaderNom")
                    .prenom("LeaderPrenom")
                    .role(Role.LEADER)
                    .statut(Utilisateur.StatutUtilisateur.ACTIF)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
        }

        @Test
        @DisplayName("Doit créer une église de maison avec leader")
        void shouldCreateEgliseMaisonWithLeader() {
            // Given
            CreateEgliseMaisonCommand command = CreateEgliseMaisonCommand.builder()
                    .nom("EM Bonamoussadi")
                    .egliseLocaleId(existingEgliseLocale.getId())
                    .leaderId(leader.getId())
                    .adresse("Bonamoussadi, Douala")
                    .build();

            when(egliseLocaleRepository.findById(existingEgliseLocale.getId()))
                    .thenReturn(Optional.of(existingEgliseLocale));
            when(utilisateurRepository.findById(leader.getId())).thenReturn(Optional.of(leader));
            when(egliseMaisonRepository.save(any(EgliseMaison.class))).thenAnswer(inv -> inv.getArgument(0));
            when(utilisateurRepository.findByEgliseMaisonId(any())).thenReturn(Collections.emptyList());

            // When
            EgliseMaisonResponse response = createEgliseMaisonUseCase.execute(command);

            // Then
            assertNotNull(response);
            assertEquals("EM Bonamoussadi", response.getNom());
            assertEquals(existingEgliseLocale.getId(), response.getEgliseLocaleId());
            assertEquals("CMCI Douala Centre", response.getEgliseLocaleNom());
            assertEquals(leader.getId(), response.getLeaderId());
            assertEquals("LeaderPrenom LeaderNom", response.getLeaderNom());
            assertEquals(0, response.getNombreFideles());
        }

        @Test
        @DisplayName("Doit créer une église de maison sans leader")
        void shouldCreateEgliseMaisonSansLeader() {
            // Given
            CreateEgliseMaisonCommand command = CreateEgliseMaisonCommand.builder()
                    .nom("EM Akwa")
                    .egliseLocaleId(existingEgliseLocale.getId())
                    .adresse("Akwa, Douala")
                    .build();

            when(egliseLocaleRepository.findById(existingEgliseLocale.getId()))
                    .thenReturn(Optional.of(existingEgliseLocale));
            when(egliseMaisonRepository.save(any(EgliseMaison.class))).thenAnswer(inv -> inv.getArgument(0));
            when(utilisateurRepository.findByEgliseMaisonId(any())).thenReturn(Collections.emptyList());

            // When
            EgliseMaisonResponse response = createEgliseMaisonUseCase.execute(command);

            // Then
            assertNotNull(response);
            assertNull(response.getLeaderId());
            assertNull(response.getLeaderNom());
        }

        @Test
        @DisplayName("Doit échouer si l'église locale n'existe pas")
        void shouldFailIfEgliseLocaleNotFound() {
            // Given
            UUID unknownId = UUID.randomUUID();
            CreateEgliseMaisonCommand command = CreateEgliseMaisonCommand.builder()
                    .nom("Test")
                    .egliseLocaleId(unknownId)
                    .build();

            when(egliseLocaleRepository.findById(unknownId)).thenReturn(Optional.empty());

            // When & Then
            assertThrows(NoSuchElementException.class, () -> createEgliseMaisonUseCase.execute(command));
        }
    }
}
