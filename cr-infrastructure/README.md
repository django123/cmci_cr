# Module cr-infrastructure

Module d'infrastructure implémentant les ports définis dans le module domaine (Hexagonal Architecture).

## Responsabilités

Ce module contient toutes les implémentations techniques :
- **Persistence** : Repositories JPA et mappers
- **Cache** : Configuration Redis et décorateurs de cache
- **Messaging** : Configuration Kafka et publishers d'événements
- **Security** : Configuration OAuth2/Keycloak et JWT
- **Configuration** : application.yml et autres configurations Spring

## Structure du Module

```
cr-infrastructure/
├── src/main/java/com/cmci/cr/infrastructure/
│   ├── persistence/
│   │   ├── entity/              # Entités JPA
│   │   │   ├── CompteRenduJpaEntity.java
│   │   │   ├── UtilisateurJpaEntity.java
│   │   │   ├── CommentaireJpaEntity.java
│   │   │   ├── RegionJpaEntity.java
│   │   │   ├── ZoneJpaEntity.java
│   │   │   ├── EgliseLocaleJpaEntity.java
│   │   │   └── EgliseMaisonJpaEntity.java
│   │   ├── repository/          # Repositories Spring Data JPA
│   │   │   ├── CompteRenduJpaRepository.java
│   │   │   ├── UtilisateurJpaRepository.java
│   │   │   ├── CommentaireJpaRepository.java
│   │   │   ├── RegionJpaRepository.java
│   │   │   ├── ZoneJpaRepository.java
│   │   │   ├── EgliseLocaleJpaRepository.java
│   │   │   └── EgliseMaisonJpaRepository.java
│   │   ├── adapter/             # Adaptateurs implémentant les ports du domaine
│   │   │   ├── CompteRenduRepositoryAdapter.java
│   │   │   ├── UtilisateurRepositoryAdapter.java
│   │   │   └── CommentaireRepositoryAdapter.java
│   │   └── mapper/              # Mappers Domain <-> JPA
│   │       ├── CompteRenduMapper.java
│   │       ├── UtilisateurMapper.java
│   │       └── CommentaireMapper.java
│   ├── cache/                   # Configuration et décorateurs de cache
│   │   ├── CacheNames.java
│   │   ├── CacheableCompteRenduRepositoryDecorator.java
│   │   └── CacheableUtilisateurRepositoryDecorator.java
│   ├── event/                   # Publication d'événements Kafka
│   │   ├── KafkaEventPublisher.java
│   │   └── DomainEventPublisherAdapter.java
│   ├── security/                # Configuration sécurité OAuth2/Keycloak
│   │   ├── SecurityConfig.java
│   │   ├── SecurityContextService.java
│   │   └── KeycloakJwtConverter.java
│   └── config/                  # Configurations Spring
│       ├── RedisConfig.java
│       ├── KafkaConfig.java
│       └── KafkaTopics.java
├── src/main/resources/
│   ├── application.yml          # Configuration principale
│   ├── application-test.yml     # Configuration tests
│   └── db/migration/            # Scripts Flyway
│       └── V1__init_schema.sql
└── src/test/java/               # Tests d'intégration
    ├── BaseIntegrationTest.java
    ├── persistence/adapter/
    │   └── CompteRenduRepositoryAdapterIntegrationTest.java
    ├── cache/
    │   └── CacheIntegrationTest.java
    └── event/
        └── KafkaEventPublisherIntegrationTest.java
```

## Persistence JPA

### Entités JPA

Toutes les entités JPA sont annotées avec :
- `@Entity` et `@Table` pour le mapping
- `@Id` avec génération UUID automatique via `@GenericGenerator`
- `@PrePersist` et `@PreUpdate` pour la gestion automatique des timestamps
- Lombok (`@Getter`, `@Setter`, `@Builder`) pour réduire le boilerplate

**Exemple : CompteRenduJpaEntity**
```java
@Entity
@Table(name = "compte_rendu")
public class CompteRenduJpaEntity {
    @Id
    @GeneratedValue(generator = "UUID")
    private UUID id;

    @Column(name = "priere_seule", nullable = false)
    private Duration priereSeule;

    @Enumerated(EnumType.STRING)
    private StatutCREnum statut;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
}
```

### Repositories Spring Data JPA

Les repositories étendent `JpaRepository<T, UUID>` et définissent des méthodes de requête :
- Méthodes dérivées du nom (ex: `findByEmail`, `findByUtilisateurId`)
- Requêtes JPQL avec `@Query` pour les cas complexes
- Méthodes de comptage et d'existence

