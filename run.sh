#!/bin/bash
# Complete run script - starts both backend and frontend servers

set -e

# Get the directory where this script is located
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Color codes for output
RED='\033[0;31m'
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Cleanup function
cleanup() {
    echo -e "\n${YELLOW}🛑 Shutting down servers...${NC}"
    
    if [ ! -z "$BACKEND_PID" ]; then
        kill $BACKEND_PID 2>/dev/null || true
        echo -e "${BLUE}✓ Backend stopped (PID: $BACKEND_PID)${NC}"
    fi
    
    if [ ! -z "$FRONTEND_PID" ]; then
        kill $FRONTEND_PID 2>/dev/null || true
        echo -e "${BLUE}✓ Frontend stopped (PID: $FRONTEND_PID)${NC}"
    fi
    
    echo -e "${GREEN}Goodbye! 👋${NC}"
    exit 0
}

# Handle Ctrl+C
trap cleanup SIGINT SIGTERM

echo -e "${GREEN}================================${NC}"
echo -e "${GREEN}🚀 Algorithm Visualizer${NC}"
echo -e "${GREEN}================================${NC}"
echo ""

# ============================================================================
# BUILD & START BACKEND
# ============================================================================
echo -e "${BLUE}[1/3] Checking backend...${NC}"

cd "$SCRIPT_DIR/backend"

if [ ! -f "target/algorithm-visualizer-1.0.0.jar" ]; then
    echo -e "${YELLOW}📦 Building backend JAR (this may take a moment)...${NC}"
    mvn clean package -DskipTests > /dev/null 2>&1
    if [ $? -ne 0 ]; then
        echo -e "${RED}❌ Build failed! Check backend/pom.xml${NC}"
        exit 1
    fi
    echo -e "${GREEN}✓ Build successful${NC}"
else
    echo -e "${GREEN}✓ JAR file already built${NC}"
fi

echo -e "${BLUE}Starting backend server...${NC}"
java -jar target/algorithm-visualizer-1.0.0.jar > /tmp/backend.log 2>&1 &
BACKEND_PID=$!
echo -e "${GREEN}✓ Backend started (PID: $BACKEND_PID)${NC}"

# Wait for backend to be ready
echo -e "${YELLOW}⏳ Waiting for backend to be ready...${NC}"
for i in {1..30}; do
    if curl -s http://localhost:8080/health > /dev/null 2>&1; then
        echo -e "${GREEN}✓ Backend is ready!${NC}"
        break
    fi
    if [ $i -eq 30 ]; then
        echo -e "${RED}❌ Backend failed to start${NC}"
        echo -e "${YELLOW}Last log contents:${NC}"
        tail -20 /tmp/backend.log
        kill $BACKEND_PID 2>/dev/null || true
        exit 1
    fi
    sleep 0.2
done

cd ..

# ============================================================================
# START FRONTEND
# ============================================================================
echo -e "${BLUE}[2/3] Starting frontend server...${NC}"

cd "$SCRIPT_DIR/frontend"

if command -v python3 &> /dev/null; then
    python3 -m http.server 8000 > /tmp/frontend.log 2>&1 &
    FRONTEND_PID=$!
elif command -v python &> /dev/null; then
    python -m SimpleHTTPServer 8000 > /tmp/frontend.log 2>&1 &
    FRONTEND_PID=$!
elif command -v npx &> /dev/null; then
    npx http-server -p 8000 > /tmp/frontend.log 2>&1 &
    FRONTEND_PID=$!
else
    echo -e "${RED}❌ No HTTP server found!${NC}"
    echo "Please install Python or Node.js"
    kill $BACKEND_PID 2>/dev/null || true
    exit 1
fi

echo -e "${GREEN}✓ Frontend started (PID: $FRONTEND_PID)${NC}"

# ============================================================================
# DISPLAY INFO & OPEN BROWSER
# ============================================================================
echo -e "${BLUE}[3/3] Ready for use!${NC}"
echo ""
echo -e "${GREEN}✅ All systems operational!${NC}"
echo ""
echo -e "${BLUE}📍 Access the application:${NC}"
echo -e "   ${YELLOW}Frontend:${NC} http://localhost:8000"
echo -e "   ${YELLOW}Backend API:${NC} http://localhost:8080/api"
echo -e "   ${YELLOW}Health Check:${NC} http://localhost:8080/health"
echo ""
echo -e "${BLUE}🔌 Process IDs:${NC}"
echo -e "   ${YELLOW}Backend:${NC} $BACKEND_PID"
echo -e "   ${YELLOW}Frontend:${NC} $FRONTEND_PID"
echo ""
echo -e "${BLUE}📖 Documentation:${NC}"
echo -e "   Start with:${NC} ${YELLOW}QUICK_REFERENCE.md${NC}"
echo -e "   Learn algorithms:${NC} ${YELLOW}ALGORITHM_GUIDE.md${NC}"
echo ""
echo -e "${BLUE}🛑 To stop:${NC} Press ${YELLOW}Ctrl+C${NC}"
echo ""

# Try to open browser (macOS/Linux)
if command -v open &> /dev/null; then
    sleep 1
    open http://localhost:8000
elif command -v xdg-open &> /dev/null; then
    sleep 1
    xdg-open http://localhost:8000
fi

# Keep script running
wait
