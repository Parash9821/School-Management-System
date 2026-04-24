#!/bin/bash
set -e

# Fix execute permissions for mvnw
chmod +x mvnw

# Run Maven build
./mvnw clean package -DskipTests