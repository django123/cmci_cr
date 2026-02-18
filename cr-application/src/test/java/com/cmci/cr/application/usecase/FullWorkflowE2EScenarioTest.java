package com.cmci.cr.application.usecase;

import com.cmci.cr.application.dto.command.*;
import com.cmci.cr.application.dto.response.*;
import com.cmci.cr.domain.model.*;
import com.cmci.cr.domain.repository.*;
import com.cmci.cr.domain.valueobject.Role;
import com.cmci.cr.domain.valueobject.RDQD;
import com.cmci.cr.domain.valueobject.StatutCR;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test E2E complet simulant le workflow A-Z de l'application CMCI CR.
 *
 * Scénario complet :
 * 1. Création de la structure géographique (Région -> Zone -> Église Locale -> Église de Maison)
 * 2. Création des utilisateurs avec différents rôles (Pasteur, Leader, FD, Fidèles)
 * 3. Assignation des FD aux fidèles
 * 4. Création de Comptes Rendus quotidiens par les fidèles
 * 5. Validation des CR par les FD
 * 6. Vérification des statistiques et des liens hiérarchiques
 *
 * Ce test utilise des repositories en mémoire pour simuler la persistance BD.
 */
@DisplayName("Scénario E2E Complet - Workflow CMCI CR de A à Z")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ExtendWith(MockitoExtension.class)
class FullWorkflowE2EScenarioTest {

    // ===== In-memory repositories =====
    private static final InMemoryUtilisateurRepository utilisateurRepo = new InMemoryUtilisateurRepository();
    private static final InMemoryCompteRenduRepository crRepo = new InMemoryCompteRenduRepository();
    private static final InMemoryRegionRepository regionRepo = new InMemoryRegionRepository();
    private static final InMemoryZoneRepository zoneRepo = new InMemoryZoneRepository();
    private static final InMemoryEgliseLocaleRepository egliseLocaleRepo = new InMemoryEgliseLocaleRepository();
    private static final InMemoryEgliseMaisonRepository egliseMaisonRepo = new InMemoryEgliseMaisonRepository();
    private static final InMemoryCommentaireRepository commentaireRepo = new InMemoryCommentaireRepository();

    // ===== Use Cases =====
    private static CreateRegionUseCase createRegionUseCase;
    private static CreateZoneUseCase createZoneUseCase;
    private static CreateEgliseLocaleUseCase createEgliseLocaleUseCase;
    private static CreateEgliseMaisonUseCase createEgliseMaisonUseCase;
    private static CreateUtilisateurUseCase createUtilisateurUseCase;
    private static AssignFDUseCase assignFDUseCase;
    private static CreateCRUseCase createCRUseCase;
    private static ValidateCRUseCase validateCRUseCase;
    private static AddCommentaireUseCase addCommentaireUseCase;
    private static GetCommentairesUseCase getCommentairesUseCase;

    // ===== Data references =====
    private static RegionResponse region;
    private static ZoneResponse zone;
    private static EgliseLocaleResponse egliseLocale;
    private static EgliseMaisonResponse egliseMaison;
    private static UtilisateurResponse pasteur;
    private static UtilisateurResponse leader;
    private static UtilisateurResponse fd1;
    private static UtilisateurResponse fd2;
    private static UtilisateurResponse fidele1;
    private static UtilisateurResponse fidele2;
    private static UtilisateurResponse fidele3;
    private static CRResponse crFidele1;
    private static CRResponse crFidele2;
    private static CRResponse crFidele3;

    @BeforeAll
    static void initUseCases() {
        createRegionUseCase = new CreateRegionUseCase(regionRepo, zoneRepo);
        createZoneUseCase = new CreateZoneUseCase(zoneRepo, regionRepo, egliseLocaleRepo);
        createEgliseLocaleUseCase = new CreateEgliseLocaleUseCase(
                egliseLocaleRepo, zoneRepo, egliseMaisonRepo, utilisateurRepo);
        createEgliseMaisonUseCase = new CreateEgliseMaisonUseCase(
                egliseMaisonRepo, egliseLocaleRepo, utilisateurRepo);
        createUtilisateurUseCase = new CreateUtilisateurUseCase(utilisateurRepo);
        assignFDUseCase = new AssignFDUseCase(utilisateurRepo);
        createCRUseCase = new CreateCRUseCase(crRepo);
        validateCRUseCase = new ValidateCRUseCase(crRepo);
        addCommentaireUseCase = new AddCommentaireUseCase(commentaireRepo, crRepo);
        getCommentairesUseCase = new GetCommentairesUseCase(commentaireRepo);
    }

    // ==========================================
    // ÉTAPE 1: GÉOGRAPHIE
    // ==========================================

