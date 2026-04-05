#!/bin/bash
# Diagnostic script to check backend connectivity

echo ""
echo "🔍 Algorithm Visualizer - Diagnostic Check"
echo "=========================================="
echo ""

# Check if port 8080 is listening
echo "🔌 Checking if backend is running on port 8080..."
if nc -z localhost 8080 2>/dev/null; then
    echo "✅ Port 8080 is open"
    
    # Try health check
    echo ""
    echo "💓 Testing health endpoint..."
    RESPONSE=$(curl -s http://localhost:8080/health)
    if echo "$RESPONSE" | grep -q "ok"; then
        echo "✅ Backend is responding: $RESPONSE"
    else
        echo "⚠️  Backend not responding properly. Response: $RESPONSE"
    fi
else
    echo "❌ Port 8080 is closed - Backend is NOT running!"
    echo ""
    echo "To start the backend, run:"
    echo "  ./run-backend.sh"
    echo ""
    exit 1
fi

# Check frontend 
echo ""
echo "🌐 Checking if frontend is running on port 8000..."
if nc -z localhost 8000 2>/dev/null; then
    echo "✅ Port 8000 is open"
    
    # Try to fetch index.html
    RESPONSE=$(curl -s -w "%{http_code}" -o /dev/null http://localhost:8000/index.html)
    if [ "$RESPONSE" = "200" ]; then
        echo "✅ Frontend is responding correctly (HTTP 200)"
    else
        echo "⚠️  Frontend HTTP response: $RESPONSE"
    fi
else
    echo "❌ Port 8000 is closed - Frontend is NOT running!"
    echo ""
    echo "To start the frontend, run:"
    echo "  ./run-frontend.sh"
    echo ""
    exit 1
fi

echo ""
echo "🎯 Quick Test API"
echo "=================="
echo ""
echo "Testing pathfinding API with a simple 5x5 grid..."
RESULT=$(curl -s -X POST http://localhost:8080/api/pathfinding \
  -H "Content-Type: application/json" \
  -d '{
    "algorithm": "astar",
    "gridWidth": 5,
    "gridHeight": 5,
    "startX": 0,
    "startY": 0,
    "goalX": 4,
    "goalY": 4,
    "obstacles": [[1,1], [2,2]]
  }')

echo "$RESULT" | grep -q "path"
if [ $? -eq 0 ]; then
    echo "✅ API test passed! Backend is working correctly."
    echo "   Response contains path data"
    echo ""
    echo "You can now:"
    echo "1. Open http://localhost:8000 in your browser"
    echo "2. Click the 'Start' button to run pathfinding"
    echo "3. Select different algorithms from dropdown"
else
    echo "❌ API test failed. Response:"
    echo "$RESULT" | head -c 500
fi

echo ""
echo "✅ Diagnostic complete!"
echo ""
