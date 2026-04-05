package com.algo.algorithms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import com.algo.models.Grid;
import com.algo.models.Node;

/**
 * Breadth-First Search (BFS) pathfinding algorithm.
 * Explores all nodes at depth k before exploring nodes at k+1.
 */
public class BFS {
    private Grid grid;
    private Queue<Node> queue;
    private Set<Node> visited;
    private Map<Node, Node> parent;
    private int visitCount;
    private List<ExecutionStep> steps;

    public BFS(Grid grid) {
        this.grid = grid;
        this.queue = new LinkedList<>();
        this.visited = new HashSet<>();
        this.parent = new HashMap<>();
        this.visitCount = 0;
        this.steps = new ArrayList<>();
    }

    /**
     * Execute BFS algorithm.
     */
    public PathfindingResult execute(Node start, Node goal) {
        PathfindingResult result = new PathfindingResult();
        this.steps.clear();
        this.visitCount = 0;

        queue.add(start);
        visited.add(start);
        start.visitOrder = visitCount++;

        while (!queue.isEmpty()) {
            Node current = queue.poll();
            
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
            for (Node neighbor : grid.getNeighbors(current, false)) {
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    neighbor.visitOrder = visitCount++;
                    parent.put(neighbor, current);
                    queue.add(neighbor);
                    
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
        queue.clear();
        visited.clear();
        parent.clear();
        visitCount = 0;
        steps.clear();
    }
}
