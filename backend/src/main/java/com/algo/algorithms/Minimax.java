package com.algo.algorithms;

import com.algo.models.GameState;
import com.algo.models.GameTreeNode;
import java.util.*;

/**
 * Minimax algorithm for game AI (Tic-Tac-Toe).
 * Evaluates game positions by exploring the game tree.
 */
public class Minimax {
    private int maxDepth;
    private List<GameTreeNode> treeNodes;
    private int nodeCount;

    public Minimax(int maxDepth) {
        this.maxDepth = maxDepth;
        this.treeNodes = new ArrayList<>();
        this.nodeCount = 0;
    }

    /**
     * Execute minimax algorithm.
     * Returns the best move for the AI player.
     */
    public int[] findBestMove(GameState state) {
        this.treeNodes.clear();
        this.nodeCount = 0;

        int bestScore = Integer.MIN_VALUE;
        int[] bestMove = null;

        List<int[]> availableMoves = state.getAvailableMoves();

        for (int[] move : availableMoves) {
            GameState newState = new GameState(state);
            newState.makeMove(move[0], move[1], GameState.AI);

            int score = minimax(newState, 0, false);

            if (score > bestScore) {
                bestScore = score;
                bestMove = move;
            }
        }

        return bestMove;
    }

    /**
     * Recursive minimax function.
     * @param state Current game state
     * @param depth Current depth in game tree
     * @param isMaximizing True if maximizing (AI), false if minimizing (human)
     * @return Evaluation score
     */
    private int minimax(GameState state, int depth, boolean isMaximizing) {
        int winner = state.getWinner();
        
        // Terminal node evaluations
        if (winner == GameState.AI) {
            return 10 - depth;  // Prefer wins at shallower depths
        } else if (winner == GameState.HUMAN) {
            return depth - 10;  // Prefer losses at deeper depths
        } else if (state.isGameOver()) {
            return 0;  // Draw
        }

        if (depth >= maxDepth) {
            return evaluatePosition(state);
        }

        List<int[]> availableMoves = state.getAvailableMoves();
        GameTreeNode node = new GameTreeNode(state.getBoard(), -1, -1, 0, isMaximizing, depth);
        treeNodes.add(node);

        if (isMaximizing) {
            int maxEval = Integer.MIN_VALUE;

            for (int[] move : availableMoves) {
                GameState newState = new GameState(state);
                newState.makeMove(move[0], move[1], GameState.AI);

                int eval = minimax(newState, depth + 1, false);
                maxEval = Math.max(maxEval, eval);

                GameTreeNode childNode = new GameTreeNode(newState.getBoard(), move[0], move[1], eval, false, depth + 1);
                node.children.add(childNode);
            }
            node.score = maxEval;
            return maxEval;
        } else {
            int minEval = Integer.MAX_VALUE;

            for (int[] move : availableMoves) {
                GameState newState = new GameState(state);
                newState.makeMove(move[0], move[1], GameState.HUMAN);

                int eval = minimax(newState, depth + 1, true);
                minEval = Math.min(minEval, eval);

                GameTreeNode childNode = new GameTreeNode(newState.getBoard(), move[0], move[1], eval, true, depth + 1);
                node.children.add(childNode);
            }
            node.score = minEval;
            return minEval;
        }
    }

    /**
     * Simple position evaluation heuristic.
     */
    private int evaluatePosition(GameState state) {
        int score = 0;
        int[][] board = state.getBoard();

        // Count lines (potential winning lines)
        score += countLines(board, GameState.AI) * 3;
        score -= countLines(board, GameState.HUMAN) * 3;

        return score;
    }

    /**
     * Count potential winning lines for a player.
     */
    private int countLines(int[][] board, int player) {
        int count = 0;
        int boardSize = board.length;

        // Check rows
        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++) {
                if (board[i][j] == player) count++;
            }
        }

        return count;
    }

    public List<GameTreeNode> getTreeNodes() {
        return treeNodes;
    }

    public int getNodeCount() {
        return treeNodes.size();
    }
}
