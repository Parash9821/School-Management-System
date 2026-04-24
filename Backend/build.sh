#!/bin/bash
set -e

# Install Maven via apt if available
if ! command -v mvn &> /dev/null; then
    echo "Installing Maven via apt..."
    apt-get update -qq
    apt-get install -y -qq maven
fi

# Fix execute permissions for mvnw
chmod +x mvnw

# Run Maven build
./mvnw clean package -DskipTests