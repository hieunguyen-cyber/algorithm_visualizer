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
 * Depth-Limited Search (DLS) implementation.
 * Depth-First Search with a maximum depth limit.
 */
public class DLS {
    private Grid grid;
    private Set<Node> visited;
    private Map<Node, Node> cameFrom;
    private int visitCount;
    private List<ExecutionStep> steps;
    private int depthLimit;
    private boolean found;

    public DLS(Grid grid) {
        this.grid = grid;
        this.visited = new HashSet<>();
        this.cameFrom = new HashMap<>();
        this.visitCount = 0;
        this.steps = new ArrayList<>();
        this.depthLimit = 20; // Default depth limit
        this.found = false;
    }

    /**
     * Set the depth limit for the search.
     */
    public void setDepthLimit(int limit) {
        this.depthLimit = limit;
    }

    /**
     * Execute DLS algorithm with step-by-step tracking.
     */
    public PathfindingResult execute(Node start, Node goal) {
        PathfindingResult result = new PathfindingResult();
        this.steps.clear();
        this.visited.clear();
        this.cameFrom.clear();
        this.visitCount = 0;
        this.found = false;

        // Start DLS
        depthLimitedSearch(start, goal, 0);

        if (found) {
            // Rebuild path from cameFrom map
            result.path = reconstructPath(cameFrom, goal);
            markPath(result.path);
            result.success = true;
        } else {
            result.success = false;
        }

        result.visitCount = visitCount;
        result.steps = new ArrayList<>(steps);
        return result;
    }

    /**
     * Recursive DLS function.
     */
    private boolean depthLimitedSearch(Node current, Node goal, int depth) {
        if (found) {
            return true;
        }

        visited.add(current);
        current.visitOrder = visitCount++;
        steps.add(new ExecutionStep("visit", current.x, current.y));

        if (current.equals(goal)) {
            found = true;
            return true;
        }

        if (depth < depthLimit) {
            for (Node neighbor : grid.getNeighbors(current, true)) {
                if (!visited.contains(neighbor)) {
                    neighbor.parent = current;
                    cameFrom.put(neighbor, current);
                    steps.add(new ExecutionStep("open", neighbor.x, neighbor.y));

                    if (depthLimitedSearch(neighbor, goal, depth + 1)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * Reconstruct path from goal to start using cameFrom map.
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
        visited.clear();
        cameFrom.clear();
        visitCount = 0;
        steps.clear();
        found = false;
    }
}
