package com.algo.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a node in the minimax game tree.
 */
public class GameTreeNode {
    public int[][] boardState;
    public int row, col;
    public int score;
    public boolean isMax;
    public int depth;
    public int alpha, beta;
    public boolean pruned;
    public List<GameTreeNode> children;

    public GameTreeNode(int[][] board, int row, int col, int score, boolean isMax, int depth) {
        this.boardState = deepCopy(board);
        this.row = row;
        this.col = col;
        this.score = score;
        this.isMax = isMax;
        this.depth = depth;
        this.alpha = Integer.MIN_VALUE;
        this.beta = Integer.MAX_VALUE;
        this.pruned = false;
        this.children = new ArrayList<>();
    }

    private int[][] deepCopy(int[][] board) {
        int[][] copy = new int[board.length][];
        for (int i = 0; i < board.length; i++) {
            copy[i] = board[i].clone();
        }
        return copy;
    }
}
