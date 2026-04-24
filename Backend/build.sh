#!/bin/bash
set -e

# Install Maven if not present
if ! command -v mvn &> /dev/null; then
    echo "Installing Maven..."
    cd /tmp
    wget -q https://archive.apache.org/dist/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.tar.gz
    tar -xzf apache-maven-3.9.6-bin.tar.gz
    export PATH="/tmp/apache-maven-3.9.6/bin:$PATH"
    cd -
fi

# Fix execute permissions for mvnw
chmod +x mvnw

# Run Maven build using system mvn if available, otherwise wrapper
if command -v mvn &> /dev/null; then
    mvn clean package -DskipTests
else
    ./mvnw clean package -DskipTests
fi