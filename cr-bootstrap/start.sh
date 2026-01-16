#!/bin/bash

# Script de démarrage pour CMCI CR Backend

set -e

echo "========================================="
echo "  CMCI Compte Rendu - Backend Startup"
echo "========================================="
echo ""

# Vérifier les variables d'environnement
if [ -f .env ]; then
    echo "Loading environment variables from .env file..."
    export $(cat .env | grep -v '^#' | xargs)
fi

# Profile par défaut
PROFILE=${SPRING_PROFILES_ACTIVE:-dev}
echo "Active profile: $PROFILE"
echo ""

# Démarrer les services d'infrastructure si en mode dev
if [ "$PROFILE" = "dev" ]; then
    echo "Starting infrastructure services (PostgreSQL, Redis, Kafka, Keycloak)..."
    docker-compose -f docker-compose.dev.yml up -d

    echo "Waiting for services to be ready..."
    sleep 10
    echo ""
fi

# Construire l'application si nécessaire
if [ ! -f "../cr-bootstrap/target/*.jar" ] || [ "$1" = "--build" ]; then
    echo "Building application..."
    cd ..
    mvn clean package -DskipTests
    cd cr-bootstrap
    echo "Build completed!"
    echo ""
fi

# Démarrer l'application
echo "Starting CMCI CR Backend..."
echo "Application will be available at: http://localhost:${SERVER_PORT:-8081}"
echo "Swagger UI: http://localhost:${SERVER_PORT:-8081}/swagger-ui.html"
echo "Actuator Health: http://localhost:${SERVER_PORT:-8081}/actuator/health"
echo ""

java -jar target/*.jar --spring.profiles.active=$PROFILE
