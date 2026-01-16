# CR Domain Module

Module Domain du projet CMCI CR - Contient la logique métier pure et les entités du domaine.

## Architecture

Ce module suit les principes du **Domain-Driven Design (DDD)** et de l'**architecture hexagonale**.

## Structure

```
cr-domain/
├── model/                  # Entités et Aggregates
│   ├── CompteRendu.java
│   ├── Utilisateur.java
│   ├── Commentaire.java
│   ├── Region.java
│   ├── Zone.java
│   ├── EgliseLocale.java
│   └── EgliseMaison.java
├── valueobject/           # Value Objects
│   ├── RDQD.java
│   ├── Role.java
│   └── StatutCR.java
├── repository/            # Ports (Interfaces)
│   ├── CompteRenduRepository.java
│   ├── UtilisateurRepository.java
│   ├── CommentaireRepository.java
│   ├── RegionRepository.java
│   ├── ZoneRepository.java
│   ├── EgliseLocaleRepository.java
│   └── EgliseMaisonRepository.java
├── service/               # Domain Services
│   ├── CRDomainService.java
│   └── StatisticsService.java
└── event/                 # Domain Events
    ├── DomainEvent.java (interface)
    ├── CRCreatedEvent.java
    ├── CRUpdatedEvent.java
    ├── CRValidatedEvent.java
    ├── CommentaireAddedEvent.java
    └── UtilisateurCreatedEvent.java
```

## Entités du Domaine

### CompteRendu
**Aggregate Root** représentant un compte rendu spirituel quotidien.

**Attributs principaux:**
- `id`: UUID
- `utilisateurId`: UUID
- `date`: LocalDate
- `rdqd`: RDQD (Value Object)
- `priereSeule`: Duration
- `lectureBiblique`: Integer
- `statut`: StatutCR (BROUILLON, SOUMIS, VALIDE)

**Règles métier:**
- Un seul CR par jour par utilisateur
- Les champs RDQD, priereSeule et lectureBiblique sont obligatoires
- La date ne peut pas être dans le futur
- Modification possible dans les 7 jours ou si statut = BROUILLON
- Transitions de statut: BROUILLON → SOUMIS → VALIDE

**Méthodes métier:**
- `validate()`: Valide l'état du CR
- `isModifiable()`: Vérifie si le CR peut être modifié
- `marquerCommeVu()`: Marque le CR comme vu par le FD
- `valider()`: Valide le CR (transition vers VALIDE)
- `soumettre()`: Soumet le CR (transition vers SOUMIS)

### Utilisateur
**Aggregate Root** représentant un disciple membre de la CMCI.

**Attributs principaux:**
- `id`: UUID
- `email`: String
- `nom`, `prenom`: String
- `role`: Role (FIDELE, FD, LEADER, PASTEUR, ADMIN)
- `egliseMaisonId`: UUID
- `fdId`: UUID (référence au FD qui accompagne ce disciple)
- `statut`: StatutUtilisateur (ACTIF, INACTIF, SUSPENDU)

**Règles métier:**
- Email valide obligatoire
- Hiérarchie de responsabilité spirituelle définie par le rôle
- Un disciple (fidèle) peut avoir un FD assigné qui l'accompagne
- Permissions basées sur le rôle (RBAC)

**Note:** Tous les utilisateurs sont des disciples. Un FD est un disciple qui accompagne d'autres disciples.

**Méthodes métier:**
- `validate()`: Valide les données de l'utilisateur
- `getNomComplet()`: Retourne "Prénom Nom"
- `isActif()`: Vérifie si l'utilisateur est actif
- `hasFD()`: Vérifie si un FD est assigné
- `canViewCROf(Utilisateur)`: Vérifie les permissions de lecture de CR
- `canCommentCROf(Utilisateur)`: Vérifie les permissions de commentaire

### Commentaire
**Entity** représentant un commentaire d'un responsable sur un CR.

**Attributs principaux:**
- `id`: UUID
- `compteRenduId`: UUID
- `auteurId`: UUID
- `contenu`: String (max 5000 caractères)
- `createdAt`: LocalDateTime

**Méthodes métier:**
- `validate()`: Valide le commentaire
- `isAuthoredBy(UUID)`: Vérifie l'auteur
- `getApercu()`: Retourne les 100 premiers caractères

### Hiérarchie Organisationnelle

#### Region
Continent ou zone géographique (ex: Afrique, Europe)

#### Zone
Pays ou groupe de pays (ex: RDC, France)

#### EgliseLocale
Église locale (500-3000 fidèles), dirigée par un Pasteur

#### EgliseMaison
Église de maison (10-100 fidèles), dirigée par un Leader

## Value Objects

### RDQD
Représente le Rendez-vous Quotidien avec Dieu.

**Format:** `"accompli/attendu"` (ex: `"1/1"`, `"0/1"`)

**Méthodes:**
- `fromString(String)`: Parse depuis une chaîne
- `of(int, int)`: Création directe
- `isComplete()`: Vérifie si accompli = attendu
- `getCompletionPercentage()`: Calcule le pourcentage

### Role
Énumération des rôles dans la hiérarchie de **responsabilité spirituelle**.

**⚠️ IMPORTANT:** Tous les rôles représentent des **disciples de Jésus-Christ**. La hiérarchie reflète les niveaux de responsabilité dans l'accompagnement spirituel, pas une différence de statut spirituel. Un FD est d'abord un disciple qui amène d'autres disciples à Christ.

