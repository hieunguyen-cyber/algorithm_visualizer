#!/bin/bash
# Quick start script for Algorithm Visualizer

echo "🚀 Algorithm Visualizer - Quick Start Script"
echo "=============================================="
echo ""

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Check if Java is installed
if ! command -v java &> /dev/null; then
    echo -e "${RED}❌ Java is not installed. Please install Java 11 or higher.${NC}"
    exit 1
fi

echo -e "${GREEN}✓ Java found: $(java -version 2>&1 | head -n 1)${NC}"

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    echo -e "${RED}❌ Maven is not installed. Please install Maven 3.6 or higher.${NC}"
    exit 1
fi

echo -e "${GREEN}✓ Maven found: $(mvn -v | head -n 1)${NC}"
echo ""

# Build backend
echo -e "${YELLOW}📦 Building backend...${NC}"
cd backend
mvn clean package -DskipTests

if [ $? -ne 0 ]; then
    echo -e "${RED}❌ Build failed!${NC}"
    exit 1
fi

echo -e "${GREEN}✓ Backend built successfully!${NC}"
echo ""

# Start backend
echo -e "${YELLOW}🔧 Starting backend server on port 8080...${NC}"
java -jar target/algorithm-visualizer-1.0.0.jar &
BACKEND_PID=$!

sleep 2

# Check if backend is running
if ps -p $BACKEND_PID > /dev/null; then
    echo -e "${GREEN}✓ Backend running (PID: $BACKEND_PID)${NC}"
else
    echo -e "${RED}❌ Failed to start backend${NC}"
    exit 1
fi

echo ""
echo -e "${YELLOW}📡 Starting frontend server on port 8000...${NC}"
cd ../frontend

# Try different methods to start HTTP server
if command -v python3 &> /dev/null; then
    python3 -m http.server 8000 &
    FRONTEND_PID=$!
elif command -v python &> /dev/null; then
    python -m SimpleHTTPServer 8000 &
    FRONTEND_PID=$!
elif command -v node &> /dev/null; then
    npx http-server -p 8000 &
    FRONTEND_PID=$!
else
    echo -e "${RED}❌ No HTTP server found (Python or Node.js required)${NC}"
    kill $BACKEND_PID
    exit 1
fi

sleep 2

echo -e "${GREEN}✓ Frontend running (PID: $FRONTEND_PID)${NC}"
echo ""
echo -e "${GREEN}✅ Algorithm Visualizer is ready!${NC}"
echo ""
echo "📍 Frontend: http://localhost:8000"
echo "🔌 Backend API: http://localhost:8080/api"
echo "💊 Health Check: http://localhost:8080/health"
echo ""
echo "Press Ctrl+C to stop both servers"
echo ""

# Wait for Ctrl+C
trap "kill $BACKEND_PID $FRONTEND_PID; echo 'Servers stopped.'; exit 0" INT

wait
