# Module cr-api

Module API REST exposant les fonctionnalités du système CMCI Compte Rendu via HTTP.

## Responsabilités

Ce module contient toutes les API REST publiques :
- **REST Controllers** : Endpoints HTTP pour les clients
- **DTOs API** : Requêtes et réponses spécifiques à l'API
- **Exception Handling** : Gestion centralisée des erreurs
- **OpenAPI/Swagger** : Documentation interactive de l'API
- **Validation** : Validation des entrées utilisateur

## Structure du Module

```
cr-api/
├── src/main/java/com/cmci/cr/api/
│   ├── controller/              # REST Controllers
│   │   ├── CompteRenduController.java
│   │   ├── CommentaireController.java
│   │   └── StatisticsController.java
│   ├── dto/
│   │   ├── request/            # DTOs de requête
│   │   │   ├── CreateCompteRenduRequest.java
│   │   │   ├── UpdateCompteRenduRequest.java
│   │   │   └── AddCommentaireRequest.java
│   │   └── response/           # DTOs de réponse
│   │       ├── CompteRenduResponse.java
│   │       ├── CommentaireResponse.java
│   │       ├── StatisticsResponse.java
│   │       ├── ErrorResponse.java
│   │       └── PageResponse.java
│   ├── mapper/                 # Mappers API <-> Application
│   │   ├── CompteRenduApiMapper.java
│   │   ├── CommentaireApiMapper.java
│   │   └── StatisticsApiMapper.java
│   ├── exception/              # Gestion des erreurs
│   │   └── GlobalExceptionHandler.java
│   └── config/                 # Configuration
│       ├── OpenApiConfig.java
│       └── WebConfig.java
├── src/main/resources/
│   └── application-api.yml
└── src/test/java/              # Tests unitaires
    ├── controller/
    │   ├── CompteRenduControllerTest.java
    │   ├── CommentaireControllerTest.java
    │   └── StatisticsControllerTest.java
    └── exception/
        └── GlobalExceptionHandlerTest.java
```

## REST Controllers

### CompteRenduController

Gestion complète des comptes rendus quotidiens.

**Base URL**: `/api/v1/cr`

#### Endpoints

| Méthode | Endpoint | Description | Auth |
|---------|----------|-------------|------|
| POST | `/` | Créer un compte rendu | ✓ |
| GET | `/{id}` | Récupérer un CR par ID | ✓ |
| PUT | `/{id}` | Mettre à jour un CR | ✓ |
| DELETE | `/{id}` | Supprimer un CR | ✓ |
| GET | `/user/{utilisateurId}` | Liste des CR d'un utilisateur | ✓ |
| GET | `/user/{utilisateurId}/period` | CR sur une période | ✓ |
| POST | `/{id}/submit` | Soumettre un CR | ✓ |
| POST | `/{id}/validate` | Valider un CR | FD+ |
| POST | `/{id}/mark-viewed` | Marquer comme vu | FD+ |

**Exemple de requête - Créer un CR**:
```bash
curl -X POST http://localhost:8081/api/v1/cr \
  -H "Authorization: Bearer <JWT_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "date": "2025-01-09",
    "rdqd": "5/7",
    "priereSeuleMinutes": 30,
    "priereCoupleMinutes": 15,
    "priereAvecEnfantsMinutes": 10,
    "tempsEtudeParoleMinutes": 45,
    "nombreContactsUtiles": 3,
    "invitationsCulte": 2,
    "offrande": 5000,
    "evangelisations": 1,
    "commentaire": "Journée bénie!"
  }'
```

**Réponse (201 Created)**:
```json
{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "utilisateurId": "223e4567-e89b-12d3-a456-426614174001",
  "date": "2025-01-09",
  "rdqd": "5/7",
  "priereSeuleMinutes": 30,
  "statut": "BROUILLON",
  "vuParFd": false,
  "createdAt": "2025-01-09T10:30:00",
  "updatedAt": "2025-01-09T10:30:00"
}
```

