package com.algo.controllers;

import com.algo.algorithms.AlphaBeta;
import com.algo.algorithms.Minimax;
import com.algo.models.GameState;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * Controller for game/AI algorithm requests (Minimax, Alpha-Beta).
 */
public class GameController {
    private Gson gson;
    private GameState currentGame;

    public GameController() {
        this.gson = new Gson();
    }

    /**
     * Initialize a new game with configurable board dimensions.
     * Request: { "boardWidth": int, "boardHeight": int, "winCondition": int }
     */
    public String initGame(String requestBody) {
        try {
            JsonObject request = gson.fromJson(requestBody, JsonObject.class);
            
            // For now, create square boards (GameState limitation)
            // Use the larger dimension to ensure win condition fits
            int boardWidth = request.has("boardWidth") ? request.get("boardWidth").getAsInt() : 3;
            int boardHeight = request.has("boardHeight") ? request.get("boardHeight").getAsInt() : 3;
            int winCondition = request.has("winCondition") ? request.get("winCondition").getAsInt() : 3;
            
            // Create square board matching the larger dimension
            int boardSize = Math.max(boardWidth, boardHeight);
            
            currentGame = new GameState(boardSize);
            
            // Validate win condition
            if (winCondition < 3 || winCondition > 5) {
                winCondition = 3;
            }
            
            System.out.println("🎮 Game initialized: " + boardWidth + "x" + boardHeight + " board (size=" + boardSize + "), win=" + winCondition);
            
            JsonObject response = new JsonObject();
            response.addProperty("success", true);
            response.addProperty("message", "Game initialized");
            response.addProperty("boardWidth", boardWidth);
            response.addProperty("boardHeight", boardHeight);
            response.addProperty("boardSize", boardSize);
            response.addProperty("winCondition", winCondition);
            
            return gson.toJson(response);
        } catch (Exception e) {
            System.err.println("❌ Error initializing game: " + e.getMessage());
            return errorResponse("Error initializing game: " + e.getMessage());
        }
    }

    /**
     * Make a move in the current game.
     * Request: { "row": 0, "col": 1, "player": 1 }
     */
    public String makeMove(String requestBody) {
        try {
            JsonObject request = gson.fromJson(requestBody, JsonObject.class);
            int row = request.get("row").getAsInt();
            int col = request.get("col").getAsInt();
            int player = request.get("player").getAsInt();

            if (currentGame == null) {
                return errorResponse("Game not initialized");
            }

            if (!currentGame.isValidMove(row, col)) {
                return errorResponse("Invalid move");
            }

            currentGame.makeMove(row, col, player);

            JsonObject response = new JsonObject();
            response.addProperty("success", true);
            response.add("board", boardToJson(currentGame.getBoard()));
            response.addProperty("gameOver", currentGame.isGameOver());
            
            int winner = currentGame.getWinner();
            if (winner != GameState.EMPTY) {
                response.addProperty("winner", winner == GameState.AI ? "AI" : "Human");
            }

            return gson.toJson(response);
        } catch (Exception e) {
            return errorResponse("Error making move: " + e.getMessage());
        }
    }

