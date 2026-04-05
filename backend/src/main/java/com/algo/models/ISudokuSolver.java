package com.algo.models;

import java.util.List;

/**
 * Solver interface for multiple algorithm implementations.
 */
public interface ISudokuSolver {
    /**
     * Verify the board is valid before solving.
     */
    boolean isValidBoard();
    
    /**
     * Solve and collect step-by-step visualization data.
     * Returns list of SolverSteps.
     */
    List<SolverStep> solveWithSteps();
    
    /**
     * Get the solved board after solving.
     */
    int[][] getSolvedBoard();
    
    /**
     * Get number of nodes explored during solving.
     */
    int getNodeCount();
}
