#!/bin/bash
set -e

# Download and install Maven if not present
if [ ! -d "/opt/maven" ]; then
    echo "Downloading Maven..."
    cd /tmp
    curl -sL https://archive.apache.org/dist/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.tar.gz -o maven.tar.gz
    echo "Extracting Maven..."
    tar -xzf maven.tar.gz
    mv apache-maven-3.9.6 /opt/maven
    rm maven.tar.gz
fi

export PATH="/opt/maven/bin:$PATH"

# Fix execute permissions for mvnw
chmod +x mvnw

# Run Maven build
mvn clean package -DskipTests