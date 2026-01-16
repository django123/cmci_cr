# CR Application Module

Module Application du projet CMCI CR - Contient les Use Cases, Commands, DTOs et la logique d'orchestration.

## Architecture

Ce module implémente la **couche Application** de l'architecture hexagonale. Il orchestre les entités du domaine et expose les cas d'usage aux couches supérieures (API).

## Structure

```
cr-application/
├── dto/                    # Data Transfer Objects
│   ├── CreateCRCommand.java
│   ├── UpdateCRCommand.java
│   ├── CRResponse.java
│   ├── AddCommentaireCommand.java
│   ├── CommentaireResponse.java
│   ├── DiscipleWithCRStatusResponse.java
│   ├── PersonalStatisticsResponse.java
│   └── GroupStatisticsResponse.java
├── usecase/               # Use Cases (Business Logic)
│   ├── CreateCRUseCase.java
│   ├── UpdateCRUseCase.java
│   ├── GetCRUseCase.java
│   ├── DeleteCRUseCase.java
│   ├── ValidateCRUseCase.java
│   ├── MarkCRAsViewedUseCase.java
│   ├── AddCommentaireUseCase.java
│   ├── GetCommentairesUseCase.java
│   ├── ViewDisciplesCRUseCase.java
│   ├── GetPersonalStatisticsUseCase.java
│   └── GetGroupStatisticsUseCase.java
└── mapper/                # Mappers (Domain ↔ DTO)
    └── (À implémenter si nécessaire)
```

## Use Cases Implémentés

### Epic 2: Gestion des CR

#### CreateCRUseCase (US2.1)
Crée un nouveau Compte Rendu quotidien.

**Règles métier:**
- ✅ Un seul CR par jour par utilisateur
- ✅ Champs obligatoires: RDQD, priereSeule, lectureBiblique
- ✅ Date ne peut pas être dans le futur
- ✅ Statut initial: SOUMIS
- ✅ Validation automatique des données

**Input:** `CreateCRCommand`
**Output:** `CRResponse`

```java
CreateCRCommand command = CreateCRCommand.builder()
    .utilisateurId(userId)
    .date(LocalDate.now())
    .rdqd("1/1")
    .priereSeule("01:30")
    .lectureBiblique(5)
    .livreBiblique("Psaumes")
    .build();

CRResponse response = createCRUseCase.execute(command);
```

#### UpdateCRUseCase (US2.2)
Modifie un CR existant.

**Règles métier:**
- ✅ Seul le propriétaire peut modifier
- ✅ Modification possible si BROUILLON ou dans les 7 jours
- ✅ Validation via `CRDomainService.canModifyCR()`
- ✅ Mise à jour du timestamp `updatedAt`

**Input:** `UpdateCRCommand`
**Output:** `CRResponse`

#### GetCRUseCase (US2.3)
Consulte les Comptes Rendus.

**Méthodes:**
- `getById(UUID)`: Récupère un CR par ID
- `getByUtilisateurId(UUID)`: Tous les CR d'un utilisateur
- `getByUtilisateurIdAndDateRange(UUID, LocalDate, LocalDate)`: CR sur une période
- `getByUtilisateurIdAndDate(UUID, LocalDate)`: CR d'une date spécifique
- `getUnviewedByUtilisateurId(UUID)`: CR non vus par le FD

**Output:** `CRResponse` ou `List<CRResponse>`

#### DeleteCRUseCase (US2.4)
Supprime un CR (soft delete).

**Règles métier:**
- ✅ Seul le propriétaire peut supprimer
- ✅ Mêmes règles de modification (7 jours ou BROUILLON)
- ✅ Soft delete dans l'infrastructure

**Input:** `UUID id, UUID utilisateurId`

#### ValidateCRUseCase (US2.5)
Valide un CR (par un FD/Leader/Pasteur).

**Règles métier:**
- ✅ Seuls les CR au statut SOUMIS peuvent être validés
- ✅ Transition: SOUMIS → VALIDE
- ✅ Marque automatiquement comme `vuParFd = true`
- ✅ Émet un événement `CRValidatedEvent`

**Input:** `UUID crId, UUID validatorId`
**Output:** `CRResponse`

#### MarkCRAsViewedUseCase (US3.3)
Marque un CR comme "vu" par le FD.

**Règles métier:**
- ✅ Seuls les CR SOUMIS peuvent être marqués comme vus
- ✅ Change `vuParFd` à `true`