    /**
     * Get AI move using Minimax or Alpha-Beta.
     * Request: { "algorithm": "minimax|alphabeta", "depth": number }
     * 
     * CRITICAL: Depth limits to prevent exponential explosion on larger boards
     * - 3x3: max depth 5
     * - 4x4: max depth 4  
     * - 5x5: max depth 3
     * - 6x6+: max depth 2
     */
    public String getAIMove(String requestBody) {
        try {
            JsonObject request = gson.fromJson(requestBody, JsonObject.class);
            String algorithm = request.has("algorithm") ? 
                request.get("algorithm").getAsString() : "minimax";
            int requestedDepth = request.has("depth") ? 
                request.get("depth").getAsInt() : 5;

            if (currentGame == null) {
                return errorResponse("Game not initialized");
            }

            // Enforce maximum depth limits based on board size
            int boardArea = currentGame.getBoardSize() * currentGame.getBoardSize();
            int maxDepth = getMaxDepthForBoardSize(boardArea);
            int actualDepth = Math.min(requestedDepth, maxDepth);
            
            System.out.println("🤖 AI Move: algorithm=" + algorithm + ", requested depth=" + requestedDepth + ", actual depth=" + actualDepth);

            int[] aiMove;
            int treeNodeCount = 0;
            int prunedCount = 0;

            if ("alphabeta".equalsIgnoreCase(algorithm)) {
                AlphaBeta ab = new AlphaBeta(actualDepth);
                aiMove = ab.findBestMove(currentGame);
                treeNodeCount = ab.getTreeNodes().size();
                prunedCount = ab.getPrunedCount();
            } else {
                Minimax minimax = new Minimax(actualDepth);
                aiMove = minimax.findBestMove(currentGame);
                treeNodeCount = minimax.getNodeCount();
            }

            if (aiMove == null) {
                return errorResponse("No valid move available");
            }

            // Make the AI move
            currentGame.makeMove(aiMove[0], aiMove[1], GameState.AI);

            JsonObject response = new JsonObject();
            response.addProperty("success", true);
            response.addProperty("algorithm", algorithm);
            response.addProperty("depth", actualDepth);
            response.addProperty("row", aiMove[0]);
            response.addProperty("col", aiMove[1]);
            response.add("board", boardToJson(currentGame.getBoard()));
            response.addProperty("gameOver", currentGame.isGameOver());
            response.addProperty("treeNodeCount", treeNodeCount);
            response.addProperty("prunedCount", prunedCount);

            int winner = currentGame.getWinner();
            if (winner != GameState.EMPTY) {
                response.addProperty("winner", winner == GameState.AI ? "AI" : "Human");
            }

            return gson.toJson(response);
        } catch (Exception e) {
            return errorResponse("Error getting AI move: " + e.getMessage());
        }
    }

    /**
     * Calculate maximum safe depth based on board size.
     * Prevents exponential explosion on larger boards.
     */
    private int getMaxDepthForBoardSize(int boardArea) {
        if (boardArea <= 9) return 5;     // 3x3: depth 5
        if (boardArea <= 16) return 4;    // 4x4: depth 4
        if (boardArea <= 25) return 3;    // 5x5: depth 3
        if (boardArea <= 36) return 3;    // 6x6: depth 3
        return 2;                         // 7x7+: depth 2
    }

    /**
     * Get current game state.
     */
    public String getGameState(String requestBody) {
        try {
            if (currentGame == null) {
                return errorResponse("Game not initialized");
            }

            JsonObject response = new JsonObject();
            response.add("board", boardToJson(currentGame.getBoard()));
            response.addProperty("gameOver", currentGame.isGameOver());
            response.addProperty("moveCount", currentGame.getMoveCount());
            
            int winner = currentGame.getWinner();
            if (winner != GameState.EMPTY) {
                response.addProperty("winner", winner == GameState.AI ? "AI" : "Human");
            }

            return gson.toJson(response);
        } catch (Exception e) {
            return errorResponse("Error getting game state: " + e.getMessage());
        }
    }

    /**
     * Reset the game.
     */
    public String resetGame(String requestBody) {
        try {
            if (currentGame == null) {
                return errorResponse("Game not initialized");
            }

            currentGame.reset();

            JsonObject response = new JsonObject();
            response.addProperty("success", true);
            response.add("board", boardToJson(currentGame.getBoard()));

            return gson.toJson(response);
        } catch (Exception e) {
            return errorResponse("Error resetting game: " + e.getMessage());
        }
    }

    /**
     * Convert board to JSON format.
     */
    private JsonArray boardToJson(int[][] board) {
        JsonArray boardArray = new JsonArray();
        for (int i = 0; i < board.length; i++) {
            JsonArray row = new JsonArray();
            for (int j = 0; j < board[i].length; j++) {
                row.add(board[i][j]);
            }
            boardArray.add(row);
        }
        return boardArray;
    }

    /**
     * Create error response.
     */
    private String errorResponse(String message) {
        JsonObject error = new JsonObject();
        error.addProperty("error", message);
        return gson.toJson(error);
    }
}
