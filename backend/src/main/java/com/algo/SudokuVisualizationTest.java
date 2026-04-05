package com.algo;

import com.algo.controllers.SudokuController;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Integration test for Sudoku solver with visualization.
 * Tests all solving algorithms and step tracking.
 */
public class SudokuVisualizationTest {
    
    private static final Gson gson = new Gson();
    
    // Sample Sudoku puzzles
    private static final int[][] EASY_PUZZLE = {
        {5,3,0,0,7,0,0,0,0},
        {6,0,0,1,9,5,0,0,0},
        {0,9,8,0,0,0,0,6,0},
        {8,0,0,0,6,0,0,0,3},
        {4,0,0,8,0,3,0,0,1},
        {7,0,0,0,2,0,0,0,6},
        {0,6,0,0,0,0,2,8,0},
        {0,0,0,4,1,9,0,0,5},
        {0,0,0,0,8,0,0,7,9}
    };

    private static final int[][] MEDIUM_PUZZLE = {
        {3,0,6,5,0,8,4,0,0},
        {5,2,0,0,0,0,0,0,0},
        {0,8,7,0,0,0,0,3,1},
        {0,0,3,0,1,0,0,8,0},
        {9,0,0,8,6,3,0,0,5},
        {0,5,0,0,9,0,6,0,0},
        {1,3,0,0,0,0,2,5,0},
        {0,0,0,0,0,0,0,7,4},
        {0,0,5,2,0,6,3,0,0}
    };

    public static void main(String[] args) {
        System.out.println("═══════════════════════════════════════════════════════════════");
        System.out.println("    Sudoku Solver Visualization Integration Test");
        System.out.println("═══════════════════════════════════════════════════════════════\n");

        SudokuController controller = new SudokuController();
        
        // Test 1: Validate board
        testValidateBoard(controller);
        
        // Test 2: Check solvability
        testCheckSolvable(controller);
        
        // Test 3: Solve with different algorithms
        testSolveWithAlgorithms(controller);
        
        // Test 4: Compare algorithm performance
        compareAlgorithmPerformance(controller);
        
        System.out.println("\n═══════════════════════════════════════════════════════════════");
        System.out.println("    ✅ All tests completed successfully!");
        System.out.println("═══════════════════════════════════════════════════════════════");
    }

    /**
     * Test board validation.
     */
    private static void testValidateBoard(SudokuController controller) {
        System.out.println("\n📋 TEST 1: Board Validation");
        System.out.println("───────────────────────────────────────────────────────────────");
        
        String request = createRequest(EASY_PUZZLE, null);
        String response = controller.validateBoard(request);
        
        JsonObject result = JsonParser.parseString(response).getAsJsonObject();
        
        if (result.get("success").getAsBoolean()) {
            System.out.println("✅ Easy puzzle validation: PASSED");
            System.out.println("   Message: " + result.get("message").getAsString());
        } else {
            System.out.println("❌ Validation failed: " + result.get("error").getAsString());
        }
    }

    /**
     * Test solvability check.
     */
    private static void testCheckSolvable(SudokuController controller) {
        System.out.println("\n📋 TEST 2: Solvability Check");
        System.out.println("───────────────────────────────────────────────────────────────");
        
        String request = createRequest(MEDIUM_PUZZLE, null);
        String response = controller.checkSolvable(request);
        
        JsonObject result = JsonParser.parseString(response).getAsJsonObject();
        
        if (result.get("success").getAsBoolean()) {
            System.out.println("✅ Medium puzzle solvability: PASSED");
            System.out.println("   Status: " + result.get("message").getAsString());
        } else {
            System.out.println("❌ Check failed: " + result.get("error").getAsString());
        }
    }

    /**
     * Test solving with different algorithms.
     */
    private static void testSolveWithAlgorithms(SudokuController controller) {
        System.out.println("\n📋 TEST 3: Solving with Different Algorithms");
        System.out.println("───────────────────────────────────────────────────────────────");
        
        String[] algorithms = {"backtracking", "forward-checking", "mrv"};
        
        for (String algorithm : algorithms) {
            System.out.println("\n🔧 Testing algorithm: " + algorithm.toUpperCase());
            
            String request = createRequest(EASY_PUZZLE.clone(), algorithm);
            String response = controller.solveWithSteps(request);
            
            JsonObject result = JsonParser.parseString(response).getAsJsonObject();
            
            if (result.get("success").getAsBoolean()) {
                int stepCount = result.get("stepCount").getAsInt();
                int nodeCount = result.get("nodeCount").getAsInt();
                long elapsed = result.get("elapsedMs").getAsLong();
                
                System.out.println("   ✅ Solving successful");
                System.out.println("   Steps: " + stepCount);
                System.out.println("   Nodes explored: " + nodeCount);
                System.out.println("   Time: " + elapsed + "ms");
                
                // Show first few steps
                JsonArray stepsArray = result.getAsJsonArray("steps");
                if (stepsArray.size() > 0) {
                    System.out.println("\n   📍 Sample Steps (first 5):");
                    for (int i = 0; i < Math.min(5, stepsArray.size()); i++) {
                        JsonObject step = stepsArray.get(i).getAsJsonObject();
                        System.out.println("      [" + (i+1) + "] " + formatStep(step));
                    }
                }
            } else {
                System.out.println("   ❌ Failed: " + result.get("error").getAsString());
            }
        }
    }