    @Test
    @Order(1)
    @DisplayName("1.1 - Créer une Région : Afrique Centrale")
    void step1_createRegion() {
        region = createRegionUseCase.execute(
                CreateRegionCommand.builder()
                        .nom("Afrique Centrale")
                        .code("AF-CENT")
                        .build()
        );

        assertNotNull(region.getId());
        assertEquals("Afrique Centrale", region.getNom());
        assertEquals("AF-CENT", region.getCode());
        System.out.println("  Region créée: " + region.getNom() + " [" + region.getId() + "]");
    }

    @Test
    @Order(2)
    @DisplayName("1.2 - Créer une Zone : Cameroun dans Afrique Centrale")
    void step2_createZone() {
        zone = createZoneUseCase.execute(
                CreateZoneCommand.builder()
                        .nom("Cameroun")
                        .regionId(region.getId())
                        .build()
        );

        assertNotNull(zone.getId());
        assertEquals("Cameroun", zone.getNom());
        assertEquals(region.getId(), zone.getRegionId());
        assertEquals("Afrique Centrale", zone.getRegionNom());
        System.out.println("  Zone créée: " + zone.getNom() + " -> " + zone.getRegionNom());
    }

    @Test
    @Order(3)
    @DisplayName("1.3 - Vérifier qu'une zone ne peut pas être créée dans une région inexistante")
    void step3_createZoneInvalidRegion() {
        assertThrows(NoSuchElementException.class, () ->
                createZoneUseCase.execute(CreateZoneCommand.builder()
                        .nom("Invalid")
                        .regionId(UUID.randomUUID())
                        .build())
        );
    }

    // ==========================================
    // ÉTAPE 2: UTILISATEURS (PASTEUR & LEADER D'ABORD)
    // ==========================================

    @Test
    @Order(4)
    @DisplayName("2.1 - Créer un Pasteur")
    void step4_createPasteur() {
        pasteur = createUtilisateurUseCase.execute(
                CreateUtilisateurCommand.builder()
                        .email("pasteur.david@cmci.org")
                        .nom("Kamga")
                        .prenom("David")
                        .role("PASTEUR")
                        .telephone("+237690000001")
                        .dateBapteme(LocalDate.of(2000, 6, 15))
                        .build()
        );

        assertEquals("PASTEUR", pasteur.getRole());
        assertEquals("David Kamga", pasteur.getNomComplet());
        System.out.println("  Pasteur créé: " + pasteur.getNomComplet());
    }

    @Test
    @Order(5)
    @DisplayName("2.2 - Créer un Leader")
    void step5_createLeader() {
        leader = createUtilisateurUseCase.execute(
                CreateUtilisateurCommand.builder()
                        .email("leader.andre@cmci.org")
                        .nom("Nkwenkam")
                        .prenom("André")
                        .role("LEADER")
                        .telephone("+237690000002")
                        .dateBapteme(LocalDate.of(2005, 3, 20))
                        .build()
        );

        assertEquals("LEADER", leader.getRole());
        System.out.println("  Leader créé: " + leader.getNomComplet());
    }

    // ==========================================
    // ÉTAPE 3: ÉGLISE LOCALE & ÉGLISE DE MAISON
    // ==========================================

    @Test
    @Order(6)
    @DisplayName("3.1 - Créer une Église Locale avec Pasteur")
    void step6_createEgliseLocale() {
        egliseLocale = createEgliseLocaleUseCase.execute(
                CreateEgliseLocaleCommand.builder()
                        .nom("CMCI Douala Centre")
                        .zoneId(zone.getId())
                        .adresse("123 Rue de la Liberté, Douala")
                        .pasteurId(pasteur.getId())
                        .build()
        );

        assertNotNull(egliseLocale.getId());
        assertEquals("CMCI Douala Centre", egliseLocale.getNom());
        assertEquals(pasteur.getId(), egliseLocale.getPasteurId());
        assertEquals("David Kamga", egliseLocale.getPasteurNom());
        System.out.println("  Église Locale: " + egliseLocale.getNom() + " (Pasteur: " + egliseLocale.getPasteurNom() + ")");
    }

    @Test
    @Order(7)
    @DisplayName("3.2 - Créer une Église de Maison avec Leader")
    void step7_createEgliseMaison() {
        egliseMaison = createEgliseMaisonUseCase.execute(
                CreateEgliseMaisonCommand.builder()
                        .nom("EM Bonamoussadi")
                        .egliseLocaleId(egliseLocale.getId())
                        .leaderId(leader.getId())
                        .adresse("Bonamoussadi, Douala")
                        .build()
        );

        assertNotNull(egliseMaison.getId());
        assertEquals("EM Bonamoussadi", egliseMaison.getNom());
        assertEquals(leader.getId(), egliseMaison.getLeaderId());
        assertEquals("André Nkwenkam", egliseMaison.getLeaderNom());
        System.out.println("  Église Maison: " + egliseMaison.getNom() + " (Leader: " + egliseMaison.getLeaderNom() + ")");
    }

