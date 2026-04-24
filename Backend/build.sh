#!/bin/bash
set -e

echo "=== Starting build ==="

# Make mvnw executable
chmod 755 mvnw

# Try using Maven wrapper directly
echo "Running Maven wrapper..."
bash mvnw clean package -DskipTests