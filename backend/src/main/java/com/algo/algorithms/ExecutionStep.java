package com.algo.algorithms;

/**
 * Single step in algorithm execution for animation.
 */
public class ExecutionStep {
    public String type;          // "visit", "open", "close", "final_path"
    public int nodeX, nodeY;
    public double gCost;
    public double hCost;
    public double fCost;
    public String description;

    public ExecutionStep(String type, int x, int y) {
        this.type = type;
        this.nodeX = x;
        this.nodeY = y;
    }

    public ExecutionStep(String type, int x, int y, double g, double h, double f) {
        this(type, x, y);
        this.gCost = g;
        this.hCost = h;
        this.fCost = f;
    }
}