    // ==========================================
    // ÉTAPE 4: FD (FAISEURS DE DISCIPLES)
    // ==========================================

    @Test
    @Order(8)
    @DisplayName("4.1 - Créer deux FD")
    void step8_createFDs() {
        fd1 = createUtilisateurUseCase.execute(
                CreateUtilisateurCommand.builder()
                        .email("fd.pierre@cmci.org")
                        .nom("Ngounou")
                        .prenom("Pierre")
                        .role("FD")
                        .egliseMaisonId(egliseMaison.getId())
                        .telephone("+237690000003")
                        .build()
        );

        fd2 = createUtilisateurUseCase.execute(
                CreateUtilisateurCommand.builder()
                        .email("fd.marie@cmci.org")
                        .nom("Tchinda")
                        .prenom("Marie")
                        .role("FD")
                        .egliseMaisonId(egliseMaison.getId())
                        .telephone("+237690000004")
                        .build()
        );

        assertEquals("FD", fd1.getRole());
        assertEquals("FD", fd2.getRole());
        System.out.println("  FD 1: " + fd1.getNomComplet());
        System.out.println("  FD 2: " + fd2.getNomComplet());
    }

    // ==========================================
    // ÉTAPE 5: FIDÈLES
    // ==========================================

    @Test
    @Order(9)
    @DisplayName("5.1 - Créer trois Fidèles")
    void step9_createFideles() {
        fidele1 = createUtilisateurUseCase.execute(
                CreateUtilisateurCommand.builder()
                        .email("jean.fidele@cmci.org")
                        .nom("Mbarga")
                        .prenom("Jean")
                        .role("FIDELE")
                        .egliseMaisonId(egliseMaison.getId())
                        .telephone("+237690000005")
                        .dateNaissance(LocalDate.of(1995, 8, 10))
                        .dateBapteme(LocalDate.of(2020, 12, 25))
                        .build()
        );

        fidele2 = createUtilisateurUseCase.execute(
                CreateUtilisateurCommand.builder()
                        .email("sarah.fidele@cmci.org")
                        .nom("Fotso")
                        .prenom("Sarah")
                        .role("FIDELE")
                        .egliseMaisonId(egliseMaison.getId())
                        .telephone("+237690000006")
                        .build()
        );

        fidele3 = createUtilisateurUseCase.execute(
                CreateUtilisateurCommand.builder()
                        .email("paul.fidele@cmci.org")
                        .nom("Tagne")
                        .prenom("Paul")
                        .role("FIDELE")
                        .egliseMaisonId(egliseMaison.getId())
                        .telephone("+237690000007")
                        .build()
        );

        assertEquals("FIDELE", fidele1.getRole());
        assertEquals("FIDELE", fidele2.getRole());
        assertEquals("FIDELE", fidele3.getRole());
        System.out.println("  Fidèle 1: " + fidele1.getNomComplet());
        System.out.println("  Fidèle 2: " + fidele2.getNomComplet());
        System.out.println("  Fidèle 3: " + fidele3.getNomComplet());
    }

    @Test
    @Order(10)
    @DisplayName("5.2 - Vérifier qu'on ne peut pas créer un fidèle avec email dupliqué")
    void step10_rejectDuplicateEmail() {
        assertThrows(IllegalArgumentException.class, () ->
                createUtilisateurUseCase.execute(
                        CreateUtilisateurCommand.builder()
                                .email("jean.fidele@cmci.org") // Email déjà utilisé
                                .nom("Autre")
                                .prenom("Nom")
                                .role("FIDELE")
                                .build()
                )
        );
    }

    // ==========================================
    // ÉTAPE 6: ASSIGNATION FD -> FIDÈLES
    // ==========================================

    @Test
    @Order(11)
    @DisplayName("6.1 - Assigner FD1 à Fidèle1 et Fidèle2")
    void step11_assignFD1() {
        UtilisateurResponse updated1 = assignFDUseCase.execute(
                AssignFDCommand.builder()
                        .discipleId(fidele1.getId())
                        .fdId(fd1.getId())
                        .build()
        );
        assertEquals(fd1.getId(), updated1.getFdId());
        assertEquals("Pierre Ngounou", updated1.getFdNom());

        UtilisateurResponse updated2 = assignFDUseCase.execute(
                AssignFDCommand.builder()
                        .discipleId(fidele2.getId())
                        .fdId(fd1.getId())
                        .build()
        );
        assertEquals(fd1.getId(), updated2.getFdId());

        System.out.println("  " + fidele1.getNomComplet() + " -> FD: " + fd1.getNomComplet());
        System.out.println("  " + fidele2.getNomComplet() + " -> FD: " + fd1.getNomComplet());
    }

