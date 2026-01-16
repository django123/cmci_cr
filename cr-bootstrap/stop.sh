#!/bin/bash

# Script d'arrêt pour CMCI CR Backend

echo "Stopping CMCI CR Backend infrastructure services..."

docker-compose -f docker-compose.dev.yml down

echo "Services stopped successfully!"
