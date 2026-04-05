#!/bin/bash
# Simple script to start the frontend development server

echo "🚀 Starting Algorithm Visualizer Frontend..."
echo "============================================"
echo ""

cd "$(dirname "$0")/frontend"

if command -v python3 &> /dev/null; then
    echo "📡 Using Python 3 HTTP server on http://localhost:8000"
    echo "Press Ctrl+C to stop"
    echo ""
    python3 -m http.server 8000
elif command -v python &> /dev/null; then
    echo "📡 Using Python 2 HTTP server on http://localhost:8000"
    echo "Press Ctrl+C to stop"
    echo ""
    python -m SimpleHTTPServer 8000
elif command -v npx &> /dev/null; then
    echo "📡 Using Node.js http-server on http://localhost:8000"
    echo "Press Ctrl+C to stop"
    echo ""
    npx http-server -p 8000
else
    echo "❌ No HTTP server found!"
    echo "Please install Python or Node.js to run this script."
    exit 1
fi
