#!/bin/sh

# Build and Run Script for Jedis Project
# Cleans, packages, and runs the Redis clone server
# Usage: ./build-and-run.sh [JAVA_ARGS...]
# Example: ./build-and-run.sh --port 8080

set -e  # Exit on error

echo "========================================"
echo "  Jedis Build and Run Script"
echo "========================================"
echo ""

# Step 1: Clean previous builds
echo "[1/3] Cleaning previous builds..."
mvn clean
echo "Clean complete"
echo ""

# Step 2: Package the project (compile + jar)
echo "[2/3] Packaging project with Maven..."
mvn -q package
echo "Packaging complete — JAR built in target/"
echo ""

# Step 3: Run the application
echo "[3/3] Starting Jedis server..."
echo "========================================"
java -jar ./target/jedis.jar "$@"
