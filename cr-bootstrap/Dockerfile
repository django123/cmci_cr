# Multi-stage build pour optimiser la taille de l'image
FROM maven:3.9-eclipse-temurin-21-alpine AS build

WORKDIR /app

# Copier les fichiers pom.xml d'abord pour le cache des dépendances
COPY pom.xml .
COPY cr-domain/pom.xml cr-domain/
COPY cr-application/pom.xml cr-application/
COPY cr-infrastructure/pom.xml cr-infrastructure/
COPY cr-api/pom.xml cr-api/
COPY cr-bootstrap/pom.xml cr-bootstrap/

# Télécharger les dépendances
RUN mvn dependency:go-offline -B

# Copier tout le code source
COPY cr-domain/src cr-domain/src
COPY cr-application/src cr-application/src
COPY cr-infrastructure/src cr-infrastructure/src
COPY cr-api/src cr-api/src
COPY cr-bootstrap/src cr-bootstrap/src

# Build l'application (skip tests pour accélérer le build)
RUN mvn clean package -DskipTests -B

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Créer un utilisateur non-root pour la sécurité
RUN addgroup -S cmci && adduser -S cmci -G cmci

# Copier le JAR depuis le stage de build
COPY --from=build /app/cr-bootstrap/target/*.jar app.jar

# Créer le dossier logs
RUN mkdir -p /app/logs && chown -R cmci:cmci /app

# Passer à l'utilisateur non-root
USER cmci

# Exposer le port
EXPOSE 8081

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8081/actuator/health || exit 1

# Variables d'environnement par défaut
ENV SPRING_PROFILES_ACTIVE=prod \
    JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC"

# Commande de démarrage
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar app.jar"]