    @Test
    @Order(12)
    @DisplayName("6.2 - Assigner FD2 à Fidèle3")
    void step12_assignFD2() {
        UtilisateurResponse updated = assignFDUseCase.execute(
                AssignFDCommand.builder()
                        .discipleId(fidele3.getId())
                        .fdId(fd2.getId())
                        .build()
        );
        assertEquals(fd2.getId(), updated.getFdId());
        System.out.println("  " + fidele3.getNomComplet() + " -> FD: " + fd2.getNomComplet());
    }

    @Test
    @Order(13)
    @DisplayName("6.3 - Vérifier le nombre de disciples par FD")
    void step13_verifyDiscipleCount() {
        long countFD1 = utilisateurRepo.countByFdId(fd1.getId());
        long countFD2 = utilisateurRepo.countByFdId(fd2.getId());

        assertEquals(2, countFD1, "FD1 devrait avoir 2 disciples");
        assertEquals(1, countFD2, "FD2 devrait avoir 1 disciple");
        System.out.println("  FD1 (" + fd1.getNomComplet() + ") : " + countFD1 + " disciples");
        System.out.println("  FD2 (" + fd2.getNomComplet() + ") : " + countFD2 + " disciples");
    }

    @Test
    @Order(14)
    @DisplayName("6.4 - Vérifier que l'assignation échoue si le FD est un fidèle")
    void step14_rejectFideleAsFD() {
        assertThrows(IllegalArgumentException.class, () ->
                assignFDUseCase.execute(
                        AssignFDCommand.builder()
                                .discipleId(fidele1.getId())
                                .fdId(fidele2.getId()) // Un fidèle ne peut pas être FD
                                .build()
                )
        );
    }

    // ==========================================
    // ÉTAPE 7: COMPTES RENDUS
    // ==========================================

    @Test
    @Order(15)
    @DisplayName("7.1 - Fidèle1 crée un CR quotidien complet")
    void step15_fidele1CreatesCR() {
        crFidele1 = createCRUseCase.execute(
                CreateCRCommand.builder()
                        .utilisateurId(fidele1.getId())
                        .date(LocalDate.now().minusDays(1))
                        .rdqd("1/1")
                        .priereSeule("01:30")
                        .lectureBiblique(3)
                        .livreBiblique("Matthieu 5-7")
                        .litteraturePages(15)
                        .litteratureTotal(200)
                        .litteratureTitre("Le Sentier de la Vie")
                        .priereAutres(2)
                        .confession(true)
                        .jeune(false)
                        .evangelisation(1)
                        .offrande(true)
                        .notes("Belle journée, méditation sur le sermon de la montagne")
                        .build()
        );

        assertNotNull(crFidele1.getId());
        assertEquals("SOUMIS", crFidele1.getStatut());
        assertEquals(fidele1.getId(), crFidele1.getUtilisateurId());
        System.out.println("  CR Fidèle1: " + crFidele1.getId() + " (statut: " + crFidele1.getStatut() + ")");
    }

    @Test
    @Order(16)
    @DisplayName("7.2 - Fidèle2 crée un CR minimal")
    void step16_fidele2CreatesCR() {
        crFidele2 = createCRUseCase.execute(
                CreateCRCommand.builder()
                        .utilisateurId(fidele2.getId())
                        .date(LocalDate.now().minusDays(1))
                        .rdqd("0/1")
                        .priereSeule("00:20")
                        .lectureBiblique(1)
                        .build()
        );

        assertNotNull(crFidele2.getId());
        assertEquals("SOUMIS", crFidele2.getStatut());
        System.out.println("  CR Fidèle2: " + crFidele2.getId() + " (statut: " + crFidele2.getStatut() + ")");
    }

    @Test
    @Order(17)
    @DisplayName("7.3 - Fidèle3 crée un CR avec jeûne")
    void step17_fidele3CreatesCR() {
        crFidele3 = createCRUseCase.execute(
                CreateCRCommand.builder()
                        .utilisateurId(fidele3.getId())
                        .date(LocalDate.now().minusDays(1))
                        .rdqd("1/1")
                        .priereSeule("02:00")
                        .lectureBiblique(5)
                        .livreBiblique("Jean 1-5")
                        .jeune(true)
                        .typeJeune("Jeûne sec de 6h à 18h")
                        .evangelisation(2)
                        .confession(true)
                        .offrande(true)
                        .build()
        );

        assertNotNull(crFidele3.getId());
        assertTrue(crFidele3.getJeune());
        assertEquals("Jeûne sec de 6h à 18h", crFidele3.getTypeJeune());
        System.out.println("  CR Fidèle3: " + crFidele3.getId() + " (avec jeûne)");
    }

