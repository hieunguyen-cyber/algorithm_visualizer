#!/bin/bash
# Simple script to start the backend server only

echo "🚀 Starting Algorithm Visualizer Backend..."
echo "==========================================="
echo ""

cd "$(dirname "$0")/backend"

if [ ! -f "target/algorithm-visualizer-1.0.0.jar" ]; then
    echo "📦 Building backend (this may take a moment)..."
    mvn clean package -DskipTests
    if [ $? -ne 0 ]; then
        echo "❌ Build failed!"
        exit 1
    fi
fi

echo "🔧 Starting server on http://localhost:8080"
echo "📡 API: http://localhost:8080/api"
echo "💊 Health: http://localhost:8080/health"
echo ""
echo "Press Ctrl+C to stop the server"
echo ""

java -jar target/algorithm-visualizer-1.0.0.jar
