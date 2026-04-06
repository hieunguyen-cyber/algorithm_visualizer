# Render Web Service Deployment Guide

This application is configured to deploy on Render Web Service without manual modifications.

## Deployment Steps

1. **Connect Repository to Render**
   - Go to [render.com](https://render.com)
   - Create a new Web Service
   - Connect your GitHub repository
   - Set Root Directory: `backend`

2. **Configure Build & Start Commands**
   
   **Build Command:**
   ```bash
   ./mvnw clean package
   ```
   
   **Start Command:**
   ```bash
   java -jar target/*.jar
   ```

3. **Environment Variables (Optional)**
   - `PORT`: (Automatically set by Render, default: 8080)
   - No additional configuration required

## How It Works

### Backend Configuration
- **Port Handling**: The application automatically reads the `PORT` environment variable set by Render. If not present, it defaults to 8080 for local development.
- **Location**: `/backend/src/main/java/com/algo/server/AlgorithmServer.java`
- **Method**: `getPort()` function reads environment variable at startup

```java
private static final int PORT = getPort();

private static int getPort() {
    String portEnv = System.getenv("PORT");
    if (portEnv != null && !portEnv.isEmpty()) {
        try { return Integer.parseInt(portEnv); }
        catch (NumberFormatException e) { System.err.println("Invalid PORT"); }
    }
    return 8080;
}
```

### Frontend Configuration
- **API URL Detection**: The frontend automatically detects environment and uses correct API endpoint.
- **Location**: `/frontend/config.js`
- **Logic**: 
  - Local development (localhost): Uses `http://localhost:8080/api`
  - Production (Render): Uses same origin (e.g., `https://your-app.onrender.com/api`)

```javascript
const getApiBase = () => {
    if (window.location.hostname === 'localhost' || window.location.hostname === '127.0.0.1') {
        return 'http://localhost:8080/api';
    }
    return window.location.origin + '/api';
};
```

## Build Process

The build uses Maven Wrapper (`mvnw`) to ensure consistency:
- **No local Maven required**: Maven wrapper is included in repository
- **Output**: Single executable JAR with all dependencies: `target/*.jar`
- **Build Time**: ~30-60 seconds (first build may be slower)

Maven Wrapper files:
- `backend/mvnw` (Linux/Mac wrapper)
- `backend/mvnw.cmd` (Windows wrapper)
- `backend/.mvn/wrapper/` (Configuration and downloaded Maven)

## API Endpoints

After deployment, the following endpoints are available:

### Sudoku Solver
- `POST /api/sudoku/solve-with-steps` - Solve with step-by-step animation
- `POST /api/sudoku/validate` - Validate puzzle solution

### Pathfinding
- `POST /api/pathfinding/*` - Various pathfinding algorithms

### Game
- `POST /api/game/*` - Game-related endpoints

## Verification

To verify deployment is working:

1. **Check Health**: 
   ```bash
   curl https://your-app.onrender.com/health
   ```

2. **Test API**:
   ```bash
   curl -X POST https://your-app.onrender.com/api/sudoku/validate \
     -H "Content-Type: application/json" \
     -d '{"board": [[...]]}'
   ```

3. **Check Frontend**: Open `https://your-app.onrender.com` in browser

## Local Development

To test locally before deploying:

```bash
cd backend

# Build
./mvnw clean package

# Run (uses default PORT 8080)
java -jar target/*.jar

# Or run with custom port
PORT=3000 java -jar target/*.jar
```

Then open `http://localhost:8080` (or your custom port) in browser.

## Troubleshooting

### Build fails
- Clear Maven cache: `rm -rf backend/.mvn/wrapper/`
- Re-run: `./mvnw clean package`

### Port issues
- Check if port is already in use
- Set different PORT: `PORT=3001 java -jar target/*.jar`

### API not responding
- Check browser console for network errors
- Verify API base URL in browser DevTools Network tab
- Ensure backend is running

## File Structure

```
/backend
├── mvnw                    # Maven wrapper (Linux/Mac)
├── mvnw.cmd               # Maven wrapper (Windows)
├── .mvn/wrapper/          # Maven wrapper config
├── pom.xml                # Maven configuration
├── src/
│   └── main/java/
│       └── com/algo/server/
│           ├── AlgorithmServer.java  # Main server (port handling)
│           ├── AlgorithmController.java
│           ├── GameController.java
│           ├── SudokuController.java
│           └── solvers/   # Solver implementations
└── target/
    └── app.jar           # Built application (after mvnw clean package)

/frontend
├── index.html
├── config.js              # Configuration with dynamic API URL
├── sudoku-solver.js       # Sudoku visualization
├── styles.css
└── public/
    └── sudoku-visualizer.html  # Deprecated standalone version
```

## Notes

- Both build and start are environment-agnostic (no hardcoded hosts/ports)
- Single codebase works in development, staging, and production
- Frontend and backend are served from the same origin on Render
- Maven wrapper ensures reproducible builds across different environments