### CommentaireController

Gestion des commentaires sur les comptes rendus.

**Base URL**: `/api/v1/cr/{compteRenduId}/commentaires`

#### Endpoints

| Méthode | Endpoint | Description | Auth |
|---------|----------|-------------|------|
| POST | `/` | Ajouter un commentaire | ✓ |
| GET | `/` | Liste des commentaires | ✓ |

**Exemple - Ajouter un commentaire**:
```bash
curl -X POST http://localhost:8081/api/v1/cr/{crId}/commentaires \
  -H "Authorization: Bearer <JWT_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "contenu": "Excellent travail, continue comme ça!"
  }'
```

### StatisticsController

Consultation des statistiques et rapports.

**Base URL**: `/api/v1/statistics`

#### Endpoints

| Méthode | Endpoint | Description | Auth |
|---------|----------|-------------|------|
| GET | `/personal?startDate={date}&endDate={date}` | Stats personnelles | ✓ |
| GET | `/user/{utilisateurId}?startDate={date}&endDate={date}` | Stats d'un utilisateur | FD+ |

**Exemple - Statistiques personnelles**:
```bash
curl -X GET "http://localhost:8081/api/v1/statistics/personal?startDate=2025-01-01&endDate=2025-01-09" \
  -H "Authorization: Bearer <JWT_TOKEN>"
```

**Réponse**:
```json
{
  "startDate": "2025-01-01",
  "endDate": "2025-01-09",
  "totalCRSoumis": 7,
  "totalCRValides": 6,
  "tauxCompletion": 85.71,
  "totalRDQDAccomplis": 35,
  "totalRDQDAttendus": 49,
  "moyenneRDQD": 5.0,
  "totalPriereSeuleMinutes": 210,
  "totalContactsUtiles": 21,
  "totalOffrandes": 35000,
  "totalEvangelisations": 7
}
```

## DTOs de Requête

### CreateCompteRenduRequest

```java
{
  "date": "2025-01-09",              // @NotNull, @PastOrPresent
  "rdqd": "5/7",                     // @NotNull, @Pattern("\\d+/\\d+")
  "priereSeuleMinutes": 30,          // @NotNull, @Min(0)
  "priereCoupleMinutes": 15,         // @Min(0), optionnel
  "priereAvecEnfantsMinutes": 10,    // @Min(0), optionnel
  "tempsEtudeParoleMinutes": 45,     // @NotNull, @Min(0)
  "nombreContactsUtiles": 3,         // @NotNull, @Min(0)
  "invitationsCulte": 2,             // @NotNull, @Min(0)
  "offrande": 5000,                  // @NotNull, @DecimalMin("0.0")
  "evangelisations": 1,              // @NotNull, @Min(0)
  "commentaire": "..."               // @Size(max=1000), optionnel
}
```

### UpdateCompteRenduRequest

Tous les champs sont optionnels, seuls les champs fournis seront mis à jour.

### AddCommentaireRequest

```java
{
  "contenu": "Excellent travail!"    // @NotBlank, @Size(min=1, max=2000)
}
```

## DTOs de Réponse

### CompteRenduResponse

```java
{
  "id": "uuid",
  "utilisateurId": "uuid",
  "date": "2025-01-09",
  "rdqd": "5/7",
  "priereSeuleMinutes": 30,
  "priereCoupleMinutes": 15,
  "priereAvecEnfantsMinutes": 10,
  "tempsEtudeParoleMinutes": 45,
  "nombreContactsUtiles": 3,
  "invitationsCulte": 2,
  "offrande": 5000,
  "evangelisations": 1,
  "commentaire": "...",
  "statut": "BROUILLON|SOUMIS|VALIDE",
  "vuParFd": false,
  "createdAt": "2025-01-09T10:30:00",
  "updatedAt": "2025-01-09T10:30:00"
}
```

