package com.algo.algorithms;

import com.algo.models.Node;
import com.algo.models.Grid;
import java.util.*;

/**
 * Greedy Best-First Search implementation.
 * Uses only heuristic (h), not actual cost (g).
 */
public class GreedyBestFirst {
    private Grid grid;
    private PriorityQueue<Node> openSet;
    private Set<Node> closedSet;
    private Map<Node, Node> cameFrom;
    private int visitCount;
    private List<ExecutionStep> steps;

    public GreedyBestFirst(Grid grid) {
        this.grid = grid;
        this.openSet = new PriorityQueue<>((a, b) -> Double.compare(a.hCost, b.hCost));
        this.closedSet = new HashSet<>();
        this.cameFrom = new HashMap<>();
        this.visitCount = 0;
        this.steps = new ArrayList<>();
    }

    /**
     * Execute Greedy Best-First Search algorithm with step-by-step tracking.
     */
    public PathfindingResult execute(Node start, Node goal) {
        PathfindingResult result = new PathfindingResult();
        this.steps.clear();
        this.visitCount = 0;

        // Initialize
        start.hCost = heuristic(start, goal);
        start.inOpenSet = true;
        openSet.add(start);

        while (!openSet.isEmpty()) {
            Node current = openSet.poll();
            current.inOpenSet = false;
            current.inClosedSet = true;
            current.visitOrder = visitCount++;
            
            steps.add(new ExecutionStep("visit", current.x, current.y, 0, current.hCost, 0));

            if (current.equals(goal)) {
                // Reconstruct path
                result.path = reconstructPath(cameFrom, current);
                markPath(result.path);
                result.success = true;
                result.visitCount = visitCount;
                result.steps = new ArrayList<>(steps);
                return result;
            }

            // Check neighbors
            for (Node neighbor : grid.getNeighbors(current, true)) {
                if (neighbor.inClosedSet) {
                    continue;
                }

                if (!neighbor.inOpenSet) {
                    neighbor.inOpenSet = true;
                    neighbor.hCost = heuristic(neighbor, goal);
                    neighbor.parent = current;
                    cameFrom.put(neighbor, current);
                    openSet.add(neighbor);
                    
                    steps.add(new ExecutionStep("open", neighbor.x, neighbor.y, 0, neighbor.hCost, 0));
                }
            }
        }

        // No path found
        result.success = false;
        result.visitCount = visitCount;
        result.steps = new ArrayList<>(steps);
        return result;
    }

    /**
     * Manhattan distance heuristic.
     */
    private double heuristic(Node a, Node b) {
        return Math.abs(a.x - b.x) + Math.abs(a.y - b.y);
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
        openSet.clear();
        closedSet.clear();
        cameFrom.clear();
        visitCount = 0;
        steps.clear();
    }
}
