package com.algo.models;

import java.util.*;

/**
 * Standard backtracking solver for Sudoku.
 * Records all steps for visualization.
 */
public class BacktrackingSolver implements ISudokuSolver {
    private static final int SIZE = 9;
    private static final int EMPTY = 0;
    private static final int SUBGRID_SIZE = 3;
    
    private int[][] board;
    private int nodeCount = 0;
    private List<SolverStep> steps = new ArrayList<>();
    
    public BacktrackingSolver(int[][] board) {
        this.board = copyBoard(board);
    }
    
    @Override
    public boolean isValidBoard() {
        for (int row = 0; row < SIZE; row++) {
            Set<Integer> seen = new HashSet<>();
            for (int col = 0; col < SIZE; col++) {
                int num = board[row][col];
                if (num != EMPTY) {
                    if (seen.contains(num) || num < 1 || num > 9) return false;
                    seen.add(num);
                }
            }
        }
        
        for (int col = 0; col < SIZE; col++) {
            Set<Integer> seen = new HashSet<>();
            for (int row = 0; row < SIZE; row++) {
                int num = board[row][col];
                if (num != EMPTY) {
                    if (seen.contains(num) || num < 1 || num > 9) return false;
                    seen.add(num);
                }
            }
        }
        
        for (int boxRow = 0; boxRow < SIZE; boxRow += SUBGRID_SIZE) {
            for (int boxCol = 0; boxCol < SIZE; boxCol += SUBGRID_SIZE) {
                Set<Integer> seen = new HashSet<>();
                for (int row = boxRow; row < boxRow + SUBGRID_SIZE; row++) {
                    for (int col = boxCol; col < boxCol + SUBGRID_SIZE; col++) {
                        int num = board[row][col];
                        if (num != EMPTY) {
                            if (seen.contains(num) || num < 1 || num > 9) return false;
                            seen.add(num);
                        }
                    }
                }
            }
        }
        
        return true;
    }
    
    @Override
    public List<SolverStep> solveWithSteps() {
        this.nodeCount = 0;
        this.steps = new ArrayList<>();
        backtrack();
        return steps;
    }
    
    private boolean backtrack() {
        nodeCount++;
        
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                if (board[row][col] == EMPTY) {
                    List<Integer> candidates = new ArrayList<>();
                    
                    for (int num = 1; num <= 9; num++) {
                        if (isValid(row, col, num)) {
                            candidates.add(num);
                        }
                    }
                    
                    // Record "trying" step
                    steps.add(new SolverStep(row, col, 0, "trying", new ArrayList<>(candidates)));
                    
                    for (int num : candidates) {
                        // Record placing
                        steps.add(new SolverStep(row, col, num, "placed"));
                        board[row][col] = num;
                        
                        if (backtrack()) {
                            return true;
                        }
                        
                        // Record backtrack
                        steps.add(new SolverStep(row, col, num, "backtrack"));
                        board[row][col] = EMPTY;
                    }
                    
                    return false;
                }
            }
        }
        
        // Puzzle solved
        steps.add(new SolverStep(-1, -1, 0, "solved"));
        return true;
    }
    
    private boolean isValid(int row, int col, int num) {
        for (int c = 0; c < SIZE; c++) {
            if (board[row][c] == num) return false;
        }
        
        for (int r = 0; r < SIZE; r++) {
            if (board[r][col] == num) return false;
        }
        
        int boxRow = row - row % SUBGRID_SIZE;
        int boxCol = col - col % SUBGRID_SIZE;
        for (int r = boxRow; r < boxRow + SUBGRID_SIZE; r++) {
            for (int c = boxCol; c < boxCol + SUBGRID_SIZE; c++) {
                if (board[r][c] == num) return false;
            }
        }
        
        return true;
    }
    
    @Override
    public int[][] getSolvedBoard() {
        return copyBoard(board);
    }
    
    @Override
    public int getNodeCount() {
        return nodeCount;
    }
    
    private int[][] copyBoard(int[][] original) {
        int[][] copy = new int[SIZE][SIZE];
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                copy[i][j] = original[i][j];
            }
        }
        return copy;
    }
}

/**
 * Backtracking with forward checking (naked singles).
 * More efficient due to constraint propagation.
 */
