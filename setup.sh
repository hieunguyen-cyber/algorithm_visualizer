#!/bin/bash
# Complete setup script - builds and configures everything

echo "=============================================
echo "Algorithm Visualizer - Complete Setup"
echo "============================================="
echo ""

# Check requirements
echo "📋 Checking prerequisites..."

if ! command -v java &> /dev/null; then
    echo "❌ Java not found. Please install Java 11+"
    exit 1
fi
echo "✓ Java: $(java -version 2>&1 | head -n 1)"

if ! command -v mvn &> /dev/null; then
    echo "❌ Maven not found. Please install Maven 3.6+"
    exit 1
fi
echo "✓ Maven: $(mvn -v | head -n 1)"

echo ""
echo "📦 Building backend..."
cd "$(dirname "$0")/backend"
mvn clean package -DskipTests

if [ $? -eq 0 ]; then
    echo "✓ Backend build successful!"
else
    echo "❌ Backend build failed!"
    exit 1
fi

echo ""
echo "✅ Setup complete!"
echo ""
echo "To start the application:"
echo "1. Terminal 1: ./run-backend.sh"
echo "2. Terminal 2: ./run-frontend.sh"
echo ""
echo "Then open http://localhost:8000 in your browser"
