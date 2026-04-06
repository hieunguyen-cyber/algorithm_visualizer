# Docker Deployment Guide for Render

This backend is fully containerized and ready for deployment on Render.

## Docker Setup Overview

The project uses a **multi-stage Docker build**:
- **Stage 1**: Maven build environment (compiles JAR)
- **Stage 2**: Lightweight Java runtime (runs application)

## Files Included

1. **Dockerfile** - Multi-stage build configuration
2. **.dockerignore** - Excludes unnecessary files from build context
3. **pom.xml** - Maven configuration (unchanged, already optimal)
4. **AlgorithmServer.java** - PORT configured to read env variable

## Deploy on Render

### Step 1: Connect Repository
- Go to [render.com](https://render.com)
- Click "New +" → "Web Service"
- Connect GitHub repository

### Step 2: Configure Deployment

Select **Docker** environment and configure:

| Setting | Value |
|---------|-------|
| Root Directory | `backend` |
| Build Command | (Uses Dockerfile) |
| Start Command | (Uses ENTRYPOINT) |
| Instance Type | Standard (or suitable for your needs) |

### Step 3: Deploy
- Click "Create Web Service"
- Render will:
  1. Clone repository
  2. Build Docker image using Dockerfile
  3. Start container on assigned PORT
  4. Set PORT environment variable automatically

## How It Works

### Dockerfile Execution

```dockerfile
# Stage 1: Build (Maven compiles the project)
FROM maven:3.8.6-openjdk-11 AS builder
WORKDIR /build
COPY pom.xml src .mvn ./
RUN ./mvnw clean package -DskipTests

# Stage 2: Runtime (Lightweight JRE runs the JAR)
FROM openjdk:11-jre-slim
COPY --from=builder /build/target/algorithm-visualizer-*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### Port Configuration

The application **automatically reads the PORT environment variable**:

```java
private static int getPort() {
    String portEnv = System.getenv("PORT");
    if (portEnv != null && !portEnv.isEmpty()) {
        try {
            return Integer.parseInt(portEnv);
        } catch (NumberFormatException e) {
            System.err.println("Invalid PORT env variable: " + portEnv);
        }
    }
    return 8080;  // Default for local development
}
```

Render will set `PORT` automatically - no configuration needed.

### Health Check

The container includes a health check that pings `/health`:

```dockerfile
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
    CMD curl -f http://localhost:${PORT:-8080}/health || exit 1
```

This endpoint returns:
```json
{"status": "ok", "message": "Backend is running"}
```

## Build Output

The Maven build produces:
- **Primary JAR**: `target/algorithm-visualizer-1.0.0.jar` (329KB, including all dependencies)
- **Original JAR**: `target/original-algorithm-visualizer-1.0.0.jar` (excluded in final container)

The Dockerfile uses the primary JAR which includes all Gson dependencies and other required libraries.

## API Endpoints

Once deployed on Render, access:
- **Root**: `https://your-service.onrender.com`
- **Health**: `https://your-service.onrender.com/health`
- **Sudoku API**: `https://your-service.onrender.com/api/sudoku/*`
- **Pathfinding API**: `https://your-service.onrender.com/api/pathfinding/*`
- **Game API**: `https://your-service.onrender.com/api/game/*`

## Testing Locally

### With Docker (if Docker is running):

```bash
cd backend

# Build image
docker build -t algorithm-visualizer:latest .

# Run container
docker run -d -p 8080:8080 -e PORT=8080 algorithm-visualizer:latest

# Test health
curl http://localhost:8080/health
```

### Without Docker (using Maven):

```bash
cd backend

# Build
./mvnw clean package -DskipTests

# Run on custom port
PORT=9090 java -jar target/algorithm-visualizer-1.0.0.jar

# Test health
curl http://localhost:9090/health
```

## Build Process Timeline

1. **Render receives push**: Starts Docker build
2. **Pull builder image**: `maven:3.8.6-openjdk-11` (~400MB)
3. **Maven build**: `./mvnw clean package -DskipTests` (~60 seconds)
4. **Pull runtime image**: `openjdk:11-jre-slim` (~200MB)
5. **Copy JAR**: From builder to runtime
6. **Container starts**: Automatically with assigned PORT
7. **Health check**: Verifies endpoint responds
8. **Ready**: Service accessible at Render URL

## Optimization Notes

- **Multi-stage build**: Final image contains ONLY runtime (~500MB), not build tools
- **Skip tests during build**: Faster deployment (`-DskipTests`)
- **Maven wrapper**: Ensures consistent Maven version
- **Lightweight JRE**: `openjdk:11-jre-slim` is smaller than full JDK
- **Single JAR**: Fat JAR includes all dependencies (no classpath issues)

## Troubleshooting

### Build fails
- Check Maven output in Render logs
- Verify `pom.xml` is valid
- Ensure Java source files compile (Java 11)

### Container won't start
- Check Render logs for error messages
- Verify main class: `com.algo.server.AlgorithmServer`
- Ensure JAR built successfully

### Port not accessible
- Verify PORT environment variable is set in Render
- Check if service is listening on correct port
- Review health check in Dockerfile

### High memory usage
- Consider upgrading instance type in Render
- Reduce JVM heap: Add JVM options if needed
- Profile application with Render metrics

## Verification Checklist

Before deployment, confirm:

- ✅ Dockerfile exists in `/backend`
- ✅ `.dockerignore` configured
- ✅ `pom.xml` has shade plugin
- ✅ `AlgorithmServer.java` reads PORT env variable
- ✅ Health endpoint at `/health` works
- ✅ JAR builds successfully: `./mvnw clean package`
- ✅ JAR runs on custom PORT: `PORT=9090 java -jar target/*.jar`

## Related Documentation

- [RENDER_DEPLOY.md](../RENDER_DEPLOY.md) - Non-Docker deployment guide
- Render Documentation: https://docs.render.com/docker
- Docker Best Practices: https://docs.docker.com/develop/dev-best-practices/

---

**Status**: ✅ Ready for Render Docker deployment
