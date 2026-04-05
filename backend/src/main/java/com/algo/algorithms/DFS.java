package com.algo.algorithms;

import com.algo.models.Node;
import com.algo.models.Grid;
import java.util.*;

/**
 * Depth-First Search (DFS) pathfinding algorithm.
 * Explores as far as possible along each branch before backtracking.
 */
public class DFS {
    private Grid grid;
    private Stack<Node> stack;
    private Set<Node> visited;
    private Map<Node, Node> parent;
    private int visitCount;
    private List<ExecutionStep> steps;

    public DFS(Grid grid) {
        this.grid = grid;
        this.stack = new Stack<>();
        this.visited = new HashSet<>();
        this.parent = new HashMap<>();
        this.visitCount = 0;
        this.steps = new ArrayList<>();
    }

    /**
     * Execute DFS algorithm.
     */
    public PathfindingResult execute(Node start, Node goal) {
        PathfindingResult result = new PathfindingResult();
        this.steps.clear();
        this.visitCount = 0;

        stack.push(start);
        visited.add(start);
        start.visitOrder = visitCount++;

        while (!stack.isEmpty()) {
            Node current = stack.pop();
            
            steps.add(new ExecutionStep("visit", current.x, current.y));

            if (current.equals(goal)) {
                // Reconstruct path
                result.path = reconstructPath(parent, current);
                markPath(result.path);
                result.success = true;
                result.visitCount = visitCount;
                result.steps = new ArrayList<>(steps);
                return result;
            }

            // Explore neighbors
            List<Node> neighbors = grid.getNeighbors(current, false);
            // Reverse to maintain order when using stack (LIFO)
            for (int i = neighbors.size() - 1; i >= 0; i--) {
                Node neighbor = neighbors.get(i);
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    neighbor.visitOrder = visitCount++;
                    parent.put(neighbor, current);
                    stack.push(neighbor);
                    
                    steps.add(new ExecutionStep("open", neighbor.x, neighbor.y));
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
     * Reconstruct path from goal to start.
     */
    private List<Node> reconstructPath(Map<Node, Node> parent, Node current) {
        List<Node> path = new ArrayList<>();
        path.add(current);
        
        while (parent.containsKey(current)) {
            current = parent.get(current);
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
        stack.clear();
        visited.clear();
        parent.clear();
        visitCount = 0;
        steps.clear();
    }
}