**Valeurs:**
- `FIDELE` (niveau 1): Disciple membre de la communauté
- `FD` (niveau 2): Faiseur de Disciples - Disciple qui accompagne 5-15 autres disciples
- `LEADER` (niveau 3): Disciple qui supervise plusieurs FD (église de maison)
- `PASTEUR` (niveau 4): Disciple qui dirige une église locale
- `ADMIN` (niveau 5): Administrateur système

**Méthodes:**
- `canSupervise(Role)`: Vérifie la supervision hiérarchique
- `canViewCROf(Role)`: Vérifie les permissions de lecture

### StatutCR
Énumération des statuts d'un Compte Rendu.

**Valeurs:**
- `BROUILLON`: En cours de rédaction
- `SOUMIS`: Soumis par le fidèle
- `VALIDE`: Validé par le FD

**Méthodes:**
- `isModifiable()`: Vérifie si modifiable
- `canBeValidated()`: Vérifie si peut être validé

## Repositories (Ports)

Les repositories définissent les **interfaces** pour la persistence. Ils sont implémentés dans le module **cr-infrastructure**.

### CompteRenduRepository
- `save(CompteRendu)`: Sauvegarde un CR
- `findById(UUID)`: Trouve par ID
- `findByUtilisateurIdAndDate(UUID, LocalDate)`: Trouve par utilisateur et date
- `findByUtilisateurIdAndDateBetween(...)`: Trouve dans une période
- `existsByUtilisateurIdAndDate(...)`: Vérifie l'existence
- `countByUtilisateurIdAndDateBetween(...)`: Compte les CR

### UtilisateurRepository
- `save(Utilisateur)`: Sauvegarde un utilisateur
- `findById(UUID)`: Trouve par ID
- `findByEmail(String)`: Trouve par email
- `findByFdId(UUID)`: Trouve les disciples d'un FD
- `findByRole(Role)`: Trouve par rôle

## Domain Services

### CRDomainService
Service contenant la logique métier complexe des CR.

**Méthodes:**
- `canCreateCR(UUID, LocalDate)`: Vérifie si création possible
- `canModifyCR(CompteRendu)`: Vérifie si modification possible
- `canViewCR(Utilisateur, CompteRendu)`: Vérifie les permissions
- `calculateRegularityRate(UUID, LocalDate, LocalDate)`: Calcule taux de régularité
- `countConsecutiveDays(UUID)`: Compte les jours consécutifs (gamification)

### StatisticsService
Service pour les statistiques et analyses.

**Méthodes:**
- `calculatePersonalStatistics(UUID, LocalDate, LocalDate)`: Stats personnelles
- `calculateGroupStatistics(List<UUID>, LocalDate, LocalDate)`: Stats de groupe

**DTOs:**
- `PersonalStatistics`: Statistiques individuelles
- `GroupStatistics`: Statistiques de groupe (FD/Leader)

## Domain Events

Les événements représentent des faits métier importants.

### CRCreatedEvent
Émis lors de la création d'un CR.

**Données:**
- `compteRenduId`, `utilisateurId`, `fdId`
- `dateCR`, `rdqd`, `statut`

### CRUpdatedEvent
Émis lors de la modification d'un CR.

### CRValidatedEvent
Émis lors de la validation d'un CR par un FD.

### CommentaireAddedEvent
Émis lors de l'ajout d'un commentaire.

### UtilisateurCreatedEvent
Émis lors de la création d'un utilisateur.

## Tests Unitaires

Le module contient des tests unitaires complets:

### Value Objects
- `RDQDTest`: 12 tests
- `RoleTest`: 11 tests
- `StatutCRTest`: 8 tests

### Entités
- `CompteRenduTest`: 19 tests
- `UtilisateurTest`: 23 tests
- `CommentaireTest`: 12 tests

**Total: 85+ tests unitaires**

## Exécution des Tests

```bash
# Tests du module domain uniquement
cd cr-domain
mvn test

# Avec rapport de couverture
mvn test jacoco:report
```

## Dépendances

- **Lombok**: Réduction du boilerplate
- **Jakarta Validation**: Annotations de validation
- **JUnit 5**: Framework de tests
- **Mockito**: Mocking pour les tests

Aucune dépendance vers Spring ou frameworks externes - **domaine pur**.

## Principes Respectés

✅ **Domain-Driven Design (DDD)**
- Entités riches avec comportement
- Value Objects immuables
- Aggregate Roots
- Domain Services pour logique complexe
- Domain Events

✅ **Architecture Hexagonale**
- Domaine indépendant de l'infrastructure
- Ports (interfaces) pour les repositories
- Pas de dépendances vers frameworks externes

✅ **SOLID Principles**
- Single Responsibility
- Open/Closed
- Liskov Substitution
- Interface Segregation
- Dependency Inversion

✅ **Clean Code**
- Noms explicites
- Méthodes courtes et focalisées
- Validation stricte
- Tests unitaires complets
- Documentation JavaDoc

## Prochaines Étapes

Le module domain est complet. Les étapes suivantes sont:

1. **cr-application**: Use Cases et orchestration
2. **cr-infrastructure**: Implémentation des repositories (JPA)
3. **cr-api**: Contrôleurs REST et DTOs API

## Contact

Pour toute question sur le module domain:
- Architecture: Voir le cahier des charges (section 7.2)
- Modèle de données: Voir l'Annexe B du cahier des charges
