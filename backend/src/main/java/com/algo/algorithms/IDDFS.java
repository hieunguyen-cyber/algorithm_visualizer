package com.algo.algorithms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.algo.models.Grid;
import com.algo.models.Node;

/**
 * Iterative Deepening Depth-First Search (IDDFS) implementation.
 * Repeatedly performs DLS with increasing depth limits.
 */
public class IDDFS {
    private Grid grid;
    private Map<Node, Node> cameFrom;
    private int visitCount;
    private List<ExecutionStep> steps;
    private int maxDepth;
    private Set<Node> visited;

    public IDDFS(Grid grid) {
        this.grid = grid;
        this.cameFrom = new HashMap<>();
        this.visitCount = 0;
        this.steps = new ArrayList<>();
        this.maxDepth = 30; // Maximum depth to try
        this.visited = new HashSet<>();
    }

    /**
     * Execute IDDFS algorithm with step-by-step tracking.
     */
    public PathfindingResult execute(Node start, Node goal) {
        PathfindingResult result = new PathfindingResult();
        this.steps.clear();
        this.cameFrom.clear();
        this.visitCount = 0;
        this.visited.clear();

        // Try with increasing depth limits
        for (int depth = 0; depth <= maxDepth; depth++) {
            this.visited.clear();
            this.cameFrom.clear();

            if (depthLimitedSearch(start, goal, 0, depth)) {
                // Found solution
                result.path = reconstructPath(cameFrom, goal);
                markPath(result.path);
                result.success = true;
                result.visitCount = visitCount;
                result.steps = new ArrayList<>(steps);
                return result;
            }
        }

        // No path found
        result.success = false;
        result.visitCount = visitCount;
        result.steps = new ArrayList<>(steps);
        return result;
    }

    /**
     * Recursive DLS function with depth limit.
     */
    private boolean depthLimitedSearch(Node current, Node goal, int depth, int depthLimit) {
        visited.add(current);
        current.visitOrder = visitCount++;
        steps.add(new ExecutionStep("visit", current.x, current.y));

        if (current.equals(goal)) {
            return true;
        }

        if (depth < depthLimit) {
            for (Node neighbor : grid.getNeighbors(current, true)) {
                if (!visited.contains(neighbor)) {
                    neighbor.parent = current;
                    cameFrom.put(neighbor, current);
                    steps.add(new ExecutionStep("open", neighbor.x, neighbor.y));

                    if (depthLimitedSearch(neighbor, goal, depth + 1, depthLimit)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * Reconstruct path from goal to start.
     */
    private List<Node> reconstructPath(Map<Node, Node> cameFrom, Node current) {
        List<Node> path = new ArrayList<>();
        path.add(current);
        
        while (cameFrom.containsKey(current)) {
            current = cameFrom.get(current);
            path.add(0, current);
        }
        
        return path;
    }

    /**
     * Mark nodes in final path.
     */
    private void markPath(List<Node> path) {
        for (Node node : path) {
            node.inPath = true;
        }
    }

    public List<ExecutionStep> getSteps() {
        return steps;
    }

    public void reset() {
        cameFrom.clear();
        visitCount = 0;
        steps.clear();
        visited.clear();
    }
}