**Exemple : CompteRenduJpaRepository**
```java
@Repository
public interface CompteRenduJpaRepository extends JpaRepository<CompteRenduJpaEntity, UUID> {
    Optional<CompteRenduJpaEntity> findByUtilisateurIdAndDate(UUID utilisateurId, LocalDate date);

    @Query("SELECT cr FROM CompteRenduJpaEntity cr " +
           "WHERE cr.utilisateurId = :utilisateurId " +
           "AND cr.date BETWEEN :startDate AND :endDate")
    List<CompteRenduJpaEntity> findByUtilisateurIdAndDateBetween(...);
}
```

### Adapters

Les adapters implémentent les ports (interfaces) définis dans le module domaine :
- Utilisent les repositories JPA
- Utilisent les mappers pour convertir Domain ↔ JPA
- Décorés par les décorateurs de cache pour les performances

**Pattern : Port (Domain) → Adapter (Infrastructure) → Repository (JPA)**

```java
@Component
@RequiredArgsConstructor
public class CompteRenduRepositoryAdapter implements CompteRenduRepository {
    private final CompteRenduJpaRepository jpaRepository;
    private final CompteRenduMapper mapper;

    @Override
    public CompteRendu save(CompteRendu domain) {
        CompteRenduJpaEntity jpa = mapper.toJpaEntity(domain);
        return mapper.toDomain(jpaRepository.save(jpa));
    }
}
```

## Cache Redis

### Configuration

- **RedisConfig.java** : Configuration Lettuce, serializers JSON
- **CacheManager** configuré avec différents TTL par cache :
  - `utilisateurs` : 2 heures
  - `comptes-rendus` : 30 minutes
  - `commentaires` : 15 minutes
  - `statistiques` : 5 minutes
  - `referentiels` : 24 heures

### Décorateurs de Cache

Pattern **Decorator** appliqué aux repositories pour ajouter transparently le caching :

```java
@Component("cachedCompteRenduRepository")
public class CacheableCompteRenduRepositoryDecorator implements CompteRenduRepository {
    private final CompteRenduRepository delegate;

    @Override
    @Cacheable(value = CacheNames.COMPTES_RENDUS, key = "#id")
    public Optional<CompteRendu> findById(UUID id) {
        return delegate.findById(id);
    }

    @Override
    @CacheEvict(value = CacheNames.COMPTES_RENDUS, key = "#result.id")
    public CompteRendu save(CompteRendu cr) {
        return delegate.save(cr);
    }
}
```

**Stratégies de cache** :
- `@Cacheable` : Met en cache le résultat
- `@CacheEvict` : Invalide le cache lors des modifications
- `@Caching` : Combine plusieurs opérations de cache

## Messaging Kafka

### Configuration

- **KafkaConfig.java** : Configuration Producer avec sérialisation JSON
- **Topics créés automatiquement** :
  - `cr-events` : Événements de Compte Rendu (3 partitions)
  - `commentaire-events` : Événements de Commentaire (2 partitions)
  - `utilisateur-events` : Événements d'Utilisateur (2 partitions)
  - `notifications` : Notifications système (3 partitions)

### Publication d'Événements

**KafkaEventPublisher** publie les événements de domaine vers Kafka :

```java
@Component
public class KafkaEventPublisher {
    private final KafkaTemplate<String, DomainEvent> kafkaTemplate;

    public void publish(DomainEvent event) {
        String topic = determineTopicForEvent(event);
        String key = extractKeyFromEvent(event); // ID de l'agrégat
        kafkaTemplate.send(topic, key, event);
    }
}
```

**Garanties** :
- `acks=all` : Confirmation de tous les brokers
- `enable.idempotence=true` : Évite les doublons
- Clé de partition = ID de l'agrégat (garantit l'ordre des événements)

## Security OAuth2/Keycloak

### Configuration

- **SecurityConfig.java** : Configuration Spring Security avec OAuth2 Resource Server
- **JWT Decoder** : Validation des tokens JWT émis par Keycloak
- **CORS** : Configuration pour accepter les requêtes du frontend

### Autorisations

