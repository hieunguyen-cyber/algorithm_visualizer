package com.algo.models;

import java.util.*;

/**
 * Represents a 2D grid for pathfinding problems.
 */
public class Grid {
    private int width;
    private int height;
    private Node[][] nodes;
    private Node startNode;
    private Node goalNode;
    private List<AlgorithmStep> steps;
    private int stepCount;

    public Grid(int width, int height) {
        this.width = width;
        this.height = height;
        this.nodes = new Node[height][width];
        this.steps = new ArrayList<>();
        this.stepCount = 0;

        // Initialize all nodes
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                nodes[y][x] = new Node(x, y);
            }
        }
    }

    public Node getNode(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            return null;
        }
        return nodes[y][x];
    }

    public void setWalkable(int x, int y, boolean walkable) {
        Node node = getNode(x, y);
        if (node != null) {
            node.walkable = walkable;
        }
    }

    public List<Node> getNeighbors(Node node, boolean diagonal) {
        List<Node> neighbors = new ArrayList<>();
        
        // Cardinal directions
        int[][] directions = {
            {0, 1}, {1, 0}, {0, -1}, {-1, 0},  // Up, Right, Down, Left
            {1, 1}, {-1, 1}, {1, -1}, {-1, -1} // Diagonal
        };

        int maxDirs = diagonal ? 8 : 4;
        for (int i = 0; i < maxDirs; i++) {
            int newX = node.x + directions[i][0];
            int newY = node.y + directions[i][1];
            Node neighbor = getNode(newX, newY);
            
            if (neighbor != null && neighbor.walkable) {
                neighbors.add(neighbor);
            }
        }
        
        return neighbors;
    }

    public void reset() {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                nodes[y][x].reset();
            }
        }
        steps.clear();
        stepCount = 0;
    }

    public void addStep(AlgorithmStep step) {
        this.steps.add(step);
    }

    // Getters and setters
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public Node getStartNode() { return startNode; }
    public void setStartNode(Node node) { this.startNode = node; }
    public Node getGoalNode() { return goalNode; }
    public void setGoalNode(Node node) { this.goalNode = node; }
    public List<AlgorithmStep> getSteps() { return steps; }
    public Node[][] getNodes() { return nodes; }
}

