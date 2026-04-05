package com.algo.controllers;

import com.algo.models.*;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;

import java.util.List;

/**
 * Controller for Sudoku validation and solving with visualization.
 */
public class SudokuController {
    private final Gson gson;
    
    public SudokuController() {
        this.gson = new Gson();
    }
    
    /**
     * Validate Sudoku board input.
     * Request: { "board": [[row1], [row2], ..., [row9]] }
     */
    public String validateBoard(String requestBody) {
        try {
            JsonObject request = gson.fromJson(requestBody, JsonObject.class);
            int[][] board = jsonToBoard(request.getAsJsonArray("board"));
            
            if (board == null || board.length != 9 || board[0].length != 9) {
                return errorResponse("Invalid board size (must be 9x9)");
            }
            
            BacktrackingSolver solver = new BacktrackingSolver(board);
            
            if (!solver.isValidBoard()) {
                return errorResponse("Invalid Sudoku configuration: duplicate numbers in row/column/box");
            }
            
            JsonObject response = new JsonObject();
            response.addProperty("success", true);
            response.addProperty("message", "Board is valid");
            response.addProperty("valid", true);
            
            return gson.toJson(response);
        } catch (IllegalArgumentException | NullPointerException e) {
            return errorResponse("Error validating board: " + e.getMessage());
        }
    }
    
    /**
     * Check if board has a solution.
     * Request: { "board": [...] }
     */
    public String checkSolvable(String requestBody) {
        try {
            JsonObject request = gson.fromJson(requestBody, JsonObject.class);
            int[][] board = jsonToBoard(request.getAsJsonArray("board"));
            
            if (board == null || board.length != 9 || board[0].length != 9) {
                return errorResponse("Invalid board size (must be 9x9)");
            }
            
            BacktrackingSolver solver = new BacktrackingSolver(board);
            
            if (!solver.isValidBoard()) {
                return errorResponse("Invalid Sudoku configuration");
            }
            
            System.out.println("🔍 Checking if board is valid...");
            
            JsonObject response = new JsonObject();
            response.addProperty("success", true);
            response.addProperty("solvable", true);
            response.addProperty("message", "Board configuration is valid");
            
            return gson.toJson(response);
        } catch (IllegalArgumentException | NullPointerException e) {
            return errorResponse("Error checking solvability: " + e.getMessage());
        }
    }
    
    /**
     * Solve a Sudoku board and return the solution.
     * Request: { "board": [...] }
     */
    public String solveSudoku(String requestBody) {
        try {
            JsonObject request = gson.fromJson(requestBody, JsonObject.class);
            int[][] board = jsonToBoard(request.getAsJsonArray("board"));
            
            if (board == null || board.length != 9 || board[0].length != 9) {
                return errorResponse("Invalid board size");
            }
            
            BacktrackingSolver solver = new BacktrackingSolver(board);
            
            if (!solver.isValidBoard()) {
                return errorResponse("Invalid starting board");
            }
            
            System.out.println("🧩 Solving Sudoku...");
            long startTime = System.currentTimeMillis();
            
            solver.solveWithSteps();
            long elapsed = System.currentTimeMillis() - startTime;
            
            JsonObject response = new JsonObject();
            response.addProperty("success", true);
            response.addProperty("solved", true);
            response.add("board", boardToJson(solver.getSolvedBoard()));
            response.addProperty("nodeCount", solver.getNodeCount());
            response.addProperty("elapsedMs", elapsed);
            
            System.out.println("✅ Solved in " + elapsed + "ms, nodes=" + solver.getNodeCount());
            
            return gson.toJson(response);
        } catch (IllegalArgumentException | NullPointerException e) {
            return errorResponse("Error solving Sudoku: " + e.getMessage());
        }
    }
    