    @Test
    @Order(18)
    @DisplayName("7.4 - Vérifier qu'on ne peut pas créer deux CR pour la même date")
    void step18_rejectDuplicateDateCR() {
        assertThrows(IllegalArgumentException.class, () ->
                createCRUseCase.execute(
                        CreateCRCommand.builder()
                                .utilisateurId(fidele1.getId())
                                .date(LocalDate.now().minusDays(1)) // Même date que step15
                                .rdqd("1/1")
                                .priereSeule("01:00")
                                .lectureBiblique(2)
                                .build()
                )
        );
    }

    // ==========================================
    // ÉTAPE 8: VALIDATION DES CR PAR FD
    // ==========================================

    @Test
    @Order(19)
    @DisplayName("8.1 - FD1 valide le CR de Fidèle1")
    void step19_fd1ValidatesCR() {
        CRResponse validated = validateCRUseCase.execute(crFidele1.getId(), fd1.getId());

        assertEquals("VALIDE", validated.getStatut());
        assertTrue(validated.getVuParFd());
        System.out.println("  CR Fidèle1 validé par FD1 -> statut: " + validated.getStatut());
    }

    @Test
    @Order(20)
    @DisplayName("8.2 - FD1 valide le CR de Fidèle2")
    void step20_fd1ValidatesCR2() {
        CRResponse validated = validateCRUseCase.execute(crFidele2.getId(), fd1.getId());

        assertEquals("VALIDE", validated.getStatut());
        System.out.println("  CR Fidèle2 validé par FD1 -> statut: " + validated.getStatut());
    }

    @Test
    @Order(21)
    @DisplayName("8.3 - FD2 valide le CR de Fidèle3")
    void step21_fd2ValidatesCR3() {
        CRResponse validated = validateCRUseCase.execute(crFidele3.getId(), fd2.getId());

        assertEquals("VALIDE", validated.getStatut());
        System.out.println("  CR Fidèle3 validé par FD2 -> statut: " + validated.getStatut());
    }

    @Test
    @Order(22)
    @DisplayName("8.4 - Vérifier qu'on ne peut pas revalider un CR déjà validé")
    void step22_rejectRevalidation() {
        assertThrows(IllegalStateException.class, () ->
                validateCRUseCase.execute(crFidele1.getId(), fd1.getId())
        );
    }

    // ==========================================
    // ÉTAPE 9: COMMENTAIRES
    // ==========================================

    @Test
    @Order(23)
    @DisplayName("9.1 - FD1 ajoute un commentaire au CR de Fidèle1")
    void step23_addComment() {
        AddCommentaireCommand command = AddCommentaireCommand.builder()
                .compteRenduId(crFidele1.getId())
                .auteurId(fd1.getId())
                .contenu("Excellent travail spirituel ! Continue comme ça, que Dieu te bénisse.")
                .build();

        CommentaireResponse response = addCommentaireUseCase.execute(command);

        assertNotNull(response);
        assertNotNull(response.getId());
        assertEquals(crFidele1.getId(), response.getCompteRenduId());
        assertEquals(fd1.getId(), response.getAuteurId());
        System.out.println("  Commentaire ajouté par FD1 sur CR Fidèle1");
    }

    @Test
    @Order(24)
    @DisplayName("9.2 - Vérifier les commentaires du CR")
    void step24_getComments() {
        List<CommentaireResponse> comments = getCommentairesUseCase.getByCompteRenduId(crFidele1.getId());

        assertFalse(comments.isEmpty());
        assertEquals(1, comments.size());
        assertTrue(comments.get(0).getContenu().contains("Excellent travail"));
        System.out.println("  Nombre de commentaires sur CR Fidèle1: " + comments.size());
    }

    // ==========================================
    // ÉTAPE 10: VÉRIFICATIONS FINALES
    // ==========================================

