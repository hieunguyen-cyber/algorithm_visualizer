package com.algo.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a game state for Minimax and Alpha-Beta algorithms.
 */
public class GameState {
    private int[][] board;           // 0 = empty, 1 = human, 2 = AI
    private int boardSize;
    private int winCondition;
    private boolean isXNext;
    private int moveCount;
    public List<GameTreeNode> treeNodes;
    
    public static final int EMPTY = 0;
    public static final int HUMAN = 1;
    public static final int AI = 2;

    public GameState(int boardSize, int winCondition) {
        this.boardSize = boardSize;
        this.board = new int[boardSize][boardSize];
        this.winCondition = winCondition;
        this.isXNext = true;
        this.moveCount = 0;
        this.treeNodes = new ArrayList<>();
    }

    public GameState(GameState other) {
        this.boardSize = other.boardSize;
        this.board = new int[boardSize][boardSize];
        this.winCondition = other.winCondition;
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
            for (int j = 0; j <= boardSize - winCondition; j++) {
                int first = board[i][j];
                if (first == EMPTY) continue;
                boolean win = true;
                for (int k = 1; k < winCondition; k++) {
                    if (board[i][j + k] != first) {
                        win = false;
                        break;
                    }
                }
                if (win) return first;
            }
        }

        // Check columns
        for (int j = 0; j < boardSize; j++) {
            for (int i = 0; i <= boardSize - winCondition; i++) {
                int first = board[i][j];
                if (first == EMPTY) continue;
                boolean win = true;
                for (int k = 1; k < winCondition; k++) {
                    if (board[i + k][j] != first) {
                        win = false;
                        break;
                    }
                }
                if (win) return first;
            }
        }

        // Check diagonals (top-left to bottom-right)
        for (int i = 0; i <= boardSize - winCondition; i++) {
            for (int j = 0; j <= boardSize - winCondition; j++) {
                int first = board[i][j];
                if (first == EMPTY) continue;
                boolean win = true;
                for (int k = 1; k < winCondition; k++) {
                    if (board[i + k][j + k] != first) {
                        win = false;
                        break;
                    }
                }
                if (win) return first;
            }
        }

        // Check diagonals (top-right to bottom-left)
        for (int i = 0; i <= boardSize - winCondition; i++) {
            for (int j = winCondition - 1; j < boardSize; j++) {
                int first = board[i][j];
                if (first == EMPTY) continue;
                boolean win = true;
                for (int k = 1; k < winCondition; k++) {
                    if (board[i + k][j - k] != first) {
                        win = false;
                        break;
                    }
                }
                if (win) return first;
            }
        }

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
    public int getWinCondition() { return winCondition; }
}