### ErrorResponse

```java
{
  "timestamp": "2025-01-09T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Les données fournies ne sont pas valides",
  "path": "/api/v1/cr",
  "validationErrors": [
    {
      "field": "date",
      "message": "La date est obligatoire",
      "rejectedValue": null
    }
  ]
}
```

## Gestion des Erreurs

Le `GlobalExceptionHandler` gère tous les types d'exceptions et retourne des réponses HTTP appropriées :

| Exception | Status Code | Description |
|-----------|-------------|-------------|
| `MethodArgumentNotValidException` | 400 | Validation échouée |
| `IllegalArgumentException` | 400 | Règle métier violée |
| `IllegalStateException` | 409 | État invalide |
| `NoSuchElementException` | 404 | Ressource non trouvée |
| `AccessDeniedException` | 403 | Accès refusé |
| `InsufficientAuthenticationException` | 401 | Non authentifié |
| `Exception` (générique) | 500 | Erreur serveur |

**Exemple de validation échouée** :
```json
{
  "timestamp": "2025-01-09T10:30:00",
  "status": 400,
  "error": "Validation Failed",
  "message": "Les données fournies ne sont pas valides",
  "path": "/api/v1/cr",
  "validationErrors": [
    {
      "field": "priereSeuleMinutes",
      "message": "Le temps de prière doit être positif",
      "rejectedValue": -5
    }
  ]
}
```

## Validation

Validation automatique via **Bean Validation (JSR-380)** :

- `@NotNull` : Champ obligatoire
- `@NotBlank` : Chaîne non vide
- `@Size(min, max)` : Longueur de chaîne
- `@Min(value)` : Valeur minimale
- `@DecimalMin(value)` : Valeur décimale minimale
- `@PastOrPresent` : Date passée ou présente
- `@Pattern(regexp)` : Expression régulière

## Sécurité

### Authentification JWT

Tous les endpoints nécessitent un JWT valide dans le header `Authorization` :

```
Authorization: Bearer <JWT_TOKEN>
```

Le JWT est obtenu via Keycloak OAuth2.

### Autorisations par Rôle

| Endpoint | Rôles autorisés |
|----------|----------------|
| Créer/Modifier son propre CR | Tous (FIDELE+) |
| Voir les CR de ses disciples | FD, LEADER, PASTEUR, ADMIN |
| Valider un CR | FD, LEADER, PASTEUR, ADMIN |
| Supprimer un utilisateur | ADMIN uniquement |

**Utilisation de `@PreAuthorize`** :
```java
@PreAuthorize("hasAnyRole('FD', 'LEADER', 'PASTEUR', 'ADMIN')")
public ResponseEntity<Void> validateCompteRendu(@PathVariable UUID id) {
    // ...
}
```

## Documentation OpenAPI/Swagger

### Configuration

- **Swagger UI** : `http://localhost:8081/swagger-ui.html`
- **OpenAPI JSON** : `http://localhost:8081/v3/api-docs`

### Annotations Utilisées

```java
@Tag(name = "Comptes Rendus", description = "API de gestion des comptes rendus")
@Operation(summary = "Créer un compte rendu", description = "...")
@ApiResponses({
    @ApiResponse(responseCode = "201", description = "CR créé"),
    @ApiResponse(responseCode = "400", description = "Données invalides")
})
@SecurityRequirement(name = "Bearer Authentication")
```

### Accès à Swagger UI

1. Démarrer l'application
2. Ouvrir `http://localhost:8081/swagger-ui.html`
3. Cliquer sur "Authorize"
4. Entrer le JWT : `Bearer <votre-token>`
5. Tester les endpoints directement depuis l'interface

## Tests

### Tests Unitaires des Controllers

Utilisation de `@WebMvcTest` et `MockMvc` :

