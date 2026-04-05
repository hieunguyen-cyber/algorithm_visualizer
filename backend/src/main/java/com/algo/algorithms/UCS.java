package com.algo.algorithms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import com.algo.models.Grid;
import com.algo.models.Node;

/**
 * Uniform Cost Search (UCS) implementation.
 * Identical to Dijkstra - explores by actual cost only.
 */
public class UCS {
    private Grid grid;
    private PriorityQueue<Node> openSet;
    private Set<Node> closedSet;
    private Map<Node, Node> cameFrom;
    private int visitCount;
    private List<ExecutionStep> steps;

    public UCS(Grid grid) {
        this.grid = grid;
        this.openSet = new PriorityQueue<>((a, b) -> Double.compare(a.gCost, b.gCost));
        this.closedSet = new HashSet<>();
        this.cameFrom = new HashMap<>();
        this.visitCount = 0;
        this.steps = new ArrayList<>();
    }

    /**
     * Execute UCS algorithm with step-by-step tracking.
     */
    public PathfindingResult execute(Node start, Node goal) {
        PathfindingResult result = new PathfindingResult();
        this.steps.clear();
        this.visitCount = 0;

        // Initialize
        start.gCost = 0;
        start.inOpenSet = true;
        openSet.add(start);

        while (!openSet.isEmpty()) {
            Node current = openSet.poll();
            current.inOpenSet = false;
            current.inClosedSet = true;
            current.visitOrder = visitCount++;
            
            steps.add(new ExecutionStep("visit", current.x, current.y, current.gCost, 0, 0));

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

                double tentativeG = current.gCost + current.distance(neighbor);

                if (!neighbor.inOpenSet) {
                    neighbor.inOpenSet = true;
                    neighbor.gCost = tentativeG;
                    neighbor.parent = current;
                    cameFrom.put(neighbor, current);
                    openSet.add(neighbor);
                    
                    steps.add(new ExecutionStep("open", neighbor.x, neighbor.y, neighbor.gCost, 0, 0));
                } else if (tentativeG < neighbor.gCost) {
                    // Found a better path
                    neighbor.gCost = tentativeG;
                    neighbor.parent = current;
                    cameFrom.put(neighbor, current);
                    // Re-add to priority queue
                    openSet.remove(neighbor);
                    openSet.add(neighbor);
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