    @Test
    @Order(25)
    @DisplayName("10.1 - Vérifier la hiérarchie complète")
    void step25_verifyHierarchy() {
        // Vérifier les membres de l'église de maison
        List<Utilisateur> membresEM = utilisateurRepo.findByEgliseMaisonId(egliseMaison.getId());
        assertTrue(membresEM.size() >= 5, "L'église de maison devrait avoir au moins 5 membres");

        // Vérifier les disciples de FD1
        List<Utilisateur> disciplesFD1 = utilisateurRepo.findByFdId(fd1.getId());
        assertEquals(2, disciplesFD1.size());

        // Vérifier les disciples de FD2
        List<Utilisateur> disciplesFD2 = utilisateurRepo.findByFdId(fd2.getId());
        assertEquals(1, disciplesFD2.size());

        // Vérifier les rôles
        List<Utilisateur> fideles = utilisateurRepo.findByRole(Role.FIDELE);
        List<Utilisateur> fds = utilisateurRepo.findByRole(Role.FD);
        List<Utilisateur> leaders = utilisateurRepo.findByRole(Role.LEADER);
        List<Utilisateur> pasteurs = utilisateurRepo.findByRole(Role.PASTEUR);

        assertEquals(3, fideles.size(), "3 fidèles");
        assertEquals(2, fds.size(), "2 FD");
        assertEquals(1, leaders.size(), "1 leader");
        assertEquals(1, pasteurs.size(), "1 pasteur");

        System.out.println("  === RÉSUMÉ HIÉRARCHIE ===");
        System.out.println("  Pasteurs: " + pasteurs.size());
        System.out.println("  Leaders: " + leaders.size());
        System.out.println("  FDs: " + fds.size());
        System.out.println("  Fidèles: " + fideles.size());
        System.out.println("  Total: " + (fideles.size() + fds.size() + leaders.size() + pasteurs.size()));
    }

    @Test
    @Order(26)
    @DisplayName("10.2 - Vérifier tous les CR créés et validés")
    void step26_verifyCRs() {
        // Tous les CR des fidèles
        List<CompteRendu> crsFidele1 = crRepo.findByUtilisateurId(fidele1.getId());
        List<CompteRendu> crsFidele2 = crRepo.findByUtilisateurId(fidele2.getId());
        List<CompteRendu> crsFidele3 = crRepo.findByUtilisateurId(fidele3.getId());

        assertEquals(1, crsFidele1.size());
        assertEquals(1, crsFidele2.size());
        assertEquals(1, crsFidele3.size());

        // Tous doivent être VALIDE
        assertTrue(crsFidele1.stream().allMatch(cr -> cr.getStatut() == StatutCR.VALIDE));
        assertTrue(crsFidele2.stream().allMatch(cr -> cr.getStatut() == StatutCR.VALIDE));
        assertTrue(crsFidele3.stream().allMatch(cr -> cr.getStatut() == StatutCR.VALIDE));

        System.out.println("  === RÉSUMÉ CR ===");
        System.out.println("  CR Fidèle1: " + crsFidele1.size() + " (tous validés)");
        System.out.println("  CR Fidèle2: " + crsFidele2.size() + " (tous validés)");
        System.out.println("  CR Fidèle3: " + crsFidele3.size() + " (tous validés)");
    }

    @Test
    @Order(27)
    @DisplayName("10.3 - Désassigner un fidèle de son FD et réassigner")
    void step27_reassignFD() {
        // Désassigner fidèle1 de FD1
        UtilisateurResponse unassigned = assignFDUseCase.execute(
                AssignFDCommand.builder()
                        .discipleId(fidele1.getId())
                        .fdId(null)
                        .build()
        );
        assertNull(unassigned.getFdId());

        // Réassigner à FD2
        UtilisateurResponse reassigned = assignFDUseCase.execute(
                AssignFDCommand.builder()
                        .discipleId(fidele1.getId())
                        .fdId(fd2.getId())
                        .build()
        );
        assertEquals(fd2.getId(), reassigned.getFdId());

        // Vérifier les compteurs
        assertEquals(1, utilisateurRepo.countByFdId(fd1.getId()), "FD1 devrait avoir 1 disciple");
        assertEquals(2, utilisateurRepo.countByFdId(fd2.getId()), "FD2 devrait avoir 2 disciples");

        System.out.println("  Fidèle1 désassigné de FD1, réassigné à FD2");
        System.out.println("  FD1: " + utilisateurRepo.countByFdId(fd1.getId()) + " disciples");
        System.out.println("  FD2: " + utilisateurRepo.countByFdId(fd2.getId()) + " disciples");
    }

    @Test
    @Order(28)
    @DisplayName("10.4 - Vérifier la structure géographique complète")
    void step28_verifyGeography() {
        // Régions
        List<Region> regions = regionRepo.findAll();
        assertEquals(1, regions.size());

        // Zones
        List<Zone> zones = zoneRepo.findByRegionId(region.getId());
        assertEquals(1, zones.size());

        // Églises locales
        List<EgliseLocale> eglisesLocales = egliseLocaleRepo.findByZoneId(zone.getId());
        assertEquals(1, eglisesLocales.size());

        // Églises de maison
        List<EgliseMaison> eglisesMaison = egliseMaisonRepo.findByEgliseLocaleId(egliseLocale.getId());
        assertEquals(1, eglisesMaison.size());

        System.out.println("  === STRUCTURE GÉOGRAPHIQUE ===");
        System.out.println("  Région: " + regions.get(0).getNom());
        System.out.println("    └─ Zone: " + zones.get(0).getNom());
        System.out.println("       └─ Église Locale: " + eglisesLocales.get(0).getNom());
        System.out.println("          └─ Église Maison: " + eglisesMaison.get(0).getNom());
    }

