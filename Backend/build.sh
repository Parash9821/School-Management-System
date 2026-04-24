#!/bin/bash
set -e

# Fix execute permissions for mvnw
chmod +x mvnw

# Run Maven wrapper - it will download Maven if needed
./mvnw clean package -DskipTests