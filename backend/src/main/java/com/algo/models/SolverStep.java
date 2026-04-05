package com.algo.models;

import java.util.*;

/**
 * Represents a single step in the Sudoku solving process.
 * Used for step-by-step visualization.
 */
public class SolverStep {
    public int row;
    public int col;
    public int value;
    public String action;  // "trying", "placed", "backtrack", "solved"
    public List<Integer> candidates;  // Numbers being tried at this cell
    
    public SolverStep(int row, int col, int value, String action) {
        this.row = row;
        this.col = col;
        this.value = value;
        this.action = action;
        this.candidates = new ArrayList<>();
    }
    
    public SolverStep(int row, int col, int value, String action, List<Integer> candidates) {
        this.row = row;
        this.col = col;
        this.value = value;
        this.action = action;
        this.candidates = candidates;
    }
}
