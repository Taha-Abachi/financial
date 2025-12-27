#!/bin/bash

# Change to the docker directory
cd "$(dirname "$0")"

# Remote server details
REMOTE_USER="taha"
REMOTE_HOST="194.60.231.247"
REMOTE_PORT="9011"
REMOTE_PATH="/home/taha/creditapp"
JAR_FILE="../build/libs/financial-0.0.1-SNAPSHOT.jar"
PASSWORD="ihcabaahat@1A"

# Check if JAR file exists
if [ ! -f "$JAR_FILE" ]; then
    echo "Error: JAR file not found at $JAR_FILE"
    echo "Please build the project first using: ./gradlew build"
    exit 1
fi

# Check if sshpass is installed (for password authentication)
if command -v sshpass &> /dev/null; then
    # Use sshpass for password authentication
    sshpass -p "$PASSWORD" scp -P "$REMOTE_PORT" -o StrictHostKeyChecking=no "$JAR_FILE" "$REMOTE_USER@$REMOTE_HOST:$REMOTE_PATH"
else
    # Fallback: use scp with SSH key or interactive password
    echo "Note: sshpass not found. Using scp (will prompt for password or use SSH key)"
    echo "To install sshpass: sudo apt-get install sshpass"
    scp -P "$REMOTE_PORT" -o StrictHostKeyChecking=no "$JAR_FILE" "$REMOTE_USER@$REMOTE_HOST:$REMOTE_PATH"
fi

if [ $? -eq 0 ]; then
    echo "Successfully copied JAR file to $REMOTE_USER@$REMOTE_HOST:$REMOTE_PATH"
else
    echo "Error: Failed to copy JAR file"
    exit 1
fi
