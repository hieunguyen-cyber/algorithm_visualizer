package com.algo.models;

import java.util.*;

/**
 * Backtracking with MRV heuristic (Minimum Remaining Values).
 * Always selects the cell with fewest possibilities first.
 * Most efficient for visualization and solving.
 */
public class BacktrackingWithMRVSolver implements ISudokuSolver {
    private static final int SIZE = 9;
    private static final int EMPTY = 0;
    private static final int SUBGRID_SIZE = 3;
    
    private int[][] board;
    private List<Integer>[][] candidates;
    private int nodeCount = 0;
    private List<SolverStep> steps = new ArrayList<>();
    
    @SuppressWarnings("unchecked")
    public BacktrackingWithMRVSolver(int[][] board) {
        this.board = copyBoard(board);
        this.candidates = new ArrayList[SIZE][SIZE];
        initializeCandidates();
    }
    
    @SuppressWarnings("unchecked")
    private void initializeCandidates() {
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                if (board[row][col] == EMPTY) {
                    candidates[row][col] = new ArrayList<>(getPossibleValues(row, col));
                } else {
                    candidates[row][col] = new ArrayList<>();
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
        
        // Find cell with minimum remaining values (MRV heuristic)
        int minRow = -1, minCol = -1, minCount = 1000;
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                if (board[row][col] == EMPTY) {
                    int count = candidates[row][col].size();
                    if (count == 0) return false;  // No solution
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
            
            // Save candidates state
            List<Integer>[][] savedCandidates = saveCandidates();
            
            // Update candidates for related cells by removing num
            List<Integer> oldCandidates = new ArrayList<>(candidates[minRow][minCol]);
            candidates[minRow][minCol] = new ArrayList<>();  // No more candidates for this cell
            
            // Remove num from row, column, and box
            boolean valid = true;
            for (int c = 0; c < SIZE; c++) {
                if (c != minCol && board[minRow][c] == EMPTY) {
                    candidates[minRow][c].remove((Integer) num);
                    if (candidates[minRow][c].isEmpty()) {
                        valid = false;
                    }
                }
            }
            
            for (int r = 0; r < SIZE; r++) {
                if (r != minRow && board[r][minCol] == EMPTY) {
                    candidates[r][minCol].remove((Integer) num);
                    if (candidates[r][minCol].isEmpty()) {
                        valid = false;
                    }
                }
            }
            
            int boxRow = minRow - minRow % SUBGRID_SIZE;
            int boxCol = minCol - minCol % SUBGRID_SIZE;
            for (int r = boxRow; r < boxRow + SUBGRID_SIZE; r++) {
                for (int c = boxCol; c < boxCol + SUBGRID_SIZE; c++) {
                    if ((r != minRow || c != minCol) && board[r][c] == EMPTY) {
                        candidates[r][c].remove((Integer) num);
                        if (candidates[r][c].isEmpty()) {
                            valid = false;
                        }
                    }
                }
            }
            
            if (valid && backtrack()) {
                return true;
            }
            
            // Restore candidates
            candidates = savedCandidates;
            steps.add(new SolverStep(minRow, minCol, num, "backtrack"));
            board[minRow][minCol] = EMPTY;
        }
        
        return false;
    }
    
    @SuppressWarnings("unchecked")
    private List<Integer>[][] saveCandidates() {
        List<Integer>[][] saved = new ArrayList[SIZE][SIZE];
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                saved[i][j] = new ArrayList<>(candidates[i][j]);
            }
        }
        return saved;
    }
    
    private List<Integer> getPossibleValues(int row, int col) {
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
        
        return new ArrayList<>(possible);
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
