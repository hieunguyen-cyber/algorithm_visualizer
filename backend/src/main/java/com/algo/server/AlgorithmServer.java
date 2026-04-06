package com.algo.server;

import com.algo.controllers.AlgorithmController;
import com.algo.controllers.GameController;
import com.algo.controllers.SudokuController;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * HTTP server for the Algorithm Visualizer application.
 * Provides REST API endpoints for pathfinding, game algorithms, and Sudoku.
 */
public class AlgorithmServer {
    private static final int PORT = getPort();
    private static final String API_BASE = "/api";
    
    /**
     * Get port from environment variable or use default.
     * Render provides PORT env variable, default to 8080 for local dev.
     */
    private static int getPort() {
        String portEnv = System.getenv("PORT");
        if (portEnv != null && !portEnv.isEmpty()) {
            try {
                return Integer.parseInt(portEnv);
            } catch (NumberFormatException e) {
                System.err.println("Invalid PORT env variable: " + portEnv);
            }
        }
        return 8080;
    }
    
    private HttpServer server;
    private AlgorithmController algorithmController;
    private GameController gameController;
    private SudokuController sudokuController;

    public AlgorithmServer() throws IOException {
        this.algorithmController = new AlgorithmController();
        this.gameController = new GameController();
        this.sudokuController = new SudokuController();
        this.server = HttpServer.create(new InetSocketAddress(PORT), 0);
        setupRoutes();
    }

