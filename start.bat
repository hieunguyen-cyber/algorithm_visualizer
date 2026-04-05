@echo off
REM Quick start script for Algorithm Visualizer (Windows)

echo.
echo 🚀 Algorithm Visualizer - Quick Start Script (Windows)
echo ==================================================
echo.

REM Check if Java is installed
java -version >nul 2>&1
if errorlevel 1 (
    echo ❌ Java is not installed. Please install Java 11 or higher.
    pause
    exit /b 1
)

echo ✓ Java found
java -version

REM Check if Maven is installed
mvn -v >nul 2>&1
if errorlevel 1 (
    echo ❌ Maven is not installed. Please install Maven 3.6 or higher.
    pause
    exit /b 1
)

echo ✓ Maven found
mvn -v | for /f "tokens=*" %%i in ('more') do @echo %%i & exit /b0

echo.
echo 📦 Building backend...
cd backend
call mvn clean package -DskipTests

if errorlevel 1 (
    echo ❌ Build failed!
    pause
    exit /b 1
)

echo ✓ Backend built successfully!
echo.

echo 🔧 Starting backend server on port 8080...
start cmd /k "java -jar target/algorithm-visualizer-1.0.0-jar-with-dependencies.jar"

timeout /t 2 /nobreak

echo 📡 Starting frontend server on port 8000...
cd ..\frontend

REM Try Python first
python -m http.server 8000 >nul 2>&1
if errorlevel 1 (
    REM Try Node.js
    npx http-server -p 8000
) else (
    start cmd /k "python -m http.server 8000"
)

echo.
echo ✅ Algorithm Visualizer is ready!
echo.
echo 📍 Frontend: http://localhost:8000
echo 🔌 Backend API: http://localhost:8080/api
echo.
echo Open http://localhost:8000 in your browser!
echo Press Ctrl+C in both console windows to stop.
echo.
pause
