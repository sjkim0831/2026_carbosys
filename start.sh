#!/bin/bash

# Carbosys Start Script
# This script starts the Carbosys application using docker-compose

echo "=========================================="
echo "  Carbosys Docker Compose Starter"
echo "=========================================="

# Get script directory
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Change to script directory
cd "$SCRIPT_DIR"

# Check if docker-compose.yml exists
if [ ! -f "docker-compose.yml" ]; then
    echo "Error: docker-compose.yml not found"
    exit 1
fi

# Check if Dockerfile exists
if [ ! -f "Dockerfile" ]; then
    echo "Error: Dockerfile not found"
    exit 1
fi

echo "Starting Carbosys application..."
echo ""

# Build and start containers
docker-compose up -d --build

# Check if containers are running
if [ $? -eq 0 ]; then
    echo ""
    echo "=========================================="
    echo "  Carbosys started successfully!"
    echo "=========================================="
    echo ""
    echo "Services:"
    docker-compose ps
    echo ""
    echo "View logs:"
    echo "  docker-compose logs -f"
    echo ""
    echo "Stop services:"
    echo "  docker-compose down"
else
    echo ""
    echo "Error: Failed to start Carbosys"
    echo ""
    echo "Check logs:"
    echo "  docker-compose logs"
    exit 1
fi
