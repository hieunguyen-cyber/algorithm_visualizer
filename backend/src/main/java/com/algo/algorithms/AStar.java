package com.algo.algorithms;

import com.algo.models.Node;
import com.algo.models.Grid;
import java.util.*;

/**
 * A* pathfinding algorithm implementation.
 * Combines actual cost (g) and heuristic cost (h) to guide search.
 */
public class AStar {
    private Grid grid;
    private PriorityQueue<Node> openSet;
    private Set<Node> closedSet;
    private Map<Node, Node> cameFrom;
    private int visitCount;
    private List<ExecutionStep> steps;

    public AStar(Grid grid) {
        this.grid = grid;
        this.openSet = new PriorityQueue<>();
        this.closedSet = new HashSet<>();
        this.cameFrom = new HashMap<>();
        this.visitCount = 0;
        this.steps = new ArrayList<>();
    }

    /**
     * Execute A* algorithm with step-by-step tracking.
     */
    public PathfindingResult execute(Node start, Node goal) {
        PathfindingResult result = new PathfindingResult();
        this.steps.clear();
        this.visitCount = 0;

        // Initialize
        start.gCost = 0;
        start.hCost = heuristic(start, goal);
        start.fCost = start.hCost;
        start.inOpenSet = true;
        openSet.add(start);

        int stepId = 0;

        while (!openSet.isEmpty()) {
            Node current = openSet.poll();
            current.inOpenSet = false;
            current.inClosedSet = true;
            current.visitOrder = visitCount++;
            
            steps.add(new ExecutionStep("visit", current.x, current.y, current.gCost, current.hCost, current.fCost));

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
                    neighbor.hCost = heuristic(neighbor, goal);
                    neighbor.fCost = neighbor.gCost + neighbor.hCost;
                    neighbor.parent = current;
                    cameFrom.put(neighbor, current);
                    openSet.add(neighbor);
                    
                    steps.add(new ExecutionStep("open", neighbor.x, neighbor.y, 
                        neighbor.gCost, neighbor.hCost, neighbor.fCost));
                } else if (tentativeG < neighbor.gCost) {
                    // Found a better path
                    neighbor.gCost = tentativeG;
                    neighbor.fCost = neighbor.gCost + neighbor.hCost;
                    neighbor.parent = current;
                    cameFrom.put(neighbor, current);
                    // Re-add to priority queue to update priority
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
