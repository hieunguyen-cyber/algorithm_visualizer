package com.algo.algorithms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.algo.models.Node;

/**
 * Result of a pathfinding algorithm execution.
 */
public class PathfindingResult {
    public List<Node> path;
    public Set<Node> visitedNodes;
    public Set<Node> openSet;
    public Set<Node> closedSet;
    public Map<Node, Double> gCosts;
    public Map<Node, Double> hCosts;
    public Map<Node, Double> fCosts;
    public List<ExecutionStep> steps;
    public boolean success;
    public int visitCount;

    public PathfindingResult() {
        this.path = new ArrayList<>();
        this.visitedNodes = new HashSet<>();
        this.openSet = new HashSet<>();
        this.closedSet = new HashSet<>();
        this.gCosts = new HashMap<>();
        this.hCosts = new HashMap<>();
        this.fCosts = new HashMap<>();
        this.steps = new ArrayList<>();
        this.success = false;
        this.visitCount = 0;
    }
}
