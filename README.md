# CMCI CR - Backend Service

Plateforme de gestion des Comptes Rendus spirituels pour la Communauté Missionnaire Chrétienne Internationale (CMCI).

## Table des Matières

- [Vue d'ensemble](#vue-densemble)
- [Architecture](#architecture)
- [Prérequis](#prérequis)
- [Installation](#installation)
- [Configuration](#configuration)
- [Démarrage](#démarrage)
- [Tests](#tests)
- [Documentation API](#documentation-api)
- [Structure du Projet](#structure-du-projet)
- [Technologies](#technologies)
- [Contribution](#contribution)

## Vue d'ensemble

CMCI CR est une plateforme permettant aux fidèles de saisir quotidiennement leurs pratiques spirituelles et aux responsables (FD, Leaders, Pasteurs) de suivre la progression spirituelle de leurs disciples.

### Fonctionnalités principales

- ✅ Création et gestion des comptes rendus spirituels quotidiens
- ✅ Suivi hiérarchique par les responsables spirituels
- ✅ Statistiques et tableaux de bord personnalisés
- ✅ Authentification et autorisation sécurisées (OAuth2/OIDC)
- ✅ API REST documentée avec OpenAPI
- ✅ Architecture hexagonale pour une meilleure maintenabilité

### Note Théologique

**Tous les utilisateurs sont des disciples de Jésus-Christ.** La terminologie utilisée (Fidèle, FD, Leader, Pasteur) représente des niveaux de **responsabilité** dans l'accompagnement spirituel, pas une hiérarchie de statut spirituel. Un Faiseur de Disciple (FD) est d'abord un disciple qui amène d'autres disciples à Christ.

## Architecture

Le projet suit une **architecture hexagonale (ports & adapters)** avec une séparation en modules Maven :

```
cmci-cr-backend/
├── cr-domain/           # Entités métier, value objects, ports
├── cr-application/      # Use cases, commandes, DTOs
├── cr-infrastructure/   # Implémentations (JPA, Redis, Kafka)
├── cr-api/              # Contrôleurs REST, exception handlers
└── cr-bootstrap/        # Point d'entrée de l'application
```

### Stack Technique

| Composant | Technologie | Version |
|-----------|-------------|---------|
| Runtime | Java | 21 LTS |
| Framework | Spring Boot | 3.3.x |
| Build | Maven | 3.9.x |
| Base de données | PostgreSQL | 16 |
| Cache | Redis | 7.x |
| Messaging | Apache Kafka | 3.6.x |
| Authentification | Keycloak | 24.x |
| Monitoring | Prometheus + Grafana | Latest |

## Prérequis

- **Java 21 LTS** ou supérieur
- **Maven 3.9+**
- **Docker & Docker Compose** (pour les dépendances)
- **Git**

## Installation

### 1. Cloner le repository

```bash
git clone <repository-url>
cd cmci-cr-backend
```

### 2. Copier le fichier d'environnement

```bash
cp .env.example .env
```

Modifier les valeurs si nécessaire.

### 3. Démarrer les dépendances avec Docker Compose

```bash
docker-compose up -d postgres redis kafka keycloak
```

Cela démarre :
- PostgreSQL (port 5432)
- Redis (port 6379)
- Kafka (port 9092)
- Keycloak (port 8180)

### 4. Attendre que les services soient prêts

```bash
# Vérifier la santé de PostgreSQL
docker-compose ps postgres

# Accéder à Keycloak
open http://localhost:8180
```

## Configuration

### Variables d'environnement

Les principales variables sont définies dans `.env` :

```env
DB_PASSWORD=cmci_password
KEYCLOAK_ISSUER_URI=http://localhost:8180/realms/cmci
KEYCLOAK_ADMIN_PASSWORD=admin123
```

### Configuration de l'application

Le fichier `cr-bootstrap/src/main/resources/application.yml` contient la configuration Spring Boot.

Pour une configuration locale spécifique, créer `application-local.yml` (ignoré par Git).

## Démarrage

### Mode développement (local)

```bash
# Compiler le projet
mvn clean install

# Démarrer l'application
cd cr-bootstrap
mvn spring-boot:run
```

L'application démarre sur **http://localhost:8081/api**

### Avec Docker

```bash
# Construire l'image
docker build -t cmci-cr-service .

# Démarrer le conteneur
docker run -p 8081:8081 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/cmci_cr \
  cmci-cr-service
```

### Avec Docker Compose (stack complète)

```bash
docker-compose up --build
```

## Tests

### Tests unitaires

```bash
mvn test
```

### Tests d'intégration

```bash
mvn verify
```

### Tests avec couverture

```bash
mvn clean test jacoco:report
```

Le rapport de couverture est généré dans `target/site/jacoco/index.html`

## Documentation API

### Swagger UI

Une fois l'application démarrée, accéder à la documentation interactive :

**http://localhost:8081/api/swagger-ui.html**

### OpenAPI JSON

Le schéma OpenAPI est disponible à :

**http://localhost:8081/api/v3/api-docs**

### Endpoints principaux

| Endpoint | Méthode | Description |
|----------|---------|-------------|
| `/api/v1/cr` | POST | Créer un nouveau CR |
| `/api/v1/cr` | GET | Liste des CR |
| `/api/v1/cr/{id}` | GET | Détail d'un CR |
| `/api/v1/cr/{id}` | PUT | Modifier un CR |
| `/api/v1/cr/{id}` | DELETE | Supprimer un CR |

### Health Check

```bash
curl http://localhost:8081/api/actuator/health
```

### Métriques Prometheus

```bash
curl http://localhost:8081/api/actuator/prometheus
```

## Structure du Projet

```
cmci-cr-backend/
├── .github/
│   └── workflows/
│       └── ci-cd.yml              # Pipeline CI/CD GitHub Actions
├── cr-domain/                     # Module Domain
│   └── src/main/java/com/cmci/cr/domain/
│       ├── model/                 # Entités (CompteRendu, Utilisateur)
│       ├── valueobject/           # Value Objects (RDQD, Role, StatutCR)
│       ├── repository/            # Ports (interfaces)
│       ├── service/               # Services métier
│       └── event/                 # Événements domaine
├── cr-application/                # Module Application
│   └── src/main/java/com/cmci/cr/application/
│       ├── usecase/               # Use Cases
│       ├── dto/                   # DTOs (commandes, réponses)
│       └── mapper/                # Mappers
├── cr-infrastructure/             # Module Infrastructure
│   └── src/main/java/com/cmci/cr/infrastructure/
│       ├── persistence/           # Implémentation JPA
│       │   ├── entity/            # Entités JPA
│       │   ├── repository/        # Repositories JPA
│       │   └── adapter/           # Adapters
│       ├── config/                # Configurations
│       └── security/              # Configuration sécurité
├── cr-api/                        # Module API
│   └── src/main/java/com/cmci/cr/api/
│       ├── rest/                  # Contrôleurs REST
│       ├── dto/                   # DTOs API
│       └── exception/             # Gestion des exceptions
├── cr-bootstrap/                  # Module Bootstrap
│   ├── src/main/java/com/cmci/cr/
│   │   └── CRServiceApplication.java  # Point d'entrée
│   └── src/main/resources/
│       ├── application.yml        # Configuration
│       └── db/migration/          # Scripts Flyway
├── monitoring/
│   └── prometheus.yml             # Configuration Prometheus
├── docker-compose.yml             # Orchestration Docker
├── Dockerfile                     # Image Docker application
├── .env.example                   # Variables d'environnement
├── .gitignore                     # Fichiers ignorés par Git
├── pom.xml                        # POM parent
└── README.md                      # Ce fichier
```

## Monitoring

### Prometheus

Accéder à Prometheus : **http://localhost:9090**

### Grafana

Accéder à Grafana : **http://localhost:3000**

Credentials par défaut :
- Username: `admin`
- Password: `admin123` (configurable via `.env`)

## Base de données

### Migrations Flyway

Les migrations sont dans `cr-infrastructure/src/main/resources/db/migration/`

Flyway applique automatiquement les migrations au démarrage.

### Accéder à la base de données

```bash
docker exec -it cmci-postgres psql -U cmci -d cmci_cr
```

### Commandes SQL utiles

```sql
-- Lister les tables
\dt

-- Voir la structure d'une table
\d compte_rendu

-- Statistiques
SELECT COUNT(*) FROM compte_rendu;
SELECT role, COUNT(*) FROM utilisateur GROUP BY role;
```

## Contribution

### Workflow Git

1. Créer une branche feature : `git checkout -b feature/ma-fonctionnalite`
2. Commiter les changements : `git commit -am 'Ajout de ma fonctionnalité'`
3. Pousser la branche : `git push origin feature/ma-fonctionnalite`
4. Créer une Pull Request

### Standards de code

- Suivre les conventions Java standard
- Utiliser Lombok pour réduire le boilerplate
- Écrire des tests unitaires pour chaque use case
- Documenter les API avec des annotations OpenAPI

### Pipeline CI/CD

Le pipeline GitHub Actions exécute automatiquement :
- Build Maven
- Tests unitaires et d'intégration
- Analyse de qualité du code
- Construction de l'image Docker
- Publication sur GitHub Container Registry

## Roadmap

### Sprint 0 (Actuel)
- [x] Setup projet multi-modules
- [x] Configuration CI/CD
- [x] Docker Compose
- [x] Migrations base de données

### Sprint 1 (Q1 2026)
- [ ] Module authentification (Keycloak)
- [ ] CRUD Utilisateur
- [ ] Tests unitaires

### Sprint 2 (Q1 2026)
- [ ] CRUD Compte Rendu
- [ ] API REST complète
- [ ] Documentation OpenAPI

## Support

Pour toute question ou problème :
- Ouvrir une issue sur GitHub
- Contacter l'équipe technique CMCI

## Licence

Propriétaire - CMCI © 2026