    // ==========================================
    // IN-MEMORY REPOSITORIES (Simulent la BD)
    // ==========================================

    static class InMemoryUtilisateurRepository implements UtilisateurRepository {
        private final Map<UUID, Utilisateur> store = new ConcurrentHashMap<>();

        @Override public Utilisateur save(Utilisateur u) { store.put(u.getId(), u); return u; }
        @Override public Optional<Utilisateur> findById(UUID id) { return Optional.ofNullable(store.get(id)); }
        @Override public Optional<Utilisateur> findByEmail(String email) { return store.values().stream().filter(u -> u.getEmail().equals(email)).findFirst(); }
        @Override public List<Utilisateur> findByEgliseMaisonId(UUID id) { return store.values().stream().filter(u -> id.equals(u.getEgliseMaisonId())).collect(Collectors.toList()); }
        @Override public List<Utilisateur> findByFdId(UUID fdId) { return store.values().stream().filter(u -> fdId.equals(u.getFdId())).collect(Collectors.toList()); }
        @Override public List<Utilisateur> findByRole(Role role) { return store.values().stream().filter(u -> u.getRole() == role).collect(Collectors.toList()); }
        @Override public boolean existsByEmail(String email) { return store.values().stream().anyMatch(u -> u.getEmail().equals(email)); }
        @Override public void deleteById(UUID id) { store.remove(id); }
        @Override public long countByFdId(UUID fdId) { return store.values().stream().filter(u -> fdId.equals(u.getFdId())).count(); }
        @Override public List<Utilisateur> findByEgliseMaisonIdIn(List<UUID> ids) { return store.values().stream().filter(u -> ids.contains(u.getEgliseMaisonId())).collect(Collectors.toList()); }
    }

    static class InMemoryCompteRenduRepository implements CompteRenduRepository {
        private final Map<UUID, CompteRendu> store = new ConcurrentHashMap<>();

        @Override public CompteRendu save(CompteRendu cr) { store.put(cr.getId(), cr); return cr; }
        @Override public Optional<CompteRendu> findById(UUID id) { return Optional.ofNullable(store.get(id)); }
        @Override public Optional<CompteRendu> findByUtilisateurIdAndDate(UUID uid, LocalDate date) { return store.values().stream().filter(cr -> cr.getUtilisateurId().equals(uid) && cr.getDate().equals(date)).findFirst(); }
        @Override public List<CompteRendu> findByUtilisateurId(UUID uid) { return store.values().stream().filter(cr -> cr.getUtilisateurId().equals(uid)).collect(Collectors.toList()); }
        @Override public List<CompteRendu> findByUtilisateurIdAndDateBetween(UUID uid, LocalDate start, LocalDate end) { return store.values().stream().filter(cr -> cr.getUtilisateurId().equals(uid) && !cr.getDate().isBefore(start) && !cr.getDate().isAfter(end)).collect(Collectors.toList()); }
        @Override public List<CompteRendu> findByUtilisateurIdAndVuParFdFalse(UUID uid) { return store.values().stream().filter(cr -> cr.getUtilisateurId().equals(uid) && !Boolean.TRUE.equals(cr.getVuParFd())).collect(Collectors.toList()); }
        @Override public void deleteById(UUID id) { store.remove(id); }
        @Override public boolean existsByUtilisateurIdAndDate(UUID uid, LocalDate date) { return store.values().stream().anyMatch(cr -> cr.getUtilisateurId().equals(uid) && cr.getDate().equals(date)); }
        @Override public long countByUtilisateurIdAndDateBetween(UUID uid, LocalDate start, LocalDate end) { return findByUtilisateurIdAndDateBetween(uid, start, end).size(); }
        @Override public List<CompteRendu> findByUtilisateurIdInAndDateBetween(List<UUID> uids, LocalDate start, LocalDate end) { return store.values().stream().filter(cr -> uids.contains(cr.getUtilisateurId()) && !cr.getDate().isBefore(start) && !cr.getDate().isAfter(end)).collect(Collectors.toList()); }
    }

    static class InMemoryRegionRepository implements RegionRepository {
        private final Map<UUID, Region> store = new ConcurrentHashMap<>();

