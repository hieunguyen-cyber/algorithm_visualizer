package com.algo.models;

/**
 * Represents a single step in algorithm execution for visualization.
 */
public class AlgorithmStep {
    public String algorithm;
    public String action;           // "visit", "add_to_open", "add_to_closed", etc.
    public int nodeX, nodeY;
    public double gCost, hCost, fCost;
    public boolean isPath;

    public AlgorithmStep(String algorithm, String action, int x, int y) {
        this.algorithm = algorithm;
        this.action = action;
        this.nodeX = x;
        this.nodeY = y;
    }
}