    /**
     * Setup HTTP request routes.
     */
    private void setupRoutes() {
        // CORS preflight handler
        server.createContext("/", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                if ("OPTIONS".equals(exchange.getRequestMethod())) {
                    handleCORS(exchange);
                } else {
                    exchange.sendResponseHeaders(404, 0);
                    exchange.close();
                }
            }
        });

        // Pathfinding endpoints
        server.createContext(API_BASE + "/pathfinding", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                if ("OPTIONS".equals(exchange.getRequestMethod())) {
                    handleCORS(exchange);
                } else if ("POST".equals(exchange.getRequestMethod())) {
                    handlePathfinding(exchange);
                } else {
                    sendError(exchange, 405, "Method not allowed");
                }
            }
        });

        // Game endpoints
        server.createContext(API_BASE + "/game/init", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                if ("OPTIONS".equals(exchange.getRequestMethod())) {
                    handleCORS(exchange);
                } else if ("POST".equals(exchange.getRequestMethod())) {
                    handleGameInit(exchange);
                } else {
                    sendError(exchange, 405, "Method not allowed");
                }
            }
        });

        server.createContext(API_BASE + "/game/move", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                if ("OPTIONS".equals(exchange.getRequestMethod())) {
                    handleCORS(exchange);
                } else if ("POST".equals(exchange.getRequestMethod())) {
                    handleGameMove(exchange);
                } else {
                    sendError(exchange, 405, "Method not allowed");
                }
            }
        });

        server.createContext(API_BASE + "/game/ai-move", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                if ("OPTIONS".equals(exchange.getRequestMethod())) {
                    handleCORS(exchange);
                } else if ("POST".equals(exchange.getRequestMethod())) {
                    handleAIMove(exchange);
                } else {
                    sendError(exchange, 405, "Method not allowed");
                }
            }
        });

        server.createContext(API_BASE + "/game/state", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                if ("OPTIONS".equals(exchange.getRequestMethod())) {
                    handleCORS(exchange);
                } else if ("GET".equals(exchange.getRequestMethod())) {
                    handleGameState(exchange);
                } else {
                    sendError(exchange, 405, "Method not allowed");
                }
            }
        });

        server.createContext(API_BASE + "/game/reset", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                if ("OPTIONS".equals(exchange.getRequestMethod())) {
                    handleCORS(exchange);
                } else if ("POST".equals(exchange.getRequestMethod())) {
                    handleGameReset(exchange);
                } else {
                    sendError(exchange, 405, "Method not allowed");
                }
            }
        });

        // Sudoku endpoints
        server.createContext(API_BASE + "/sudoku/validate", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                if ("OPTIONS".equals(exchange.getRequestMethod())) {
                    handleCORS(exchange);
                } else if ("POST".equals(exchange.getRequestMethod())) {
                    handleSudokuValidate(exchange);
                } else {
                    sendError(exchange, 405, "Method not allowed");
                }
            }
        });

        server.createContext(API_BASE + "/sudoku/check-solvable", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                if ("OPTIONS".equals(exchange.getRequestMethod())) {
                    handleCORS(exchange);
                } else if ("POST".equals(exchange.getRequestMethod())) {
                    handleSudokuCheckSolvable(exchange);
                } else {
                    sendError(exchange, 405, "Method not allowed");
                }
            }
        });

        server.createContext(API_BASE + "/sudoku/solve", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                if ("OPTIONS".equals(exchange.getRequestMethod())) {
                    handleCORS(exchange);
                } else if ("POST".equals(exchange.getRequestMethod())) {
                    handleSudokuSolve(exchange);
                } else {
                    sendError(exchange, 405, "Method not allowed");
                }
            }
        });

        server.createContext(API_BASE + "/sudoku/solve-with-steps", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                if ("OPTIONS".equals(exchange.getRequestMethod())) {
                    handleCORS(exchange);
                } else if ("POST".equals(exchange.getRequestMethod())) {
                    handleSudokuSolveWithSteps(exchange);
                } else {
                    sendError(exchange, 405, "Method not allowed");
                }
            }
        });

        // Health check
        server.createContext("/health", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                sendResponse(exchange, 200, "{\"status\": \"ok\", \"message\": \"Backend is running\"}");
            }
        });
    }

    /**
     * Handle CORS preflight requests.
     */
    private void handleCORS(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
        exchange.getResponseHeaders().set("Access-Control-Max-Age", "3600");
        exchange.sendResponseHeaders(200, 0);
        exchange.close();
    }

    /**
     * Handle pathfinding requests.
     */
    private void handlePathfinding(HttpExchange exchange) throws IOException {
        if (!"POST".equals(exchange.getRequestMethod())) {
            sendError(exchange, 405, "Method not allowed");
            return;
        }

        String requestBody = readRequestBody(exchange);
        String response = algorithmController.pathfind(requestBody);
        sendResponse(exchange, 200, response);
    }

    /**
     * Handle game initialization.
     */
    private void handleGameInit(HttpExchange exchange) throws IOException {
        if (!"POST".equals(exchange.getRequestMethod())) {
            sendError(exchange, 405, "Method not allowed");
            return;
        }

        String requestBody = readRequestBody(exchange);
        String response = gameController.initGame(requestBody);
        sendResponse(exchange, 200, response);
    }

    /**
     * Handle player move.
     */
    private void handleGameMove(HttpExchange exchange) throws IOException {
        if (!"POST".equals(exchange.getRequestMethod())) {
            sendError(exchange, 405, "Method not allowed");
            return;
        }

        String requestBody = readRequestBody(exchange);
        String response = gameController.makeMove(requestBody);
        sendResponse(exchange, 200, response);
    }

    /**
     * Handle AI move.
     */
    private void handleAIMove(HttpExchange exchange) throws IOException {
        if (!"POST".equals(exchange.getRequestMethod())) {
            sendError(exchange, 405, "Method not allowed");
            return;
        }

        String requestBody = readRequestBody(exchange);
        String response = gameController.getAIMove(requestBody);
        sendResponse(exchange, 200, response);
    }

    /**
     * Handle get game state.
     */
    private void handleGameState(HttpExchange exchange) throws IOException {
        if (!"GET".equals(exchange.getRequestMethod())) {
            sendError(exchange, 405, "Method not allowed");
            return;
        }

        String response = gameController.getGameState("");
        sendResponse(exchange, 200, response);
    }

    /**
     * Handle game reset.
     */
    private void handleGameReset(HttpExchange exchange) throws IOException {
        if (!"POST".equals(exchange.getRequestMethod())) {
            sendError(exchange, 405, "Method not allowed");
            return;
        }

        String response = gameController.resetGame("");
        sendResponse(exchange, 200, response);
    }

    /**
     * Handle Sudoku validation.
     */
    private void handleSudokuValidate(HttpExchange exchange) throws IOException {
        if (!"POST".equals(exchange.getRequestMethod())) {
            sendError(exchange, 405, "Method not allowed");
            return;
        }

        String requestBody = readRequestBody(exchange);
        String response = sudokuController.validateBoard(requestBody);
        sendResponse(exchange, 200, response);
    }

    /**
     * Handle Sudoku solvability check.
     */
    private void handleSudokuCheckSolvable(HttpExchange exchange) throws IOException {
        if (!"POST".equals(exchange.getRequestMethod())) {
            sendError(exchange, 405, "Method not allowed");
            return;
        }

        String requestBody = readRequestBody(exchange);
        String response = sudokuController.checkSolvable(requestBody);
        sendResponse(exchange, 200, response);
    }

    /**
     * Handle Sudoku solving.
     */
    private void handleSudokuSolve(HttpExchange exchange) throws IOException {
        if (!"POST".equals(exchange.getRequestMethod())) {
            sendError(exchange, 405, "Method not allowed");
            return;
        }

        String requestBody = readRequestBody(exchange);
        String response = sudokuController.solveSudoku(requestBody);
        sendResponse(exchange, 200, response);
    }

    /**
     * Handle Sudoku solving with step-by-step visualization.
     */
    private void handleSudokuSolveWithSteps(HttpExchange exchange) throws IOException {
        if (!"POST".equals(exchange.getRequestMethod())) {
            sendError(exchange, 405, "Method not allowed");
            return;
        }

        String requestBody = readRequestBody(exchange);
        String response = sudokuController.solveWithSteps(requestBody);
        sendResponse(exchange, 200, response);
    }

    /**
     * Read request body.
     */
    private String readRequestBody(HttpExchange exchange) throws IOException {
        InputStream is = exchange.getRequestBody();
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[4096];
        while ((nRead = is.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        return buffer.toString(StandardCharsets.UTF_8);
    }

    /**
     * Send successful response.
     */
    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "POST, GET, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
        
        byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(responseBytes);
        os.close();
    }

    /**
     * Send error response.
     */
    private void sendError(HttpExchange exchange, int statusCode, String message) throws IOException {
        String error = "{\"error\": \"" + message + "\"}";
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        byte[] errorBytes = error.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, errorBytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(errorBytes);
        os.close();
    }

    /**
     * Start the server.
     */
    public void start() {
        server.setExecutor(null);
        server.start();
        System.out.println("\n" +
            "╔════════════════════════════════════════════╗\n" +
            "║   Algorithm Visualizer Server Started      ║\n" +
            "╚════════════════════════════════════════════╝\n");
        System.out.println("🔌 API Base:     http://localhost:" + PORT + "/api");
        System.out.println("💓 Health Check: http://localhost:" + PORT + "/health");
        System.out.println("🌐 Frontend:     http://localhost:8000");
        System.out.println("\n✅ Server is ready to accept requests\n");
    }

    public static void main(String[] args) {
        try {
            AlgorithmServer server = new AlgorithmServer();
            server.start();
        } catch (IOException e) {
            System.err.println("Failed to start server: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