        @Override public Region save(Region r) { store.put(r.getId(), r); return r; }
        @Override public Optional<Region> findById(UUID id) { return Optional.ofNullable(store.get(id)); }
        @Override public Optional<Region> findByCode(String code) { return store.values().stream().filter(r -> r.getCode().equals(code)).findFirst(); }
        @Override public List<Region> findAll() { return new ArrayList<>(store.values()); }
        @Override public boolean existsByCode(String code) { return store.values().stream().anyMatch(r -> r.getCode().equals(code)); }
        @Override public void deleteById(UUID id) { store.remove(id); }
    }

    static class InMemoryZoneRepository implements ZoneRepository {
        private final Map<UUID, Zone> store = new ConcurrentHashMap<>();

        @Override public Zone save(Zone z) { store.put(z.getId(), z); return z; }
        @Override public Optional<Zone> findById(UUID id) { return Optional.ofNullable(store.get(id)); }
        @Override public List<Zone> findByRegionId(UUID regionId) { return store.values().stream().filter(z -> z.getRegionId().equals(regionId)).collect(Collectors.toList()); }
        @Override public List<Zone> findAll() { return new ArrayList<>(store.values()); }
        @Override public void deleteById(UUID id) { store.remove(id); }
        @Override public long countByRegionId(UUID regionId) { return store.values().stream().filter(z -> z.getRegionId().equals(regionId)).count(); }
    }

    static class InMemoryEgliseLocaleRepository implements EgliseLocaleRepository {
        private final Map<UUID, EgliseLocale> store = new ConcurrentHashMap<>();

        @Override public EgliseLocale save(EgliseLocale el) { store.put(el.getId(), el); return el; }
        @Override public Optional<EgliseLocale> findById(UUID id) { return Optional.ofNullable(store.get(id)); }
        @Override public List<EgliseLocale> findByZoneId(UUID zoneId) { return store.values().stream().filter(el -> el.getZoneId().equals(zoneId)).collect(Collectors.toList()); }
        @Override public List<EgliseLocale> findByPasteurId(UUID pasteurId) { return store.values().stream().filter(el -> pasteurId.equals(el.getPasteurId())).collect(Collectors.toList()); }
        @Override public List<EgliseLocale> findAll() { return new ArrayList<>(store.values()); }
        @Override public void deleteById(UUID id) { store.remove(id); }
        @Override public long countByZoneId(UUID zoneId) { return store.values().stream().filter(el -> el.getZoneId().equals(zoneId)).count(); }
        @Override public List<EgliseLocale> findByPasteurIdIsNull() { return store.values().stream().filter(el -> el.getPasteurId() == null).collect(Collectors.toList()); }
    }

    static class InMemoryEgliseMaisonRepository implements EgliseMaisonRepository {
        private final Map<UUID, EgliseMaison> store = new ConcurrentHashMap<>();

        @Override public EgliseMaison save(EgliseMaison em) { store.put(em.getId(), em); return em; }
        @Override public Optional<EgliseMaison> findById(UUID id) { return Optional.ofNullable(store.get(id)); }
        @Override public List<EgliseMaison> findByEgliseLocaleId(UUID elId) { return store.values().stream().filter(em -> em.getEgliseLocaleId().equals(elId)).collect(Collectors.toList()); }
        @Override public List<EgliseMaison> findByLeaderId(UUID leaderId) { return store.values().stream().filter(em -> leaderId.equals(em.getLeaderId())).collect(Collectors.toList()); }
        @Override public List<EgliseMaison> findAll() { return new ArrayList<>(store.values()); }
        @Override public void deleteById(UUID id) { store.remove(id); }
        @Override public long countByEgliseLocaleId(UUID elId) { return store.values().stream().filter(em -> em.getEgliseLocaleId().equals(elId)).count(); }
        @Override public List<EgliseMaison> findByLeaderIdIsNull() { return store.values().stream().filter(em -> em.getLeaderId() == null).collect(Collectors.toList()); }
    }

    static class InMemoryCommentaireRepository implements CommentaireRepository {
        private final Map<UUID, Commentaire> store = new ConcurrentHashMap<>();

        @Override public Commentaire save(Commentaire c) { store.put(c.getId(), c); return c; }
        @Override public Optional<Commentaire> findById(UUID id) { return Optional.ofNullable(store.get(id)); }
        @Override public List<Commentaire> findByCompteRenduId(UUID crId) { return store.values().stream().filter(c -> c.getCompteRenduId().equals(crId)).collect(Collectors.toList()); }
        @Override public List<Commentaire> findByAuteurId(UUID auteurId) { return store.values().stream().filter(c -> c.getAuteurId().equals(auteurId)).collect(Collectors.toList()); }
        @Override public void deleteById(UUID id) { store.remove(id); }
        @Override public long countByCompteRenduId(UUID crId) { return store.values().stream().filter(c -> c.getCompteRenduId().equals(crId)).count(); }
    }
}
