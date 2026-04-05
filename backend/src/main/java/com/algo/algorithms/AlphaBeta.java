package com.algo.algorithms;

import java.util.ArrayList;
import java.util.List;

import com.algo.models.GameState;
import com.algo.models.GameTreeNode;

/**
 * Alpha-Beta pruning algorithm for game AI.
 * Optimizes minimax by pruning branches that cannot affect the final decision.
 */
public class AlphaBeta {
    private int maxDepth;
    private List<GameTreeNode> treeNodes;
    private int prunedCount;

    public AlphaBeta(int maxDepth) {
        this.maxDepth = maxDepth;
        this.treeNodes = new ArrayList<>();
        this.prunedCount = 0;
    }

    /**
     * Execute alpha-beta pruning algorithm.
     * Returns the best move for the AI player.
     */
    public int[] findBestMove(GameState state) {
        this.treeNodes.clear();
        this.prunedCount = 0;

        int bestScore = Integer.MIN_VALUE;
        int[] bestMove = null;

        List<int[]> availableMoves = state.getAvailableMoves();

        for (int[] move : availableMoves) {
            GameState newState = new GameState(state);
            newState.makeMove(move[0], move[1], GameState.AI);

            int score = alphaBeta(newState, 0, Integer.MIN_VALUE, Integer.MAX_VALUE, false);

            if (score > bestScore) {
                bestScore = score;
                bestMove = move;
            }
        }

        return bestMove;
    }

    /**
     * Alpha-Beta pruning function.
     * @param state Current game state
     * @param depth Current depth
     * @param alpha Best score found for maximizer
     * @param beta Best score found for minimizer
     * @param isMaximizing True if maximizing (AI), false if minimizing (human)
     * @return Evaluation score
     */
    private int alphaBeta(GameState state, int depth, int alpha, int beta, boolean isMaximizing) {
        int winner = state.getWinner();
        
        // Terminal evaluations
        if (winner == GameState.AI) {
            return 10 - depth;
        } else if (winner == GameState.HUMAN) {
            return depth - 10;
        } else if (state.isGameOver()) {
            return 0;
        }

        if (depth >= maxDepth) {
            return evaluatePosition(state);
        }

        List<int[]> availableMoves = state.getAvailableMoves();
        GameTreeNode node = new GameTreeNode(state.getBoard(), -1, -1, 0, isMaximizing, depth);
        node.alpha = alpha;
        node.beta = beta;
        treeNodes.add(node);

        if (isMaximizing) {
            int maxEval = Integer.MIN_VALUE;

            for (int[] move : availableMoves) {
                GameState newState = new GameState(state);
                newState.makeMove(move[0], move[1], GameState.AI);

                int eval = alphaBeta(newState, depth + 1, alpha, beta, false);
                maxEval = Math.max(maxEval, eval);

                GameTreeNode childNode = new GameTreeNode(newState.getBoard(), move[0], move[1], eval, false, depth + 1);
                childNode.alpha = alpha;
                childNode.beta = beta;
                node.children.add(childNode);

                alpha = Math.max(alpha, eval);
                if (beta <= alpha) {
                    childNode.pruned = true;
                    prunedCount++;
                    break;  // Beta cutoff
                }
            }

            node.score = maxEval;
            return maxEval;
        } else {
            int minEval = Integer.MAX_VALUE;

            for (int[] move : availableMoves) {
                GameState newState = new GameState(state);
                newState.makeMove(move[0], move[1], GameState.HUMAN);

                int eval = alphaBeta(newState, depth + 1, alpha, beta, true);
                minEval = Math.min(minEval, eval);

                GameTreeNode childNode = new GameTreeNode(newState.getBoard(), move[0], move[1], eval, true, depth + 1);
                childNode.alpha = alpha;
                childNode.beta = beta;
                node.children.add(childNode);

                beta = Math.min(beta, eval);
                if (beta <= alpha) {
                    childNode.pruned = true;
                    prunedCount++;
                    break;  // Alpha cutoff
                }
            }

            node.score = minEval;
            return minEval;
        }
    }

    /**
     * Position evaluation heuristic.
     */
    private int evaluatePosition(GameState state) {
        int score = 0;
        int[][] board = state.getBoard();

        score += countLines(board, GameState.AI) * 3;
        score -= countLines(board, GameState.HUMAN) * 3;

        return score;
    }

    /**
     * Count potential winning lines.
     */
    private int countLines(int[][] board, int player) {
        int count = 0;
        int boardSize = board.length;

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

    public int getPrunedCount() {
        return prunedCount;
    }
}
