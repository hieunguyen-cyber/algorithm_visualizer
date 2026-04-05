package com.algo.models;

import java.util.*;

/**
 * Represents a game state for Minimax and Alpha-Beta algorithms.
 */
public class GameState {
    private int[][] board;           // 0 = empty, 1 = human, 2 = AI
    private int boardSize;
    private boolean isXNext;
    private int moveCount;
    public List<GameTreeNode> treeNodes;
    
    public static final int EMPTY = 0;
    public static final int HUMAN = 1;
    public static final int AI = 2;

    public GameState(int boardSize) {
        this.boardSize = boardSize;
        this.board = new int[boardSize][boardSize];
        this.isXNext = true;
        this.moveCount = 0;
        this.treeNodes = new ArrayList<>();
    }

    public GameState(GameState other) {
        this.boardSize = other.boardSize;
        this.board = new int[boardSize][boardSize];
        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++) {
                this.board[i][j] = other.board[i][j];
            }
        }
        this.isXNext = other.isXNext;
        this.moveCount = other.moveCount;
        this.treeNodes = new ArrayList<>();
    }

    public int getValue(int row, int col) {
        if (row < 0 || row >= boardSize || col < 0 || col >= boardSize) {
            return -1; // Invalid
        }
        return board[row][col];
    }

    public void setValue(int row, int col, int value) {
        if (row >= 0 && row < boardSize && col >= 0 && col < boardSize) {
            board[row][col] = value;
            moveCount++;
            isXNext = !isXNext;
        }
    }

    public void makeMove(int row, int col, int player) {
        if (isValidMove(row, col)) {
            board[row][col] = player;
            moveCount++;
            isXNext = (player == HUMAN); // Alternate players
        }
    }

    public boolean isValidMove(int row, int col) {
        if (row < 0 || row >= boardSize || col < 0 || col >= boardSize) {
            return false;
        }
        return board[row][col] == EMPTY;
    }

    public List<int[]> getAvailableMoves() {
        List<int[]> moves = new ArrayList<>();
        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++) {
                if (board[i][j] == EMPTY) {
                    moves.add(new int[]{i, j});
                }
            }
        }
        return moves;
    }

    public int getWinner() {
        // Check rows
        for (int i = 0; i < boardSize; i++) {
            boolean allX = true, allO = true;
            for (int j = 0; j < boardSize; j++) {
                if (board[i][j] != HUMAN) allX = false;
                if (board[i][j] != AI) allO = false;
            }
            if (allX) return HUMAN;
            if (allO) return AI;
        }

        // Check columns
        for (int j = 0; j < boardSize; j++) {
            boolean allX = true, allO = true;
            for (int i = 0; i < boardSize; i++) {
                if (board[i][j] != HUMAN) allX = false;
                if (board[i][j] != AI) allO = false;
            }
            if (allX) return HUMAN;
            if (allO) return AI;
        }

        // Check diagonals
        boolean allX = true, allO = true;
        for (int i = 0; i < boardSize; i++) {
            if (board[i][i] != HUMAN) allX = false;
            if (board[i][i] != AI) allO = false;
        }
        if (allX) return HUMAN;
        if (allO) return AI;

        allX = true;
        allO = true;
        for (int i = 0; i < boardSize; i++) {
            if (board[i][boardSize - 1 - i] != HUMAN) allX = false;
            if (board[i][boardSize - 1 - i] != AI) allO = false;
        }
        if (allX) return HUMAN;
        if (allO) return AI;

        return EMPTY; // No winner
    }

    public boolean isGameOver() {
        return getWinner() != EMPTY || moveCount == boardSize * boardSize;
    }

    public int[][] getBoard() {
        return board;
    }

    public void reset() {
        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++) {
                board[i][j] = EMPTY;
            }
        }
        moveCount = 0;
        isXNext = true;
        treeNodes.clear();
    }

    public boolean isXNext() { return isXNext; }
    public int getMoveCount() { return moveCount; }
    public int getBoardSize() { return boardSize; }
}
