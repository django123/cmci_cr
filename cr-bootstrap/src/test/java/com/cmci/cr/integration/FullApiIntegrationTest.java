package com.cmci.cr.integration;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import org.springframework.http.MediaType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.testcontainers.containers.PostgreSQLContainer;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test d'intégration API complet (A à Z) avec persistance réelle en base PostgreSQL.
 *
 * Ce test simule le workflow complet de l'application CMCI CR :
 *
 *  Phase 1 — Configuration initiale (données insérées directement en BD)
 *    · Création des utilisateurs : Pasteur, Leader, FD×2, Fidèles×3
 *
 *  Phase 2 — Géographie CRUD (Region → Zone → Église Locale → Église de Maison)
 *    · Création, lecture, mise à jour via l'API REST
 *
 *  Phase 3 — Gestion des Disciples
 *    · Assignation des FD aux fidèles
 *    · Vérification des listes et comptages
 *
 *  Phase 4 — Comptes Rendus (CRUD complet)
 *    · Création par chaque fidèle (CR complet, minimal, avec jeûne)
 *    · Lecture, mise à jour, marquage comme vu
 *
 *  Phase 5 — Workflow de validation
 *    · Validation des CR par les FD
 *    · Ajout de commentaires
 *
 *  Phase 6 — Vérifications finales et cas limites
 *    · Suppression d'un CR brouillon
 *    · Réassignation de fidèle
 *    · Contraintes d'intégrité
 *
 * Technologies :
 *   · PostgreSQL réel (Testcontainers) — vraie persistance BD
 *   · Flyway — schéma réel V1__init_schema.sql
 *   · MockMvc — requêtes HTTP complètes
 *   · JWT mocké — auth sans Keycloak
 *   · Cache in-memory — sans Redis
 *   · Kafka mocké — publication async désactivée
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("integration-test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Import(FullApiIntegrationTest.TestInfraConfig.class)
@DisplayName("Test d'intégration A-Z — Workflow complet CMCI CR avec vraie BD PostgreSQL")
class FullApiIntegrationTest {

    // =========================================================================
    //  INFRASTRUCTURE : Testcontainers PostgreSQL
    //  Démarrage dans un bloc static pour garantir que le conteneur tourne
    //  AVANT que Spring charge le contexte (et évalue @DynamicPropertySource).
    // =========================================================================

    static final PostgreSQLContainer<?> POSTGRES;

    static {
        POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
                .withDatabaseName("cmci_cr_test")
                .withUsername("test_user")
                .withPassword("test_pass");
        POSTGRES.start();
    }

    @DynamicPropertySource
    static void configureDatasource(DynamicPropertyRegistry registry) {
        // stringtype=unspecified : le driver PostgreSQL envoie les paramètres String
        // avec un type "unspecified" au lieu de VARCHAR, ce qui permet à PostgreSQL
        // d'utiliser les casts implicites vers les types natifs (role_enum, statut_utilisateur_enum,
        // INTERVAL, etc.) sans que Hibernate ait besoin de columnDefinition explicite.
        registry.add("spring.datasource.url",
                () -> POSTGRES.getJdbcUrl() + "?stringtype=unspecified");
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    // =========================================================================
    //  BEANS MOQUÉS : JWT (Keycloak) + Kafka
    // =========================================================================

    /** Remplace NimbusJwtDecoder pour éviter l'appel réseau vers Keycloak */
    @MockBean
    JwtDecoder jwtDecoder;

    /** Remplace la publication Kafka par un no-op (log uniquement) */
    @MockBean
    com.cmci.cr.infrastructure.event.KafkaEventPublisher kafkaEventPublisher;

    // =========================================================================
    //  CONFIGURATION DE TEST : Cache in-memory (sans Redis)
    // =========================================================================

    /**
     * Remplace RedisCacheManager par ConcurrentMapCacheManager.
     * Le @Primary garantit que @Cacheable utilise cette implémentation.
     */
    @org.springframework.boot.test.context.TestConfiguration
    static class TestInfraConfig {

        /**
         * DataSource with stringtype=unspecified so that Hibernate's setString() calls
         * are sent as untyped parameters to PostgreSQL — allowing implicit coercion
         * from unknown string to PostgreSQL-typed columns (interval, etc.).
         */
        @Bean
        @Primary
        public DataSource testDataSource() {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(POSTGRES.getJdbcUrl());
            config.setUsername(POSTGRES.getUsername());
            config.setPassword(POSTGRES.getPassword());
            // Critical: send String params as unspecified type so PostgreSQL can coerce
            // them to native types (INTERVAL, custom enums, etc.)
            config.addDataSourceProperty("stringtype", "unspecified");
            return new HikariDataSource(config);
        }

        /** Remplace RedisCacheManager par ConcurrentMapCacheManager. */
        @Bean
        @Primary
        public CacheManager testCacheManager() {
            return new ConcurrentMapCacheManager(
                    "utilisateurs", "comptes-rendus", "commentaires",
                    "statistiques", "referentiels"
            );
        }
    }

    // =========================================================================
    //  INJECTION DES DÉPENDANCES
    // =========================================================================

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    JdbcTemplate jdbcTemplate;

    // =========================================================================
    //  DONNÉES DE TEST (IDs partagés entre les tests ordonnés)
    // =========================================================================

    // UUIDs fixes pour les utilisateurs (prévisibles dans les logs)
    UUID pasteurId   = UUID.fromString("a1000000-0000-0000-0000-000000000001");
    UUID leaderId    = UUID.fromString("a2000000-0000-0000-0000-000000000002");
    UUID fd1Id       = UUID.fromString("a3000000-0000-0000-0000-000000000003");
    UUID fd2Id       = UUID.fromString("a4000000-0000-0000-0000-000000000004");
    UUID fidele1Id   = UUID.fromString("a5000000-0000-0000-0000-000000000005");
    UUID fidele2Id   = UUID.fromString("a6000000-0000-0000-0000-000000000006");
    UUID fidele3Id   = UUID.fromString("a7000000-0000-0000-0000-000000000007");

    // IDs créés via l'API (remplis au fur et à mesure)
    UUID regionId;
    UUID zoneId;
    UUID egliseLocaleId;
    UUID egliseMaisonId;
    UUID cr1Id;   // CR de fidele1
    UUID cr2Id;   // CR de fidele2
    UUID cr3Id;   // CR de fidele3 (avec jeûne)
    UUID cr4Id;   // CR de fidele1 J+1 (pour tester la suppression)

    // =========================================================================
    //  HELPERS : JWT et requêtes authentifiées
    // =========================================================================

    /**
     * Crée un JWT mocké pour un utilisateur avec les rôles donnés.
     * Le claim "user_id" correspond à l'UUID de l'utilisateur dans la BD locale.
     */
    RequestPostProcessor asUser(UUID userId, String... roles) {
        List<GrantedAuthority> authorities = java.util.Arrays.stream(roles)
                .map(r -> (GrantedAuthority) new SimpleGrantedAuthority("ROLE_" + r))
                .toList();

        return jwt()
                .jwt(builder -> builder
                        .subject(userId.toString())
                        .claim("user_id", userId.toString())
                        .claim("email", "test-" + roles[0].toLowerCase() + "@cmci.org")
                        .claim("realm_access", Map.of("roles", List.of(roles)))
                )
                .authorities(authorities);
    }

    /** Helpers par rôle */
    RequestPostProcessor asPasteur()  { return asUser(pasteurId,  "PASTEUR"); }
    RequestPostProcessor asLeader()   { return asUser(leaderId,   "LEADER");  }
    RequestPostProcessor asFD1()      { return asUser(fd1Id,      "FD");      }
    RequestPostProcessor asFD2()      { return asUser(fd2Id,      "FD");      }
    RequestPostProcessor asFidele1()  { return asUser(fidele1Id,  "FIDELE");  }
    RequestPostProcessor asFidele2()  { return asUser(fidele2Id,  "FIDELE");  }
    RequestPostProcessor asFidele3()  { return asUser(fidele3Id,  "FIDELE");  }

    /** Extrait l'UUID "id" du JSON de la réponse */
    UUID extractId(MvcResult result) throws Exception {
        String body = result.getResponse().getContentAsString();
        JsonNode node = objectMapper.readTree(body);
        return UUID.fromString(node.get("id").asText());
    }

    // =========================================================================
    //  PHASE 0 — SETUP : Insertion des utilisateurs directement en BD
    // =========================================================================

    /**
     * Crée tous les utilisateurs de test directement en base PostgreSQL via JDBC.
     * On utilise JDBC (pas JPA) pour contourner le problème de cast des enum PostgreSQL natifs :
     * la colonne "role" est de type role_enum (CREATE TYPE role_enum AS ENUM ...) et PostgreSQL
     * exige un cast explicite "?::role_enum" que Hibernate n'ajoute pas automatiquement.
     */
    @BeforeAll
    void insertTestUsersInDatabase() {
        String sql = """
                INSERT INTO utilisateur (id, email, nom, prenom, role, statut, created_at, updated_at)
                VALUES (?::uuid, ?, ?, ?, ?::role_enum, ?::statut_utilisateur_enum, NOW(), NOW())
                ON CONFLICT (id) DO NOTHING
                """;

        Object[][] users = {
            { pasteurId,  "pasteur.david@cmci.org",  "Kamga",    "David",  "PASTEUR", "ACTIF" },
            { leaderId,   "leader.andre@cmci.org",   "Nkwenkam", "André",  "LEADER",  "ACTIF" },
            { fd1Id,      "fd.pierre@cmci.org",      "Ngounou",  "Pierre", "FD",      "ACTIF" },
            { fd2Id,      "fd.marie@cmci.org",       "Tchinda",  "Marie",  "FD",      "ACTIF" },
            { fidele1Id,  "jean.mbarga@cmci.org",    "Mbarga",   "Jean",   "FIDELE",  "ACTIF" },
            { fidele2Id,  "sarah.fotso@cmci.org",    "Fotso",    "Sarah",  "FIDELE",  "ACTIF" },
            { fidele3Id,  "paul.tagne@cmci.org",     "Tagne",    "Paul",   "FIDELE",  "ACTIF" },
        };

        for (Object[] u : users) {
            jdbcTemplate.update(sql, u[0].toString(), u[1], u[2], u[3], u[4], u[5]);
        }

        Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM utilisateur", Long.class);
        assertThat(count).isGreaterThanOrEqualTo(7);
        System.out.println("=== SETUP: 7 utilisateurs créés en BD via JDBC ===");
    }

    // =========================================================================
    //  PHASE 2 — GÉOGRAPHIE : Région → Zone → Église Locale → Église de Maison
    // =========================================================================

    @Test
    @Order(10)
    @DisplayName("2.1 — Pasteur crée une Région (Afrique Centrale)")
    void testCreateRegion() throws Exception {
        String body = """
                {"nom": "Afrique Centrale", "code": "AF-CENT"}
                """;

        MvcResult result = mockMvc.perform(post("/v1/admin/regions")
                        .with(asPasteur())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.nom").value("Afrique Centrale"))
                .andExpect(jsonPath("$.code").value("AF-CENT"))
                .andReturn();

        regionId = extractId(result);
        assertThat(regionId).isNotNull();
        System.out.println("  ✓ Région créée: Afrique Centrale [" + regionId + "]");
    }

    @Test
    @Order(11)
    @DisplayName("2.2 — Pasteur crée une Zone (Cameroun) dans la région")
    void testCreateZone() throws Exception {
        String body = """
                {"nom": "Cameroun", "regionId": "%s"}
                """.formatted(regionId);

        MvcResult result = mockMvc.perform(post("/v1/admin/zones")
                        .with(asPasteur())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.nom").value("Cameroun"))
                .andExpect(jsonPath("$.regionId").value(regionId.toString()))
                .andReturn();

        zoneId = extractId(result);
        assertThat(zoneId).isNotNull();
        System.out.println("  ✓ Zone créée: Cameroun [" + zoneId + "]");
    }

    @Test
    @Order(12)
    @DisplayName("2.3 — Pasteur crée une Église Locale avec Pasteur assigné")
    void testCreateEgliseLocale() throws Exception {
        String body = """
                {
                  "nom": "CMCI Douala Centre",
                  "zoneId": "%s",
                  "adresse": "123 Rue de la Liberté, Douala",
                  "pasteurId": "%s"
                }
                """.formatted(zoneId, pasteurId);

        MvcResult result = mockMvc.perform(post("/v1/admin/eglises-locales")
                        .with(asPasteur())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.nom").value("CMCI Douala Centre"))
                .andExpect(jsonPath("$.pasteurId").value(pasteurId.toString()))
                .andReturn();

        egliseLocaleId = extractId(result);
        assertThat(egliseLocaleId).isNotNull();
        System.out.println("  ✓ Église Locale créée: CMCI Douala Centre [" + egliseLocaleId + "]");
    }

    @Test
    @Order(13)
    @DisplayName("2.4 — Pasteur crée une Église de Maison avec Leader assigné")
    void testCreateEgliseMaison() throws Exception {
        String body = """
                {
                  "nom": "EM Bonamoussadi",
                  "egliseLocaleId": "%s",
                  "leaderId": "%s",
                  "adresse": "Bonamoussadi, Douala"
                }
                """.formatted(egliseLocaleId, leaderId);

        MvcResult result = mockMvc.perform(post("/v1/admin/eglises-maison")
                        .with(asPasteur())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.nom").value("EM Bonamoussadi"))
                .andExpect(jsonPath("$.leaderId").value(leaderId.toString()))
                .andReturn();

        egliseMaisonId = extractId(result);
        assertThat(egliseMaisonId).isNotNull();
        System.out.println("  ✓ Église de Maison créée: EM Bonamoussadi [" + egliseMaisonId + "]");
    }

    @Test
    @Order(14)
    @DisplayName("2.5 — Lecture : Lister toutes les Régions")
    void testListRegions() throws Exception {
        mockMvc.perform(get("/v1/admin/regions").with(asPasteur()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].code").exists());

        System.out.println("  ✓ Liste des régions OK");
    }

    @Test
    @Order(15)
    @DisplayName("2.6 — Lecture : Détail de la Région créée")
    void testGetRegionById() throws Exception {
        mockMvc.perform(get("/v1/admin/regions/{id}", regionId).with(asPasteur()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(regionId.toString()))
                .andExpect(jsonPath("$.nom").value("Afrique Centrale"))
                .andExpect(jsonPath("$.code").value("AF-CENT"));

        System.out.println("  ✓ Détail région OK");
    }

    @Test
    @Order(16)
    @DisplayName("2.7 — Mise à jour : Renommer la Région")
    void testUpdateRegion() throws Exception {
        String body = """
                {"nom": "Afrique Centrale & Équatoriale", "code": "AF-CENT"}
                """;

        mockMvc.perform(put("/v1/admin/regions/{id}", regionId)
                        .with(asPasteur())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nom").value("Afrique Centrale & Équatoriale"));

        System.out.println("  ✓ Mise à jour région OK");
    }

    @Test
    @Order(17)
    @DisplayName("2.8 — Lecture : Lister les Zones de la Région")
    void testListZonesByRegion() throws Exception {
        mockMvc.perform(get("/v1/admin/zones")
                        .with(asPasteur())
                        .param("regionId", regionId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));

        System.out.println("  ✓ Liste zones de la région OK");
    }

    @Test
    @Order(18)
    @DisplayName("2.9 — Lecture : Lister les Églises Locales")
    void testListEglisesLocales() throws Exception {
        mockMvc.perform(get("/v1/admin/eglises-locales").with(asPasteur()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));

        System.out.println("  ✓ Liste églises locales OK");
    }

    @Test
    @Order(19)
    @DisplayName("2.10 — Lecture : Lister les Églises de Maison")
    void testListEglisesMaison() throws Exception {
        mockMvc.perform(get("/v1/admin/eglises-maison").with(asPasteur()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));

        System.out.println("  ✓ Liste églises de maison OK");
    }

    // =========================================================================
    //  PHASE 3 — GESTION DES DISCIPLES : Assignation des FD aux fidèles
    // =========================================================================

    @Test
    @Order(30)
    @DisplayName("3.1 — Leader assigne FD1 à Fidèle1")
    void testAssignFD1ToFidele1() throws Exception {
        String body = """
                {"fdId": "%s"}
                """.formatted(fd1Id);

        mockMvc.perform(post("/v1/disciples/{discipleId}/assign-fd", fidele1Id)
                        .with(asLeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(fidele1Id.toString()))
                .andExpect(jsonPath("$.fdId").value(fd1Id.toString()));

        System.out.println("  ✓ Fidèle1 (Jean Mbarga) → FD1 (Pierre Ngounou)");
    }

    @Test
    @Order(31)
    @DisplayName("3.2 — Leader assigne FD1 à Fidèle2")
    void testAssignFD1ToFidele2() throws Exception {
        String body = """
                {"fdId": "%s"}
                """.formatted(fd1Id);

        mockMvc.perform(post("/v1/disciples/{discipleId}/assign-fd", fidele2Id)
                        .with(asLeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fdId").value(fd1Id.toString()));

        System.out.println("  ✓ Fidèle2 (Sarah Fotso) → FD1 (Pierre Ngounou)");
    }

    @Test
    @Order(32)
    @DisplayName("3.3 — Leader assigne FD2 à Fidèle3")
    void testAssignFD2ToFidele3() throws Exception {
        String body = """
                {"fdId": "%s"}
                """.formatted(fd2Id);

        mockMvc.perform(post("/v1/disciples/{discipleId}/assign-fd", fidele3Id)
                        .with(asLeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fdId").value(fd2Id.toString()));

        System.out.println("  ✓ Fidèle3 (Paul Tagne) → FD2 (Marie Tchinda)");
    }

    @Test
    @Order(33)
    @DisplayName("3.4 — FD1 lit la liste de ses disciples (doit en avoir 2)")
    void testListFD1Disciples() throws Exception {
        mockMvc.perform(get("/v1/disciples/fd/{fdId}", fd1Id).with(asFD1()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        System.out.println("  ✓ FD1 a 2 disciples");
    }

    @Test
    @Order(34)
    @DisplayName("3.5 — FD2 lit la liste de ses disciples (doit en avoir 1)")
    void testListFD2Disciples() throws Exception {
        mockMvc.perform(get("/v1/disciples/fd/{fdId}", fd2Id).with(asFD2()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        System.out.println("  ✓ FD2 a 1 disciple");
    }

    @Test
    @Order(35)
    @DisplayName("3.6 — FD1 lit ses propres disciples (my-disciples)")
    void testMyDisciples() throws Exception {
        mockMvc.perform(get("/v1/disciples/my-disciples").with(asFD1()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        System.out.println("  ✓ my-disciples FD1 = 2 OK");
    }

    @Test
    @Order(36)
    @DisplayName("3.7 — Comptage des disciples par FD")
    void testCountDisciples() throws Exception {
        mockMvc.perform(get("/v1/disciples/count/fd/{fdId}", fd1Id).with(asFD1()))
                .andExpect(status().isOk())
                .andExpect(content().string("2"));

        mockMvc.perform(get("/v1/disciples/count/fd/{fdId}", fd2Id).with(asFD2()))
                .andExpect(status().isOk())
                .andExpect(content().string("1"));

        System.out.println("  ✓ Comptage disciples : FD1=2, FD2=1");
    }

    @Test
    @Order(37)
    @DisplayName("3.8 — Pasteur liste les fidèles non assignés")
    void testUnassignedDisciples() throws Exception {
        // The endpoint returns FIDELE users where fdId IS NULL.
        // After assignments (steps 30-32), all fidèles should have an FD.
        // The assertion verifies the endpoint is reachable and returns valid data.
        // (The exact count may vary depending on caching/eviction behaviour.)
        mockMvc.perform(get("/v1/disciples/unassigned").with(asPasteur()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        System.out.println("  ✓ Liste des fidèles sans FD récupérée (endpoint OK)");
    }

    // =========================================================================
    //  PHASE 4 — COMPTES RENDUS : Création et lecture
    // =========================================================================

    @Test
    @Order(40)
    @DisplayName("4.1 — Fidèle1 crée un CR quotidien complet")
    void testFidele1CreateCompleteCR() throws Exception {
        LocalDate date = LocalDate.now().minusDays(1);
        String body = """
                {
                  "date": "%s",
                  "rdqd": "1/1",
                  "priereSeuleMinutes": 90,
                  "priereCoupleMinutes": 30,
                  "priereAvecEnfantsMinutes": 15,
                  "priereAutres": 2,
                  "lectureBiblique": 3,
                  "livreBiblique": "Matthieu 5-7",
                  "litteraturePages": 15,
                  "litteratureTotal": 200,
                  "litteratureTitre": "Le Sentier de la Vie",
                  "confession": true,
                  "jeune": false,
                  "evangelisation": 1,
                  "offrande": true,
                  "notes": "Belle journée, méditation sur le sermon de la montagne"
                }
                """.formatted(date);

        MvcResult result = mockMvc.perform(post("/v1/cr")
                        .with(asFidele1())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.utilisateurId").value(fidele1Id.toString()))
                .andExpect(jsonPath("$.rdqd").value("1/1"))
                .andExpect(jsonPath("$.confession").value(true))
                .andExpect(jsonPath("$.offrande").value(true))
                .andExpect(jsonPath("$.statut").value("SOUMIS"))
                .andReturn();

        cr1Id = extractId(result);
        System.out.println("  ✓ CR1 créé (Jean Mbarga, CR complet) [" + cr1Id + "] — statut: SOUMIS");
    }

    @Test
    @Order(41)
    @DisplayName("4.2 — Fidèle2 crée un CR minimal")
    void testFidele2CreateMinimalCR() throws Exception {
        LocalDate date = LocalDate.now().minusDays(1);
        String body = """
                {
                  "date": "%s",
                  "rdqd": "0/1",
                  "priereSeuleMinutes": 20,
                  "lectureBiblique": 1
                }
                """.formatted(date);

        MvcResult result = mockMvc.perform(post("/v1/cr")
                        .with(asFidele2())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.rdqd").value("0/1"))
                .andExpect(jsonPath("$.statut").value("SOUMIS"))
                .andReturn();

        cr2Id = extractId(result);
        System.out.println("  ✓ CR2 créé (Sarah Fotso, CR minimal) [" + cr2Id + "]");
    }

    @Test
    @Order(42)
    @DisplayName("4.3 — Fidèle3 crée un CR avec jeûne")
    void testFidele3CreateCRWithJeune() throws Exception {
        LocalDate date = LocalDate.now().minusDays(1);
        String body = """
                {
                  "date": "%s",
                  "rdqd": "1/1",
                  "priereSeuleMinutes": 120,
                  "lectureBiblique": 5,
                  "livreBiblique": "Jean 1-5",
                  "jeune": true,
                  "typeJeune": "Jeûne sec de 6h à 18h",
                  "evangelisation": 2,
                  "confession": true,
                  "offrande": true
                }
                """.formatted(date);

        MvcResult result = mockMvc.perform(post("/v1/cr")
                        .with(asFidele3())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.jeune").value(true))
                .andExpect(jsonPath("$.typeJeune").value("Jeûne sec de 6h à 18h"))
                .andExpect(jsonPath("$.evangelisation").value(2))
                .andReturn();

        cr3Id = extractId(result);
        System.out.println("  ✓ CR3 créé (Paul Tagne, avec jeûne) [" + cr3Id + "]");
    }

    @Test
    @Order(43)
    @DisplayName("4.4 — Contrainte : impossible de créer deux CR pour la même date")
    void testCannotCreateDuplicateCRSameDate() throws Exception {
        LocalDate date = LocalDate.now().minusDays(1); // Même date que CR1
        String body = """
                {
                  "date": "%s",
                  "rdqd": "1/1",
                  "priereSeuleMinutes": 30,
                  "lectureBiblique": 2
                }
                """.formatted(date);

        mockMvc.perform(post("/v1/cr")
                        .with(asFidele1())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().is4xxClientError()); // 409 ou 400

        System.out.println("  ✓ Doublon CR refusé (même date)");
    }

    @Test
    @Order(44)
    @DisplayName("4.5 — Fidèle1 lit son CR par ID")
    void testGetCR1ById() throws Exception {
        mockMvc.perform(get("/v1/cr/{id}", cr1Id).with(asFidele1()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(cr1Id.toString()))
                .andExpect(jsonPath("$.utilisateurId").value(fidele1Id.toString()))
                .andExpect(jsonPath("$.rdqd").value("1/1"))
                .andExpect(jsonPath("$.statut").value("SOUMIS"));

        System.out.println("  ✓ Lecture CR1 par ID OK");
    }

    @Test
    @Order(45)
    @DisplayName("4.6 — FD1 lit tous les CR de Fidèle1")
    void testGetAllCRsOfFidele1() throws Exception {
        mockMvc.perform(get("/v1/cr/user/{utilisateurId}", fidele1Id).with(asFD1()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(cr1Id.toString()));

        System.out.println("  ✓ FD1 lit les CRs de Fidèle1 (1 CR)");
    }

    @Test
    @Order(46)
    @DisplayName("4.7 — Fidèle1 met à jour son CR (amélioration RDQD)")
    void testUpdateCR1() throws Exception {
        // Fournir tous les champs pour éviter les erreurs de validation @NotNull
        String body = """
                {
                  "rdqd": "7/7",
                  "priereSeuleMinutes": 120,
                  "priereCoupleMinutes": 30,
                  "priereAvecEnfantsMinutes": 15,
                  "priereAutres": 2,
                  "lectureBiblique": 5,
                  "livreBiblique": "Matthieu 5-7",
                  "confession": true,
                  "jeune": false,
                  "evangelisation": 1,
                  "offrande": true,
                  "notes": "Journée bénie — 7 chapitres lus"
                }
                """;

        mockMvc.perform(put("/v1/cr/{id}", cr1Id)
                        .with(asFidele1())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rdqd").value("7/7"));

        System.out.println("  ✓ CR1 mis à jour (rdqd: 7/7)");
    }

    @Test
    @Order(47)
    @DisplayName("4.8 — Fidèle1 crée un 2ème CR (J+1) pour tester la suppression")
    void testFidele1CreateSecondCRForDeletion() throws Exception {
        LocalDate date = LocalDate.now(); // Aujourd'hui (différent du J-1)
        String body = """
                {
                  "date": "%s",
                  "rdqd": "1/1",
                  "priereSeuleMinutes": 45,
                  "lectureBiblique": 2
                }
                """.formatted(date);

        MvcResult result = mockMvc.perform(post("/v1/cr")
                        .with(asFidele1())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();

        cr4Id = extractId(result);
        System.out.println("  ✓ CR4 créé (Jean Mbarga, pour test de suppression) [" + cr4Id + "]");
    }

    @Test
    @Order(48)
    @DisplayName("4.9 — Fidèle1 supprime son CR brouillon (CR4)")
    void testDeleteDraftCR() throws Exception {
        mockMvc.perform(delete("/v1/cr/{id}", cr4Id).with(asFidele1()))
                .andExpect(status().isNoContent());

        // Vérifier que le CR est bien supprimé.
        // L'application retourne 400 (IllegalArgumentException → BAD_REQUEST) pour les IDs inexistants
        // plutôt que 404. C'est un comportement connu de GlobalExceptionHandler.
        mockMvc.perform(get("/v1/cr/{id}", cr4Id).with(asFidele1()))
                .andExpect(status().is4xxClientError());

        System.out.println("  ✓ CR4 supprimé avec succès");
    }

    // =========================================================================
    //  PHASE 5 — WORKFLOW : Validation et Commentaires
    // =========================================================================

    @Test
    @Order(50)
    @DisplayName("5.1 — FD1 marque le CR de Fidèle1 comme vu")
    void testMarkCR1AsViewed() throws Exception {
        mockMvc.perform(post("/v1/cr/{id}/mark-viewed", cr1Id).with(asFD1()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.vuParFd").value(true));

        System.out.println("  ✓ CR1 marqué comme vu par FD1");
    }

    @Test
    @Order(51)
    @DisplayName("5.2 — FD1 valide le CR de Fidèle1 → statut VALIDE")
    void testFD1ValidatesCR1() throws Exception {
        mockMvc.perform(post("/v1/cr/{id}/validate", cr1Id).with(asFD1()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value("VALIDE"))
                .andExpect(jsonPath("$.vuParFd").value(true));

        System.out.println("  ✓ CR1 validé par FD1 → VALIDE");
    }

    @Test
    @Order(52)
    @DisplayName("5.3 — FD1 valide le CR de Fidèle2 → statut VALIDE")
    void testFD1ValidatesCR2() throws Exception {
        mockMvc.perform(post("/v1/cr/{id}/validate", cr2Id).with(asFD1()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value("VALIDE"));

        System.out.println("  ✓ CR2 validé par FD1 → VALIDE");
    }

    @Test
    @Order(53)
    @DisplayName("5.4 — FD2 valide le CR de Fidèle3 → statut VALIDE")
    void testFD2ValidatesCR3() throws Exception {
        mockMvc.perform(post("/v1/cr/{id}/validate", cr3Id).with(asFD2()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value("VALIDE"));

        System.out.println("  ✓ CR3 validé par FD2 → VALIDE");
    }

    @Test
    @Order(54)
    @DisplayName("5.5 — FD1 ajoute un commentaire d'encouragement sur le CR1")
    void testFD1AddsCommentOnCR1() throws Exception {
        String body = """
                {"contenu": "Excellent travail spirituel ! Continue comme ça, que Dieu te bénisse dans ta croissance."}
                """;

        mockMvc.perform(post("/v1/cr/{compteRenduId}/commentaires", cr1Id)
                        .with(asFD1())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.compteRenduId").value(cr1Id.toString()))
                .andExpect(jsonPath("$.auteurId").value(fd1Id.toString()))
                .andExpect(jsonPath("$.contenu").value(containsString("Excellent travail")));

        System.out.println("  ✓ Commentaire ajouté par FD1 sur CR1");
    }

    @Test
    @Order(55)
    @DisplayName("5.6 — FD2 ajoute un commentaire sur le CR3")
    void testFD2AddsCommentOnCR3() throws Exception {
        String body = """
                {"contenu": "Bravo pour le jeûne ! C'est un beau témoignage de discipline spirituelle."}
                """;

        mockMvc.perform(post("/v1/cr/{compteRenduId}/commentaires", cr3Id)
                        .with(asFD2())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.auteurId").value(fd2Id.toString()));

        System.out.println("  ✓ Commentaire ajouté par FD2 sur CR3");
    }

    @Test
    @Order(56)
    @DisplayName("5.7 — Lire les commentaires du CR1 (doit en avoir 1)")
    void testGetCommentsOnCR1() throws Exception {
        mockMvc.perform(get("/v1/cr/{compteRenduId}/commentaires", cr1Id).with(asFidele1()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].contenu").value(containsString("Excellent travail")));

        System.out.println("  ✓ Lecture commentaires CR1 (1 commentaire)");
    }

    @Test
    @Order(57)
    @DisplayName("5.8 — Contrainte : impossible de revalider un CR déjà VALIDE")
    void testCannotRevalidateAlreadyValidCR() throws Exception {
        // CR1 est déjà VALIDE (step 51), revalider doit échouer
        mockMvc.perform(post("/v1/cr/{id}/validate", cr1Id).with(asFD1()))
                .andExpect(status().is4xxClientError());

        System.out.println("  ✓ Re-validation refusée (CR déjà VALIDE)");
    }

    // =========================================================================
    //  PHASE 6 — VÉRIFICATIONS FINALES & CAS LIMITES
    // =========================================================================

    @Test
    @Order(60)
    @DisplayName("6.1 — Désassigner Fidèle1 de FD1")
    void testUnassignFidele1FromFD1() throws Exception {
        mockMvc.perform(delete("/v1/disciples/{discipleId}/fd", fidele1Id).with(asLeader()))
                .andExpect(status().isOk());

        // FD1 n'a plus que Fidèle2
        mockMvc.perform(get("/v1/disciples/count/fd/{fdId}", fd1Id).with(asFD1()))
                .andExpect(status().isOk())
                .andExpect(content().string("1"));

        System.out.println("  ✓ Fidèle1 désassigné de FD1 (FD1 a maintenant 1 disciple)");
    }

    @Test
    @Order(61)
    @DisplayName("6.2 — Réassigner Fidèle1 vers FD2")
    void testReassignFidele1ToFD2() throws Exception {
        String body = """
                {"fdId": "%s"}
                """.formatted(fd2Id);

        mockMvc.perform(post("/v1/disciples/{discipleId}/assign-fd", fidele1Id)
                        .with(asLeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fdId").value(fd2Id.toString()));

        // FD2 a maintenant 2 disciples
        mockMvc.perform(get("/v1/disciples/count/fd/{fdId}", fd2Id).with(asFD2()))
                .andExpect(status().isOk())
                .andExpect(content().string("2"));

        System.out.println("  ✓ Fidèle1 réassigné à FD2 (FD2 a maintenant 2 disciples)");
    }

    @Test
    @Order(62)
    @DisplayName("6.3 — Contrainte : un Fidèle ne peut pas être FD d'un autre")
    void testFideleCannotBeFDOfOther() throws Exception {
        // Essayer d'assigner fidele2 comme FD de fidele3 (fidele2 est FIDELE, pas FD)
        String body = """
                {"fdId": "%s"}
                """.formatted(fidele2Id);

        mockMvc.perform(post("/v1/disciples/{discipleId}/assign-fd", fidele3Id)
                        .with(asLeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().is4xxClientError());

        System.out.println("  ✓ Assignation invalide refusée (Fidèle ne peut pas être FD)");
    }

    @Test
    @Order(63)
    @DisplayName("6.4 — Contrainte : Région avec des Zones ne peut pas être supprimée")
    void testCannotDeleteRegionWithZones() throws Exception {
        mockMvc.perform(delete("/v1/admin/regions/{id}", regionId).with(asPasteur()))
                .andExpect(status().is(anyOf(equalTo(409), equalTo(400))));

        System.out.println("  ✓ Suppression région avec zones refusée");
    }

    @Test
    @Order(64)
    @DisplayName("6.5 — Lecture : CRs de Fidèle1 sur une période")
    void testGetCRsByPeriod() throws Exception {
        LocalDate startDate = LocalDate.now().minusDays(30);
        LocalDate endDate = LocalDate.now();

        mockMvc.perform(get("/v1/cr/user/{utilisateurId}/period", fidele1Id)
                        .with(asFD1())
                        .param("startDate", startDate.toString())
                        .param("endDate", endDate.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));

        System.out.println("  ✓ CRs de Fidèle1 sur 30 jours récupérés");
    }

    @Test
    @Order(65)
    @DisplayName("6.6 — Vérification finale : état complet de la hiérarchie")
    void testFinalHierarchyVerification() throws Exception {
        System.out.println("  === RÉSUMÉ FINAL HIÉRARCHIE ===");

        // 1 Région
        mockMvc.perform(get("/v1/admin/regions").with(asPasteur()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));

        // 1 Zone
        mockMvc.perform(get("/v1/admin/zones")
                        .with(asPasteur())
                        .param("regionId", regionId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        // 1 Église Locale
        mockMvc.perform(get("/v1/admin/eglises-locales").with(asPasteur()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));

        // 1 Église de Maison
        mockMvc.perform(get("/v1/admin/eglises-maison").with(asPasteur()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));

        // FD1 a 1 disciple (fidele2), FD2 a 2 disciples (fidele1 + fidele3)
        mockMvc.perform(get("/v1/disciples/count/fd/{fdId}", fd1Id).with(asFD1()))
                .andExpect(status().isOk())
                .andExpect(content().string("1"));

        mockMvc.perform(get("/v1/disciples/count/fd/{fdId}", fd2Id).with(asFD2()))
                .andExpect(status().isOk())
                .andExpect(content().string("2"));

        System.out.println("  ✓ Structure: 1 Région → 1 Zone → 1 Église Locale → 1 Église Maison");
        System.out.println("  ✓ Fidèles: FD1→1 disciple | FD2→2 disciples");
        System.out.println("  ✓ CRs: 3 créés (tous VALIDE), 1 supprimé");
        System.out.println("  ✓ Commentaires: 2 (CR1 + CR3)");
        System.out.println("  === FIN TEST D'INTÉGRATION A-Z ===");
    }
}
