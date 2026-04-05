package com.algo.models;

import java.util.*;

/**
 * Standard backtracking solver for Sudoku.
 * Records all steps for visualization.
 */
public class BacktrackingWithForwardCheckingSolver implements ISudokuSolver {
    private static final int SIZE = 9;
    private static final int EMPTY = 0;
    private static final int SUBGRID_SIZE = 3;
    
    private int[][] board;
    private Set<Integer>[][] candidates;  // Possible values for each cell
    private int nodeCount = 0;
    private List<SolverStep> steps = new ArrayList<>();
    
    @SuppressWarnings("unchecked")
    public BacktrackingWithForwardCheckingSolver(int[][] board) {
        this.board = copyBoard(board);
        this.candidates = new Set[SIZE][SIZE];
        initializeCandidates();
    }
    
    @SuppressWarnings("unchecked")
    private void initializeCandidates() {
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                if (board[row][col] == EMPTY) {
                    candidates[row][col] = getPossibleValues(row, col);
                } else {
                    candidates[row][col] = new HashSet<>();
                }
            }
        }
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
    
    @SuppressWarnings("unchecked")
    private boolean backtrack() {
        nodeCount++;
        
        // Find cell with fewest candidates (forward checking)
        int minRow = -1, minCol = -1, minCount = 10;
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                if (board[row][col] == EMPTY) {
                    int count = candidates[row][col].size();
                    if (count == 0) return false;  // No solution possible
                    if (count < minCount) {
                        minCount = count;
                        minRow = row;
                        minCol = col;
                    }
                }
            }
        }
        
        if (minRow == -1) {
            steps.add(new SolverStep(-1, -1, 0, "solved"));
            return true;
        }
        
        List<Integer> tries = new ArrayList<>(candidates[minRow][minCol]);
        steps.add(new SolverStep(minRow, minCol, 0, "trying", new ArrayList<>(tries)));
        
        for (int num : tries) {
            board[minRow][minCol] = num;
            steps.add(new SolverStep(minRow, minCol, num, "placed"));
            
            // Save state
            Set<Integer>[][] savedCandidates = saveCandidates();
            
            // Forward check: remove num from related cells
            boolean valid = forwardCheck(minRow, minCol, num);
            
            if (valid && backtrack()) {
                return true;
            }
            
            // Restore state
            candidates = savedCandidates;
            steps.add(new SolverStep(minRow, minCol, num, "backtrack"));
            board[minRow][minCol] = EMPTY;
        }
        
        return false;
    }
    
    private boolean forwardCheck(int row, int col, int num) {
        // Remove num from row
        for (int c = 0; c < SIZE; c++) {
            if (c != col) {
                candidates[row][c].remove(num);
                if (board[row][c] == EMPTY && candidates[row][c].isEmpty()) {
                    return false;
                }
            }
        }
        
        // Remove num from column
        for (int r = 0; r < SIZE; r++) {
            if (r != row) {
                candidates[r][col].remove(num);
                if (board[r][col] == EMPTY && candidates[r][col].isEmpty()) {
                    return false;
                }
            }
        }
        
        // Remove num from 3x3 box
        int boxRow = row - row % SUBGRID_SIZE;
        int boxCol = col - col % SUBGRID_SIZE;
        for (int r = boxRow; r < boxRow + SUBGRID_SIZE; r++) {
            for (int c = boxCol; c < boxCol + SUBGRID_SIZE; c++) {
                if (r != row || c != col) {
                    candidates[r][c].remove(num);
                    if (board[r][c] == EMPTY && candidates[r][c].isEmpty()) {
                        return false;
                    }
                }
            }
        }
        
        return true;
    }
    
    @SuppressWarnings("unchecked")
    private Set<Integer>[][] saveCandidates() {
        Set<Integer>[][] saved = new Set[SIZE][SIZE];
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                saved[i][j] = new HashSet<>(candidates[i][j]);
            }
        }
        return saved;
    }
    
    private Set<Integer> getPossibleValues(int row, int col) {
        Set<Integer> possible = new HashSet<>();
        for (int num = 1; num <= 9; num++) {
            possible.add(num);
        }
        
        for (int c = 0; c < SIZE; c++) {
            possible.remove(board[row][c]);
        }
        
        for (int r = 0; r < SIZE; r++) {
            possible.remove(board[r][col]);
        }
        
        int boxRow = row - row % SUBGRID_SIZE;
        int boxCol = col - col % SUBGRID_SIZE;
        for (int r = boxRow; r < boxRow + SUBGRID_SIZE; r++) {
            for (int c = boxCol; c < boxCol + SUBGRID_SIZE; c++) {
                possible.remove(board[r][c]);
            }
        }
        
        return possible;
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