    /**
     * Compare performance of different algorithms.
     */
    private static void compareAlgorithmPerformance(SudokuController controller) {
        System.out.println("\n📋 TEST 4: Algorithm Performance Comparison");
        System.out.println("───────────────────────────────────────────────────────────────");
        
        String[] algorithms = {"backtracking", "forward-checking", "mrv"};
        long[] nodesCounts = new long[3];
        long[] elapsedTimes = new long[3];
        int[] stepCounts = new int[3];
        
        for (int i = 0; i < algorithms.length; i++) {
            String request = createRequest(MEDIUM_PUZZLE.clone(), algorithms[i]);
            String response = controller.solveWithSteps(request);
            
            JsonObject result = JsonParser.parseString(response).getAsJsonObject();
            
            if (result.get("success").getAsBoolean()) {
                nodesCounts[i] = result.get("nodeCount").getAsLong();
                elapsedTimes[i] = result.get("elapsedMs").getAsLong();
                stepCounts[i] = result.get("stepCount").getAsInt();
            }
        }
        
        // Display comparison table
        System.out.println("\n┌──────────────────┬──────────────┬──────────────┬────────────┐");
        System.out.println("│ Algorithm        │ Nodes        │ Steps        │ Time (ms)  │");
        System.out.println("├──────────────────┼──────────────┼──────────────┼────────────┤");
        
        for (int i = 0; i < algorithms.length; i++) {
            System.out.printf("│ %-16s │ %12d │ %12d │ %10d │\n",
                    capitalize(algorithms[i]),
                    nodesCounts[i],
                    stepCounts[i],
                    elapsedTimes[i]);
        }
        
        System.out.println("└──────────────────┴──────────────┴──────────────┴────────────┘");
        
        // Find best performer
        int bestIndex = 0;
        long lowestNodes = nodesCounts[0];
        for (int i = 1; i < nodesCounts.length; i++) {
            if (nodesCounts[i] < lowestNodes) {
                lowestNodes = nodesCounts[i];
                bestIndex = i;
            }
        }
        
        System.out.println("\n🏆 Most efficient: " + capitalize(algorithms[bestIndex]) + 
                         " (" + nodesCounts[bestIndex] + " nodes)");
    }

    /**
     * Create request JSON.
     */
    private static String createRequest(int[][] board, String algorithm) {
        JsonObject request = new JsonObject();
        
        // Convert board to JSON
        JsonArray boardArray = new JsonArray();
        for (int i = 0; i < board.length; i++) {
            JsonArray row = new JsonArray();
            for (int j = 0; j < board[i].length; j++) {
                row.add(board[i][j]);
            }
            boardArray.add(row);
        }
        
        request.add("board", boardArray);
        if (algorithm != null) {
            request.addProperty("algorithm", algorithm);
        }
        
        return request.toString();
    }

    /**
     * Format a step for display.
     */
    private static String formatStep(JsonObject step) {
        String action = step.get("action").getAsString();
        
        if (action.equals("solved")) {
            return "🎉 Puzzle solved!";
        } else if (action.equals("placed")) {
            int row = step.get("row").getAsInt();
            int col = step.get("col").getAsInt();
            int value = step.get("value").getAsInt();
            return String.format("Place %d at [%d,%d]", value, row, col);
        } else if (action.equals("backtrack")) {
            int row = step.get("row").getAsInt();
            int col = step.get("col").getAsInt();
            return String.format("Backtrack from [%d,%d]", row, col);
        } else if (action.equals("trying")) {
            int row = step.get("row").getAsInt();
            int col = step.get("col").getAsInt();
            JsonArray candidates = step.getAsJsonArray("candidates");
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < Math.min(3, candidates.size()); i++) {
                if (i > 0) sb.append(",");
                sb.append(candidates.get(i).getAsInt());
            }
            return String.format("Try [%s%s] at [%d,%d]", 
                    sb.toString(), candidates.size() > 3 ? "..." : "", row, col);
        }
        
        return action;
    }

    /**
     * Capitalize first letter.
     */
    private static String capitalize(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}
