package com.algo.models;

/**
 * Represents a node in a grid for pathfinding algorithms.
 */
public class Node implements Comparable<Node> {
    public int x, y;
    public double gCost;      // Cost from start
    public double hCost;      // Heuristic cost to goal (A* only)
    public double fCost;      // g + h (A* only)
    public Node parent;
    public boolean walkable;
    public int visitOrder;    // Order in which node was visited
    public boolean inOpenSet;
    public boolean inClosedSet;
    public boolean inPath;

    public Node(int x, int y) {
        this.x = x;
        this.y = y;
        this.gCost = Double.MAX_VALUE;
        this.hCost = 0;
        this.fCost = Double.MAX_VALUE;
        this.parent = null;
        this.walkable = true;
        this.visitOrder = -1;
        this.inOpenSet = false;
        this.inClosedSet = false;
        this.inPath = false;
    }

    @Override
    public int compareTo(Node other) {
        // For priority queue in A*: lower f cost has higher priority
        return Double.compare(this.fCost, other.fCost);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Node)) return false;
        Node other = (Node) obj;
        return this.x == other.x && this.y == other.y;
    }

    @Override
    public int hashCode() {
        return 31 * x + y;
    }

    public double distance(Node other) {
        int dx = this.x - other.x;
        int dy = this.y - other.y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    public void reset() {
        this.gCost = Double.MAX_VALUE;
        this.hCost = 0;
        this.fCost = Double.MAX_VALUE;
        this.parent = null;
        this.visitOrder = -1;
        this.inOpenSet = false;
        this.inClosedSet = false;
        this.inPath = false;
    }
}