**Input:** `UUID crId, UUID fdId`
**Output:** `CRResponse`

### Epic 3: Supervision Spirituelle

#### ViewDisciplesCRUseCase (US3.1)
Tableau de bord FD - Vue des disciples avec leur statut de CR.

**Fonctionnalités:**
- ✅ Liste tous les disciples d'un FD
- ✅ Calcule le statut CR de chaque disciple:
  - CR soumis aujourd'hui?
  - Date du dernier CR
  - Jours depuis le dernier CR
  - Taux de régularité sur 30 jours
- ✅ Indicateurs d'alerte:
  - WARNING: 3-7 jours sans CR
  - CRITICAL: >7 jours sans CR

**Input:** `UUID fdId`
**Output:** `List<DiscipleWithCRStatusResponse>`

```java
List<DiscipleWithCRStatusResponse> disciples = viewDisciplesCRUseCase.execute(fdId);

disciples.forEach(d -> {
    System.out.println(d.getNomComplet());
    System.out.println("Dernier CR: " + d.getDernierCRDate());
    System.out.println("Taux régularité: " + d.getTauxRegularite30j() + "%");
    if (d.getAlerte()) {
        System.out.println("⚠️ Alerte: " + d.getNiveauAlerte());
    }
});
```

#### AddCommentaireUseCase (US3.2)
Ajoute un commentaire sur un CR.

**Règles métier:**
- ✅ Le CR doit exister
- ✅ Contenu limité à 5000 caractères
- ✅ Émet un événement `CommentaireAddedEvent` pour notification

**Input:** `AddCommentaireCommand`
**Output:** `CommentaireResponse`

#### GetCommentairesUseCase
Consulte les commentaires.

**Méthodes:**
- `getByCompteRenduId(UUID)`: Tous les commentaires d'un CR
- `getByAuteurId(UUID)`: Tous les commentaires d'un auteur
- `countByCompteRenduId(UUID)`: Nombre de commentaires sur un CR

### Epic 4: Statistiques et Rapports

#### GetPersonalStatisticsUseCase (US4.1)
Dashboard personnel - Statistiques individuelles.

**Métriques calculées:**
- Nombre total de CR
- Taux de régularité (%)
- RDQD: nombre complet et taux (%)
- Prière: durée totale et moyenne
- Lecture biblique: total chapitres et moyenne/jour
- Évangélisation: total personnes contactées
- Pratiques: nombre de confessions et jeûnes

**Input:** `UUID utilisateurId, LocalDate startDate, LocalDate endDate`
**Output:** `PersonalStatisticsResponse`

```java
PersonalStatisticsResponse stats = getPersonalStatisticsUseCase.execute(
    userId,
    LocalDate.now().minusDays(30),
    LocalDate.now()
);

System.out.println("Taux régularité: " + stats.getTauxRegularite() + "%");
System.out.println("Temps prière total: " + stats.getDureeTotalePriere());
System.out.println("Chapitres lus: " + stats.getTotalChapitresLus());
```

#### GetGroupStatisticsUseCase (US4.2)
Dashboard FD/Leader - Statistiques de groupe.

**Métriques calculées:**
- Nombre de membres
- CR soumis aujourd'hui (nombre et %)
- Total CR sur la période
- Taux de régularité du groupe (%)
- Temps de prière: total et moyenne/membre
- Membres avec alerte (≥3 jours sans CR)
- Membres inactifs (≥7 jours sans CR)

**Input:** `UUID fdId, LocalDate startDate, LocalDate endDate`
**Output:** `GroupStatisticsResponse`

## DTOs (Data Transfer Objects)

### Commands (Input)

#### CreateCRCommand
```java
@Value
@Builder
public class CreateCRCommand {
    @NotNull UUID utilisateurId;
    @NotNull @PastOrPresent LocalDate date;
    @NotNull @Pattern(regexp = "^\\d+/\\d+$") String rdqd;
    @NotNull String priereSeule; // "HH:mm" ou ISO
    @NotNull @PositiveOrZero Integer lectureBiblique;
    // ... autres champs optionnels
}
```

#### UpdateCRCommand
Similaire à `CreateCRCommand` mais tous les champs (sauf `id` et `utilisateurId`) sont optionnels.

