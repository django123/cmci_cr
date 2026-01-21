# Guide de Deploiement CMCI CR Service

## Pre-requis

- Docker et Docker Compose
- Java 17+
- Maven 3.8+

## Demarrage rapide

### 1. Lancer l'infrastructure (PostgreSQL, Keycloak)

```bash
docker-compose up -d postgres keycloak-postgres keycloak
```

Attendre que Keycloak soit pret (environ 1-2 minutes). Verifier avec:
```bash
docker-compose logs -f keycloak
```

### 2. Compiler l'application

```bash
mvn clean package -DskipTests
```

### 3. Lancer l'application

```bash
java -jar cr-bootstrap/target/cr-bootstrap-1.0.0-SNAPSHOT.jar
```

L'application sera disponible sur: http://localhost:8081/api

## URLs importantes

| Service | URL | Credentials |
|---------|-----|-------------|
| API Backend | http://localhost:8081/api | - |
| Swagger UI | http://localhost:8081/api/swagger-ui.html | - |
| Keycloak Admin | http://localhost:8180 | admin / admin123 |
| PostgreSQL | localhost:5433 | cmci_user / cmci_password |

## Utilisateurs de test

Le realm Keycloak est pre-configure avec les utilisateurs suivants:

### Admin
- Email: admin@cmci.org
- Password: admin123
- Role: ADMIN

### Pasteurs
| Email | Password | Role |
|-------|----------|------|
| pasteur@cmci.org | pasteur123 | PASTEUR |
| pasteur.lyon@cmci.org | pasteur123 | PASTEUR |
| pasteur.bruxelles@cmci.org | pasteur123 | PASTEUR |
| pasteur.douala@cmci.org | pasteur123 | PASTEUR |

### Leaders
| Email | Password | Role |
|-------|----------|------|
| leader@cmci.org | leader123 | LEADER |
| leader2@cmci.org | leader123 | LEADER |
| leader3@cmci.org | leader123 | LEADER |
| leader.lyon@cmci.org | leader123 | LEADER |
| leader.bruxelles@cmci.org | leader123 | LEADER |
| leader.douala@cmci.org | leader123 | LEADER |

### FDs (Disciples Makers)
| Email | Password | Role |
|-------|----------|------|
| fd@cmci.org | fd123456 | FD |
| fd2@cmci.org | fd123456 | FD |
| fd3@cmci.org | fd123456 | FD |
| fd.lyon@cmci.org | fd123456 | FD |
| fd.douala@cmci.org | fd123456 | FD |

### Fideles
| Email | Password | Role |
|-------|----------|------|
| fidele@cmci.org | fidele123 | FIDELE |
| fidele2@cmci.org | fidele123 | FIDELE |
| fidele3@cmci.org | fidele123 | FIDELE |
| fidele4@cmci.org | fidele123 | FIDELE |
| fidele5@cmci.org | fidele123 | FIDELE |
| fidele6@cmci.org | fidele123 | FIDELE |
| fidele.lyon@cmci.org | fidele123 | FIDELE |
| fidele.douala@cmci.org | fidele123 | FIDELE |
| fidele2.douala@cmci.org | fidele123 | FIDELE |

## Obtenir un token JWT

```bash
# Remplacer USERNAME et PASSWORD par les credentials d'un utilisateur
curl -X POST http://localhost:8180/realms/cmci/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "client_id=cmci-cr-frontend" \
  -d "username=fidele@cmci.org" \
  -d "password=fidele123" \
  -d "grant_type=password"
```

## Appeler l'API

```bash
# Remplacer TOKEN par le access_token obtenu
curl -X GET http://localhost:8081/api/api/v1/cr \
  -H "Authorization: Bearer TOKEN"
```

## Structure des donnees de test

### Hierarchie Ecclesiastique

```
Region Afrique (AFR)
  └── Zone RDC
      └── Eglise CMCI Kinshasa

Region Europe (EUR)
  ├── Zone France
  │   ├── Eglise CMCI Paris Centre
  │   │   ├── EM Paris 11eme (Leader Pierre)
  │   │   ├── EM Paris 18eme (Leader Thomas)
  │   │   └── EM Paris 20eme (Leader Francois)
  │   └── Eglise CMCI Lyon
  │       ├── EM Lyon Villeurbanne (Leader Philippe)
  │       └── EM Lyon Venissieux
  └── Zone Belgique
      └── Eglise CMCI Bruxelles
          └── EM Bruxelles Ixelles (Leader Luc)

Region Amerique (AME)
  └── Zone USA

Zone Cameroun (sous Region Afrique)
  └── Eglise CMCI Douala
      ├── EM Douala Akwa (Leader Emmanuel)
      └── EM Douala Bonanjo
```

### Hierarchy des Utilisateurs

```
ADMIN (admin@cmci.org)

PASTEUR Jean (pasteur@cmci.org) -> Eglise Paris Centre
  └── LEADER Pierre (leader@cmci.org)
      ├── FD Marie (fd@cmci.org)
      │   ├── FIDELE Paul (fidele@cmci.org)
      │   ├── FIDELE Antoine (fidele2@cmci.org)
      │   └── FIDELE Julie (fidele3@cmci.org)
      └── FD Sophie (fd2@cmci.org)
          ├── FIDELE Lucas (fidele4@cmci.org)
          └── FIDELE Emma (fidele5@cmci.org)
```

## Variables d'environnement

| Variable | Defaut | Description |
|----------|--------|-------------|
| SPRING_DATASOURCE_URL | jdbc:postgresql://localhost:5433/cmci_cr | URL PostgreSQL |
| SPRING_DATASOURCE_USERNAME | cmci_user | User PostgreSQL |
| SPRING_DATASOURCE_PASSWORD | cmci_password | Password PostgreSQL |
| KEYCLOAK_ISSUER_URI | http://localhost:8180/realms/cmci | URL du realm Keycloak |
| KEYCLOAK_JWK_SET_URI | http://localhost:8180/realms/cmci/protocol/openid-connect/certs | URL des certificats |

## Troubleshooting

### Keycloak ne demarre pas
Verifier que keycloak-postgres est healthy:
```bash
docker-compose ps
```

### L'application ne se connecte pas a Keycloak
S'assurer que l'URL de Keycloak est accessible depuis l'application:
```bash
curl http://localhost:8180/realms/cmci/.well-known/openid-configuration
```

### Erreur "realm not found"
Le realm est importe automatiquement au demarrage. Attendre 1-2 minutes ou reimporter manuellement via l'admin console.