```java
.authorizeHttpRequests(authorize -> authorize
    // Endpoints publics
    .requestMatchers("/actuator/health").permitAll()

    // API CR - Authentification requise
    .requestMatchers("/api/v1/cr/**").authenticated()

    // Admin uniquement
    .requestMatchers(HttpMethod.DELETE, "/api/v1/utilisateurs/**").hasRole("ADMIN")

    .anyRequest().authenticated()
)
```

### Extraction des Rôles Keycloak

**KeycloakJwtConverter** extrait les rôles depuis les claims JWT :
- `realm_access.roles` : Rôles du realm
- `resource_access.{client}.roles` : Rôles du client
- Préfixe automatique `ROLE_` pour Spring Security

### Utilitaire SecurityContextService

Service pour accéder facilement au contexte de sécurité :

```java
@Service
public class SecurityContextService {
    public Optional<String> getCurrentUserEmail() { ... }
    public Optional<UUID> getCurrentUserId() { ... }
    public boolean hasRole(String role) { ... }
    public boolean isAuthenticated() { ... }
}
```

## Configuration Application

### application.yml

Configuration complète pour :
- **PostgreSQL** : Connexion via Hikari pool
- **JPA/Hibernate** : Dialect PostgreSQL, batch inserts
- **Flyway** : Migrations de base de données
- **Redis** : Connexion Lettuce avec pool
- **Kafka** : Bootstrap servers et producer config
- **OAuth2** : Issuer URI et JWK Set URI pour Keycloak
- **Actuator** : Endpoints health, metrics, prometheus

Variables d'environnement :
```yaml
spring.datasource.url: jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/${DB_NAME:cmci_cr}
spring.data.redis.host: ${REDIS_HOST:localhost}
spring.kafka.bootstrap-servers: ${KAFKA_BOOTSTRAP_SERVERS:localhost:9092}
spring.security.oauth2.resourceserver.jwt.issuer-uri: ${KEYCLOAK_ISSUER_URI:...}
```

## Tests d'Intégration

### BaseIntegrationTest

Classe de base utilisant **Testcontainers** pour :
- PostgreSQL 16
- Redis 7
- Kafka (Confluent)

```java
@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
public abstract class BaseIntegrationTest {
    static final PostgreSQLContainer<?> POSTGRES_CONTAINER = ...;
    static final GenericContainer<?> REDIS_CONTAINER = ...;
    static final KafkaContainer KAFKA_CONTAINER = ...;
}
```

### Tests Implémentés

1. **CompteRenduRepositoryAdapterIntegrationTest**
   - Tests CRUD complets
   - Tests des requêtes personnalisées
   - Tests des méthodes de comptage

2. **CacheIntegrationTest**
   - Vérification de la mise en cache
   - Vérification de l'invalidation du cache
   - Tests des différentes stratégies de cache

3. **KafkaEventPublisherIntegrationTest**
   - Publication d'événements vers Kafka
   - Vérification de la réception des événements
   - Tests du partitionnement par clé

### Exécution des Tests

```bash
# Tous les tests
mvn test

# Tests d'intégration uniquement
mvn verify -Dtest=*IntegrationTest

# Avec Docker pour Testcontainers
docker run -v /var/run/docker.sock:/var/run/docker.sock ...
```

## Dépendances Principales

```xml
<!-- Spring Boot -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.kafka</groupId>
    <artifactId>spring-kafka</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
</dependency>

<!-- Base de données -->
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
</dependency>
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>
</dependency>

<!-- Tests -->
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>postgresql</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>kafka</artifactId>
    <scope>test</scope>
</dependency>
```

## Best Practices Appliquées

1. **Séparation des Concerns** : Entities, Repositories, Adapters, Mappers séparés
2. **Immutabilité** : Utilisation de builders et objets immutables quand possible
3. **Configuration Externalisée** : Variables d'environnement pour tous les paramètres sensibles
4. **Cache Strategy** : TTL différencié selon la volatilité des données
5. **Event Sourcing** : Clés de partition basées sur l'ID d'agrégat
6. **Security by Default** : Tous les endpoints authentifiés par défaut
7. **Observability** : Actuator endpoints pour monitoring (Prometheus)
8. **Tests Complets** : Tests d'intégration avec Testcontainers

## Prochaines Étapes

- [ ] Implémenter des consumers Kafka pour les événements
- [ ] Ajouter des métriques personnalisées pour Prometheus
- [ ] Configurer le distributed tracing (Zipkin/Jaeger)
- [ ] Implémenter le circuit breaker pattern pour les appels externes
- [ ] Ajouter des tests de performance (JMeter/Gatling)