```java
@WebMvcTest(CompteRenduController.class)
class CompteRenduControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CreateCRUseCase createCRUseCase;

    @Test
    @WithMockUser(roles = "FIDELE")
    void shouldCreateCompteRendu() throws Exception {
        mockMvc.perform(post("/api/v1/cr")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
            .andExpect(status().isCreated());
    }
}
```

### Exécution des Tests

```bash
# Tous les tests
mvn test

# Tests d'un controller spécifique
mvn test -Dtest=CompteRenduControllerTest

# Avec couverture de code
mvn test jacoco:report
```

## Configuration CORS

Configuration dans `WebConfig.java` :

```java
@Override
public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping("/api/**")
            .allowedOrigins("http://localhost:3000")
            .allowedMethods("GET", "POST", "PUT", "DELETE")
            .allowedHeaders("*")
            .allowCredentials(true);
}
```

## Bonnes Pratiques Appliquées

1. **RESTful Design** : Utilisation correcte des méthodes HTTP et codes de statut
2. **Validation Stricte** : Toutes les entrées sont validées
3. **Gestion d'Erreurs Cohérente** : Format de réponse uniforme pour les erreurs
4. **Documentation Complète** : OpenAPI/Swagger pour tous les endpoints
5. **Sécurité par Défaut** : Tous les endpoints authentifiés
6. **Séparation des Concerns** : DTOs API séparés des DTOs Application
7. **Tests Complets** : Couverture des controllers et exception handler
8. **Logging** : Toutes les opérations importantes sont loggées

## Codes de Statut HTTP

| Code | Signification | Utilisation |
|------|---------------|-------------|
| 200 | OK | Requête réussie (GET, PUT) |
| 201 | Created | Ressource créée (POST) |
| 204 | No Content | Suppression réussie (DELETE) |
| 400 | Bad Request | Validation échouée |
| 401 | Unauthorized | Non authentifié |
| 403 | Forbidden | Permissions insuffisantes |
| 404 | Not Found | Ressource non trouvée |
| 409 | Conflict | État invalide |
| 500 | Internal Server Error | Erreur serveur |

## Exemple Complet : Cycle de Vie d'un CR

### 1. Création (BROUILLON)
```bash
POST /api/v1/cr
→ 201 Created
```

### 2. Modification
```bash
PUT /api/v1/cr/{id}
→ 200 OK
```

### 3. Soumission
```bash
POST /api/v1/cr/{id}/submit
→ 200 OK (statut = SOUMIS)
```

### 4. Validation par FD
```bash
POST /api/v1/cr/{id}/validate
→ 200 OK (statut = VALIDE)
```

### 5. Ajout de commentaire
```bash
POST /api/v1/cr/{id}/commentaires
→ 201 Created
```

### 6. Consultation des statistiques
```bash
GET /api/v1/statistics/personal?startDate=...&endDate=...
→ 200 OK
```

## Intégration avec les Autres Modules

```
cr-api (Controllers)
  ↓ utilise
cr-application (Use Cases)
  ↓ utilise
cr-domain (Entités, Règles Métier)
  ↓ implémenté par
cr-infrastructure (Persistence, Cache, Kafka)
```

**Pattern** : Controllers → Use Cases → Domain Services → Repositories

## Variables d'Environnement

```yaml
# application-api.yml
app:
  api:
    version: ${API_VERSION:1.0.0}
    title: ${API_TITLE:CMCI Compte Rendu API}

server:
  port: ${SERVER_PORT:8081}

springdoc:
  swagger-ui:
    enabled: ${SWAGGER_ENABLED:true}
```

## Prochaines Étapes

- [ ] Ajouter un controller pour la gestion des utilisateurs
- [ ] Implémenter la pagination pour les listes
- [ ] Ajouter des filtres de recherche avancés
- [ ] Implémenter le rate limiting
- [ ] Ajouter des tests d'intégration end-to-end
- [ ] Implémenter le versioning de l'API (v2)
- [ ] Ajouter le support de GraphQL
