package com.algo.models;

import java.util.*;

/**
 * Sudoku solver using backtracking algorithm.
 * - Validates input board
 * - Checks if solution exists
 * - Returns solved board
 */
public class SudokuSolver {
    private static final int SIZE = 9;
    private static final int EMPTY = 0;
    private static final int SUBGRID_SIZE = 3;
    
    private int[][] board;
    private int nodeCount = 0;
    private List<int[][]> allSolutions = new ArrayList<>();
    private boolean findAll = false;
    
    public SudokuSolver(int[][] board) {
        this.board = copyBoard(board);
        this.nodeCount = 0;
    }
    
    /**
     * Validate the input board.
     * - Check for duplicates in rows, columns, 3x3 boxes
     * - But allow EMPTY (0) cells
     */
    public boolean isValidBoard() {
        // Check rows
        for (int row = 0; row < SIZE; row++) {
            Set<Integer> seen = new HashSet<>();
            for (int col = 0; col < SIZE; col++) {
                int num = board[row][col];
                if (num != EMPTY) {
                    if (seen.contains(num)) return false;
                    if (num < 1 || num > 9) return false;
                    seen.add(num);
                }
            }
        }
        
        // Check columns
        for (int col = 0; col < SIZE; col++) {
            Set<Integer> seen = new HashSet<>();
            for (int row = 0; row < SIZE; row++) {
                int num = board[row][col];
                if (num != EMPTY) {
                    if (seen.contains(num)) return false;
                    if (num < 1 || num > 9) return false;
                    seen.add(num);
                }
            }
        }
        
        // Check 3x3 boxes
        for (int boxRow = 0; boxRow < SIZE; boxRow += SUBGRID_SIZE) {
            for (int boxCol = 0; boxCol < SIZE; boxCol += SUBGRID_SIZE) {
                Set<Integer> seen = new HashSet<>();
                for (int row = boxRow; row < boxRow + SUBGRID_SIZE; row++) {
                    for (int col = boxCol; col < boxCol + SUBGRID_SIZE; col++) {
                        int num = board[row][col];
                        if (num != EMPTY) {
                            if (seen.contains(num)) return false;
                            if (num < 1 || num > 9) return false;
                            seen.add(num);
                        }
                    }
                }
            }
        }
        
        return true;
    }
    
    /**
     * Check if a number is valid at position row, col.
     */
    private boolean isValid(int row, int col, int num) {
        // Check row
        for (int c = 0; c < SIZE; c++) {
            if (board[row][c] == num) return false;
        }
        
        // Check column
        for (int r = 0; r < SIZE; r++) {
            if (board[r][col] == num) return false;
        }
        
        // Check 3x3 box
        int boxRow = row - row % SUBGRID_SIZE;
        int boxCol = col - col % SUBGRID_SIZE;
        for (int r = boxRow; r < boxRow + SUBGRID_SIZE; r++) {
            for (int c = boxCol; c < boxCol + SUBGRID_SIZE; c++) {
                if (board[r][c] == num) return false;
            }
        }
        
        return true;
    }
    
    /**
     * Backtracking solver.
     * Returns true if solution found.
     */
    public boolean solve() {
        this.nodeCount = 0;
        this.allSolutions = new ArrayList<>();
        this.findAll = false;
        
        return backtrack();
    }
    
    /**
     * Backtracking with solution counting.
     * Returns true if at least one solution exists.
     */
    public boolean hasUniqueSolution() {
        this.nodeCount = 0;
        this.allSolutions = new ArrayList<>();
        this.findAll = true;  // Find all solutions
        
        int[][] boardCopy = copyBoard(this.board);
        this.board = boardCopy;
        
        backtrack();
        
        // Return true if exactly one solution
        System.out.println("Found " + allSolutions.size() + " solutions");
        return allSolutions.size() == 1;
    }
    
    /**
     * Check if solution exists (at least one).
     */
    public boolean hasSolution() {
        this.nodeCount = 0;
        this.allSolutions = new ArrayList<>();
        this.findAll = false;  // Stop after finding one
        
        int[][] boardCopy = copyBoard(this.board);
        this.board = boardCopy;
        
        return backtrack();
    }
    
    /**
     * Core backtracking algorithm.
     */
    private boolean backtrack() {
        nodeCount++;
        
        // Find next empty cell
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                if (board[row][col] == EMPTY) {
                    // Try each number 1-9
                    for (int num = 1; num <= 9; num++) {
                        if (isValid(row, col, num)) {
                            board[row][col] = num;
                            
                            if (backtrack()) {
                                if (!findAll) return true;  // Stop after first solution
                            }
                            
                            board[row][col] = EMPTY;  // Backtrack
                        }
                    }
                    return false;
                }
            }
        }
        
        // No empty cell found - board is complete
        if (findAll && allSolutions.size() < 2) {  // Store solution
            allSolutions.add(copyBoard(this.board));
        }
        return true;
    }
    
    /**
     * Get solved board.
     */
    public int[][] getSolvedBoard() {
        return copyBoard(this.board);
    }
    
    /**
     * Get node count from solver.
     */
    public int getNodeCount() {
        return nodeCount;
    }
    
    /**
     * Copy a 2D array.
     */
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