#### AddCommentaireCommand
```java
@Value
@Builder
public class AddCommentaireCommand {
    @NotNull UUID compteRenduId;
    @NotNull UUID auteurId;
    @NotBlank @Size(max = 5000) String contenu;
}
```

### Responses (Output)

#### CRResponse
```java
@Value
@Builder
public class CRResponse {
    UUID id;
    UUID utilisateurId;
    LocalDate date;
    String rdqd;
    String priereSeule; // Format "HH:mm"
    Integer lectureBiblique;
    String livreBiblique;
    // ... tous les champs du CR
    String statut; // BROUILLON, SOUMIS, VALIDE
    Boolean vuParFd;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
```

#### DiscipleWithCRStatusResponse
```java
@Value
@Builder
public class DiscipleWithCRStatusResponse {
    UUID discipleId;
    String nomComplet;
    LocalDate dernierCRDate;
    Boolean crAujourdhui;
    Integer joursDepuisDernierCR;
    Double tauxRegularite30j;
    Boolean alerte;
    String niveauAlerte; // NONE, WARNING, CRITICAL
}
```

## Tests Unitaires

Le module contient des tests unitaires complets pour tous les Use Cases:

### CreateCRUseCaseTest
- ✅ Création réussie d'un CR
- ✅ Exception si CR existe déjà pour la date
- ✅ Gestion des champs optionnels
- ✅ Support format durée ISO et "HH:mm"
- ✅ Valeurs par défaut pour booléens

### UpdateCRUseCaseTest
- ✅ Mise à jour réussie
- ✅ Exception si CR non trouvé
- ✅ Exception si utilisateur non propriétaire
- ✅ Exception si CR non modifiable
- ✅ Mise à jour partielle (seulement certains champs)

### GetCRUseCaseTest
- ✅ Récupération par ID
- ✅ Récupération par utilisateur
- ✅ Récupération par période
- ✅ Récupération CR non vus
- ✅ Exceptions appropriées

**Total: 30+ tests unitaires**

## Exécution des Tests

```bash
# Tests du module application uniquement
cd cr-application
mvn test

# Avec rapport de couverture
mvn test jacoco:report
```

## Dépendances

- **cr-domain**: Entités et logique métier
- **Lombok**: Réduction du boilerplate
- **MapStruct**: Mapping DTO ↔ Domain (à implémenter)
- **Jakarta Validation**: Validation des commandes
- **JUnit 5 + Mockito**: Tests unitaires

## Principes de Design

✅ **CQRS Léger**
- Commands pour les opérations d'écriture
- Queries via les repositories

✅ **Use Case = Transaction**
- Chaque use case est une unité transactionnelle
- Orchestration des entités du domaine

✅ **Validation en Couches**
- Validation syntaxique: Annotations Jakarta (`@NotNull`, `@Pattern`)
- Validation métier: Domain Services (`CRDomainService`)
- Validation entité: Méthode `validate()` sur les entités

✅ **Séparation des Préoccupations**
- DTOs pour le transfert de données
- Use Cases pour la logique applicative
- Domain Services pour la logique métier complexe

✅ **Testabilité**
- Use Cases testables avec des mocks
- Pas de dépendances vers l'infrastructure
- Tests unitaires rapides

## Prochaines Étapes

Le module **cr-application** est maintenant complet pour le MVP. Les prochaines implémentations:

1. **cr-infrastructure**
   - Implémentation JPA des repositories
   - Configuration Redis pour le cache
   - Event listeners Kafka

2. **cr-api**
   - Contrôleurs REST
   - Mapping Request → Command
   - Gestion des exceptions
   - Documentation OpenAPI

3. **Améliorations futures**
   - Pagination pour les listes
   - Tri et filtres avancés
   - Export PDF/Excel (US4.4)
   - Rapports programmés (US4.5)

## Utilisation dans l'API

```java
// Dans un contrôleur REST
@RestController
@RequestMapping("/api/v1/cr")
public class CRController {

    private final CreateCRUseCase createCRUseCase;

    @PostMapping
    public ResponseEntity<CRResponse> createCR(@RequestBody CreateCRRequest request) {
        CreateCRCommand command = mapToCommand(request);
        CRResponse response = createCRUseCase.execute(command);
        return ResponseEntity.status(201).body(response);
    }
}
```

## Contact

Pour toute question sur le module application:
- Architecture: Voir le cahier des charges
- Use Cases: Voir les User Stories (section 5)

---

**Module cr-application - ✅ Complet pour MVP**
