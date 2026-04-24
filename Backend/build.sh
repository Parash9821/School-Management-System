#!/bin/bash
set -e

echo "=== Starting build ==="
echo "Checking available tools..."

# Check for curl or wget
if command -v curl &> /dev/null; then
    DOWNLOAD_CMD="curl -sL"
elif command -v wget &> /dev/null; then
    DOWNLOAD_CMD="wget -qO-"
else
    echo "ERROR: No download tool available"
    exit 1
fi

echo "Using: $DOWNLOAD_CMD"

# Download and install Maven if not present
if [ ! -d "/opt/maven" ]; then
    echo "Downloading Maven..."
    cd /tmp
    $DOWNLOAD_CMD https://archive.apache.org/dist/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.tar.gz -o maven.tar.gz
    echo "Extracting Maven..."
    tar -xzf maven.tar.gz
    mv apache-maven-3.9.6 /opt/maven
    rm maven.tar.gz
fi

export PATH="/opt/maven/bin:$PATH"
echo "Maven version: $(mvn --version)"

# Fix execute permissions for mvnw
chmod +x mvnw

# Run Maven build
echo "Running Maven build..."
mvn clean package -DskipTests