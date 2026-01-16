# Module cr-bootstrap

Point d'entrée de l'application CMCI Compte Rendu Backend. Ce module assemble tous les autres modules et lance l'application Spring Boot.

## Vue d'Ensemble

Le module **cr-bootstrap** est responsable de :
- Démarrer l'application Spring Boot
- Assembler tous les modules (domain, application, infrastructure, api)
- Configurer les beans principaux
- Gérer les profils d'environnement (dev, test, prod)
- Fournir les scripts de déploiement

## Structure du Module

```
cr-bootstrap/
├── src/main/
│   ├── java/com/cmci/cr/
│   │   ├── CmciCrApplication.java          # Point d'entrée Spring Boot
│   │   └── config/
│   │       ├── ApplicationConfiguration.java # Configuration centrale
│   │       └── BeanConfiguration.java       # Configuration des Use Cases
│   └── resources/
│       ├── application.yml                  # Configuration principale
│       ├── application-dev.yml              # Configuration développement
│       ├── application-prod.yml             # Configuration production
│       ├── application-test.yml             # Configuration tests
│       ├── banner.txt                       # Banner Spring Boot
│       └── logback-spring.xml               # Configuration logs
├── Dockerfile                               # Image Docker
├── docker-compose.yml                       # Stack complète (prod)
├── docker-compose.dev.yml                   # Stack dev (infra seulement)
├── prometheus.yml                           # Configuration Prometheus
├── .env.example                             # Exemple variables d'environnement
├── start.sh                                 # Script de démarrage
└── stop.sh                                  # Script d'arrêt
```

## Démarrage Rapide

### Prérequis