    /**
     * Solve with step-by-step visualization data.
     * Request: { "board": [...], "algorithm": "backtracking|forward-checking|mrv" }
     * 
     * Returns: { "success": true, "steps": [...], "solved": true, "nodeCount": N, "elapsedMs": N }
     */
    public String solveWithSteps(String requestBody) {
        try {
            JsonObject request = gson.fromJson(requestBody, JsonObject.class);
            int[][] board = jsonToBoard(request.getAsJsonArray("board"));
            String algorithm = request.has("algorithm") ? 
                request.get("algorithm").getAsString() : "backtracking";
            
            if (board == null || board.length != 9 || board[0].length != 9) {
                return errorResponse("Invalid board size");
            }
            
            System.out.println("🧩 Solving Sudoku with algorithm: " + algorithm);
            long startTime = System.currentTimeMillis();
            
            // Select solver based on algorithm
            ISudokuSolver solver = createSolver(algorithm, board);
            
            if (!solver.isValidBoard()) {
                return errorResponse("Invalid starting board");
            }
            
            // Solve and collect steps
            List<SolverStep> steps = solver.solveWithSteps();
            long elapsed = System.currentTimeMillis() - startTime;
            
            JsonObject response = new JsonObject();
            response.addProperty("success", true);
            response.addProperty("solved", true);
            response.addProperty("algorithm", algorithm);
            response.add("steps", stepsToJson(steps));
            response.add("board", boardToJson(solver.getSolvedBoard()));
            response.addProperty("nodeCount", solver.getNodeCount());
            response.addProperty("elapsedMs", elapsed);
            response.addProperty("stepCount", steps.size());
            
            System.out.println("✅ Solved in " + elapsed + "ms, " + steps.size() + " steps");
            
            return gson.toJson(response);
        } catch (IllegalArgumentException | NullPointerException e) {
            return errorResponse("Error solving Sudoku: " + e.getMessage());
        }
    }
    
    /**
     * Create appropriate solver based on algorithm name.
     */
    private ISudokuSolver createSolver(String algorithm, int[][] board) {
        switch (algorithm.toLowerCase()) {
            case "forward-checking":
                return new BacktrackingWithForwardCheckingSolver(board);
            case "mrv":
                return new BacktrackingWithMRVSolver(board);
            default:
                return new BacktrackingSolver(board);
        }
    }
    
    /**
     * Convert solver steps to JSON array.
     */
    private JsonArray stepsToJson(List<SolverStep> steps) {
        JsonArray stepsArray = new JsonArray();
        for (SolverStep step : steps) {
            JsonObject stepObj = new JsonObject();
            stepObj.addProperty("row", step.row);
            stepObj.addProperty("col", step.col);
            stepObj.addProperty("value", step.value);
            stepObj.addProperty("action", step.action);
            
            if (step.candidates != null && !step.candidates.isEmpty()) {
                JsonArray candidatesArray = new JsonArray();
                for (Integer candidate : step.candidates) {
                    candidatesArray.add(candidate);
                }
                stepObj.add("candidates", candidatesArray);
            }
            
            stepsArray.add(stepObj);
        }
        return stepsArray;
    }
    
    /**
     * Convert JSON array to 2D int board.
     */
    private int[][] jsonToBoard(JsonArray jsonArray) {
        try {
            int[][] board = new int[9][9];
            for (int i = 0; i < 9; i++) {
                JsonArray row = jsonArray.get(i).getAsJsonArray();
                for (int j = 0; j < 9; j++) {
                    board[i][j] = row.get(j).getAsInt();
                }
            }
            return board;
        } catch (Exception e) {
            System.err.println("Error converting JSON to board: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Convert 2D int board to JSON array.
     */
    private JsonArray boardToJson(int[][] board) {
        JsonArray boardArray = new JsonArray();
        for (int i = 0; i < 9; i++) {
            JsonArray row = new JsonArray();
            for (int j = 0; j < 9; j++) {
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
        error.addProperty("success", false);
        error.addProperty("error", message);
        return gson.toJson(error);
    }
}