- **Java 21** ou supérieur
- **Maven 3.9+**
- **Docker** et **Docker Compose** (pour l'infrastructure)
- **PostgreSQL 16** (ou via Docker)
- **Redis 7** (ou via Docker)
- **Kafka 3.6** (ou via Docker)

### 1. Cloner et Builder

```bash
# Cloner le projet
git clone <repository-url>
cd cmci-cr-backend

# Builder tous les modules
mvn clean install
```

### 2. Configuration

Copier le fichier d'exemple et ajuster les valeurs :

```bash
cd cr-bootstrap
cp .env.example .env
nano .env  # Ajuster les valeurs
```

### 3. Démarrer l'Infrastructure

**Option A: Docker Compose (Recommandé pour dev)**

```bash
# Démarrer PostgreSQL, Redis, Kafka, Keycloak
docker-compose -f docker-compose.dev.yml up -d

# Vérifier que les services sont prêts
docker-compose -f docker-compose.dev.yml ps
```

**Option B: Services locaux**

Installer et démarrer PostgreSQL, Redis, Kafka localement.

### 4. Démarrer l'Application

**Option A: Avec Maven**

```bash
# Depuis le dossier racine cmci-cr-backend
mvn spring-boot:run -pl cr-bootstrap -Dspring-boot.run.profiles=dev
```

**Option B: Avec le JAR**

```bash
cd cr-bootstrap
java -jar target/cr-bootstrap-1.0.0-SNAPSHOT.jar --spring.profiles.active=dev
```

**Option C: Avec le script**

```bash
cd cr-bootstrap
./start.sh
```

### 5. Vérifier

- **API**: http://localhost:8081
- **Swagger UI**: http://localhost:8081/swagger-ui.html
- **Health Check**: http://localhost:8081/actuator/health
- **Prometheus**: http://localhost:8081/actuator/prometheus

## Profils d'Environnement

### Profile `dev` (Développement)

```bash
SPRING_PROFILES_ACTIVE=dev
```

**Caractéristiques** :
- Logs verbeux (DEBUG)
- SQL logs activés
- Swagger UI activé
- H2 Console disponible (optionnel)
- Stack traces complètes dans les erreurs

**Utilisation** :
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### Profile `test` (Tests)

```bash
SPRING_PROFILES_ACTIVE=test
```

**Caractéristiques** :
- Base H2 en mémoire
- Flyway désactivé
- Redis et Kafka embarqués (Testcontainers)
- Sécurité simplifiée

**Utilisation** :
```bash
mvn test
```

### Profile `prod` (Production)

```bash
SPRING_PROFILES_ACTIVE=prod
```

**Caractéristiques** :
- Logs WARNING/INFO uniquement
- SQL logs désactivés
- Swagger UI désactivé
- Pool de connexions optimisé (20 connexions)
- Stack traces masquées
- Compression HTTP activée

**Utilisation** :
```bash
java -jar cr-bootstrap.jar --spring.profiles.active=prod
```

## Déploiement Docker

### Build de l'Image

```bash
# Depuis cr-bootstrap/
docker build -t cmci-cr-backend:latest -f Dockerfile ..
```

### Démarrer avec Docker Compose

**Stack complète (Production)** :
```bash
docker-compose up -d
```

Services inclus :
- `postgres` : PostgreSQL 16
- `redis` : Redis 7
- `kafka` + `zookeeper` : Kafka 3.6
- `keycloak` : Keycloak 24
- `app` : CMCI CR Backend
- `prometheus` : Monitoring
- `grafana` : Dashboards

**Stack infrastructure uniquement (Dev)** :
```bash
docker-compose -f docker-compose.dev.yml up -d
```

Services inclus : PostgreSQL, Redis, Kafka, Keycloak

### Arrêter les Services

```bash
# Infrastructure dev
docker-compose -f docker-compose.dev.yml down

# Ou avec le script
./stop.sh

# Stack complète
docker-compose down

# Supprimer aussi les volumes
docker-compose down -v
```

## Configuration

### Variables d'Environnement

| Variable | Description | Défaut | Requis |
|----------|-------------|--------|--------|
| `SPRING_PROFILES_ACTIVE` | Profil actif | dev | Non |
| `DB_HOST` | Hôte PostgreSQL | localhost | Oui |
| `DB_PORT` | Port PostgreSQL | 5432 | Non |
| `DB_NAME` | Nom base de données | cmci_cr | Oui |
| `DB_USERNAME` | Utilisateur DB | cmci_user | Oui |
| `DB_PASSWORD` | Mot de passe DB | - | Oui |
| `REDIS_HOST` | Hôte Redis | localhost | Oui |
| `REDIS_PORT` | Port Redis | 6379 | Non |
| `REDIS_PASSWORD` | Mot de passe Redis | - | Non |
| `KAFKA_BOOTSTRAP_SERVERS` | Brokers Kafka | localhost:9092 | Oui |
| `KEYCLOAK_ISSUER_URI` | URI Keycloak | - | Oui |
| `KEYCLOAK_JWK_SET_URI` | URI JWK Keycloak | - | Oui |
| `CORS_ALLOWED_ORIGINS` | Origines CORS | localhost:3000 | Non |
| `SERVER_PORT` | Port serveur | 8081 | Non |
| `LOG_LEVEL` | Niveau logs app | DEBUG | Non |
| `JAVA_OPTS` | Options JVM | - | Non |

### Fichiers de Configuration

**application.yml** : Configuration principale et par défaut

**application-dev.yml** : Surcharge pour développement
- Logs DEBUG activés
- SQL formaté
- Swagger activé

**application-prod.yml** : Surcharge pour production
- Logs WARNING/INFO
- Swagger désactivé
- Pool de connexions optimisé
- Compression HTTP

**application-test.yml** : Surcharge pour tests
- H2 en mémoire
- Testcontainers

## Monitoring et Observabilité

### Health Check

```bash
curl http://localhost:8081/actuator/health
```

Réponse :
```json
{
  "status": "UP",
  "components": {
    "db": {"status": "UP"},
    "redis": {"status": "UP"},
    "diskSpace": {"status": "UP"},
    "ping": {"status": "UP"}
  }
}
```

### Métriques Prometheus

```bash
curl http://localhost:8081/actuator/prometheus
```

Accessible aussi via Prometheus UI : http://localhost:9090

### Dashboards Grafana

- URL : http://localhost:3000
- User : `admin`
- Password : `admin`

Importer les dashboards pour :
- Métriques JVM
- Métriques applicatives
- Métriques base de données

## Logs

### Emplacement

- **Console** : Sortie standard
- **Fichier** : `logs/cmci-cr-backend.log`
- **Erreurs** : `logs/cmci-cr-backend-error.log`

### Rotation

- Taille max : 10 MB
- Historique : 30 jours (logs normaux), 90 jours (erreurs)

### Configuration

Fichier `logback-spring.xml` :
- Pattern console coloré
- Pattern fichier détaillé
- Appenders séparés pour erreurs
- Configuration par profil

## Sécurité

### OAuth2/Keycloak

L'application utilise Keycloak pour l'authentification :

1. **Obtenir un token** :
```bash
curl -X POST http://localhost:8180/realms/cmci/protocol/openid-connect/token \
  -d "client_id=cmci-client" \
  -d "client_secret=<secret>" \
  -d "grant_type=password" \
  -d "username=<user>" \
  -d "password=<password>"
```

2. **Utiliser le token** :
```bash
curl -H "Authorization: Bearer <token>" \
  http://localhost:8081/api/v1/cr
```

### Configuration Keycloak

1. Accéder à http://localhost:8180
2. Login avec `admin` / `admin`
3. Créer le realm `cmci`
4. Créer le client `cmci-client`
5. Configurer les rôles : FIDELE, FD, LEADER, PASTEUR, ADMIN

## Scripts Utilitaires

### start.sh

Démarre l'infrastructure et l'application :
```bash
./start.sh
```

Options :
- `--build` : Force la reconstruction

### stop.sh

Arrête les services d'infrastructure :
```bash
./stop.sh
```

## Dépendances

Le module **cr-bootstrap** dépend de tous les autres modules :

```xml
<dependencies>
    <dependency>
        <groupId>com.cmci.cr</groupId>
        <artifactId>cr-domain</artifactId>
    </dependency>
    <dependency>
        <groupId>com.cmci.cr</groupId>
        <artifactId>cr-application</artifactId>
    </dependency>
    <dependency>
        <groupId>com.cmci.cr</groupId>
        <artifactId>cr-infrastructure</artifactId>
    </dependency>
    <dependency>
        <groupId>com.cmci.cr</groupId>
        <artifactId>cr-api</artifactId>
    </dependency>
</dependencies>
```

## Troubleshooting

### L'application ne démarre pas

**Erreur : "Could not connect to database"**
```bash
# Vérifier que PostgreSQL est démarré
docker-compose -f docker-compose.dev.yml ps postgres

# Vérifier les logs
docker logs cmci-cr-postgres-dev
```

**Erreur : "Could not connect to Redis"**
```bash
# Vérifier Redis
docker-compose -f docker-compose.dev.yml ps redis
redis-cli ping  # Doit retourner PONG
```

**Erreur : "Kafka connection failed"**
```bash
# Vérifier Kafka
docker-compose -f docker-compose.dev.yml ps kafka
```

### Port déjà utilisé

```bash
# Trouver le processus utilisant le port 8081
lsof -i :8081

# Le tuer
kill -9 <PID>
```

### Logs

```bash
# Logs en temps réel
tail -f logs/cmci-cr-backend.log

# Logs d'erreur
tail -f logs/cmci-cr-backend-error.log

# Logs Docker
docker logs -f cmci-cr-backend
```

### Reset complet

```bash
# Arrêter tout
docker-compose down -v

# Supprimer les logs
rm -rf logs/*

# Rebuild
mvn clean install

# Redémarrer
docker-compose -f docker-compose.dev.yml up -d
./start.sh
```

## Performance

### Tuning JVM

```bash
# Production
JAVA_OPTS="-Xms1g -Xmx2g -XX:+UseG1GC -XX:MaxGCPauseMillis=200"

# Avec GC logs
JAVA_OPTS="$JAVA_OPTS -Xlog:gc*:file=logs/gc.log:time,uptime,level,tags"
```

### Pool de Connexions

Ajuster selon la charge :
```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20  # Ajuster selon CPU
      minimum-idle: 10
```

### Cache Redis

TTL par défaut configurés :
- Utilisateurs : 2 heures
- Comptes rendus : 30 minutes
- Statistiques : 5 minutes

## CI/CD

### GitHub Actions

Workflow disponible dans `.github/workflows/ci-cd.yml` :
- Build et tests sur chaque push
- Analyse de code (SonarQube)
- Build Docker
- Déploiement automatique

### Pipeline

1. **Build** : `mvn clean install`
2. **Tests** : `mvn test`
3. **Quality** : SonarQube scan
4. **Docker** : Build et push image
5. **Deploy** : Kubernetes/Docker Swarm

## Architecture

```
cr-bootstrap (Point d'entrée)
    ↓
cr-api (REST Controllers)
    ↓
cr-application (Use Cases)
    ↓
cr-domain (Entités, Business Logic)
    ↓
cr-infrastructure (Persistence, Cache, Kafka)
```

**Pattern** : Hexagonal Architecture (Ports & Adapters)

## Prochaines Étapes

- [ ] Ajouter le support de Kubernetes (Helm charts)
- [ ] Implémenter le distributed tracing (Jaeger/Zipkin)
- [ ] Ajouter le rate limiting avec Redis
- [ ] Configurer le circuit breaker (Resilience4j)
- [ ] Implémenter le blue-green deployment
- [ ] Ajouter les tests de charge (Gatling)

## Support

Pour toute question ou problème :
- GitHub Issues : https://github.com/cmci/cmci-cr/issues
- Documentation complète : `/docs`
- Email : support@cmci.org
