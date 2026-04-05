package com.algo.controllers;

import com.algo.models.*;
import com.algo.algorithms.*;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;

/**
 * Controller for pathfinding algorithm requests.
 */
public class AlgorithmController {
    private Gson gson;

    public AlgorithmController() {
        this.gson = new Gson();
    }

    /**
     * Handle pathfinding request
     * Request body: {
     *   "algorithm": "astar|bfs|dfs",
     *   "gridWidth": 20,
     *   "gridHeight": 20,
     *   "startX": 0, "startY": 0,
     *   "goalX": 19, "goalY": 19,
     *   "obstacles": [[1,1], [2,2], ...]
     * }
     */
    public String pathfind(String requestBody) {
        try {
            JsonObject request = gson.fromJson(requestBody, JsonObject.class);
            
            String algorithm = request.get("algorithm").getAsString();
            int gridWidth = request.get("gridWidth").getAsInt();
            int gridHeight = request.get("gridHeight").getAsInt();
            int startX = request.get("startX").getAsInt();
            int startY = request.get("startY").getAsInt();
            int goalX = request.get("goalX").getAsInt();
            int goalY = request.get("goalY").getAsInt();

            // Create grid
            Grid grid = new Grid(gridWidth, gridHeight);
            
            // Set obstacles
            if (request.has("obstacles")) {
                JsonArray obstaclesArray = request.getAsJsonArray("obstacles");
                for (int i = 0; i < obstaclesArray.size(); i++) {
                    JsonArray coord = obstaclesArray.get(i).getAsJsonArray();
                    grid.setWalkable(coord.get(0).getAsInt(), coord.get(1).getAsInt(), false);
                }
            }

            Node start = grid.getNode(startX, startY);
            Node goal = grid.getNode(goalX, goalY);

            if (start == null || goal == null || !start.walkable || !goal.walkable) {
                return gson.toJson(createErrorResponse("Invalid start or goal position"));
            }

            // Reset all nodes before execution
            for (int y = 0; y < grid.getHeight(); y++) {
                for (int x = 0; x < grid.getWidth(); x++) {
                    grid.getNode(x, y).reset();
                }
            }

            // Execute algorithm
            PathfindingResult result = null;
            String algorithmName = "";

            if ("astar".equalsIgnoreCase(algorithm)) {
                algorithmName = "A*";
                AStar aStar = new AStar(grid);
                result = aStar.execute(start, goal);
            } else if ("astar-dijkstra".equalsIgnoreCase(algorithm)) {
                algorithmName = "A* (No Pruning)";
                AStarNoPruning astarNoPruning = new AStarNoPruning(grid);
                result = astarNoPruning.execute(start, goal);
            } else if ("bfs".equalsIgnoreCase(algorithm)) {
                algorithmName = "BFS";
                BFS bfs = new BFS(grid);
                result = bfs.execute(start, goal);
            } else if ("dfs".equalsIgnoreCase(algorithm)) {
                algorithmName = "DFS";
                DFS dfs = new DFS(grid);
                result = dfs.execute(start, goal);
            } else if ("dijkstra".equalsIgnoreCase(algorithm)) {
                algorithmName = "Dijkstra";
                Dijkstra dijkstra = new Dijkstra(grid);
                result = dijkstra.execute(start, goal);
            } else if ("ucs".equalsIgnoreCase(algorithm)) {
                algorithmName = "UCS";
                UCS ucs = new UCS(grid);
                result = ucs.execute(start, goal);
            } else if ("greedy".equalsIgnoreCase(algorithm)) {
                algorithmName = "Greedy Best-First";
                GreedyBestFirst greedy = new GreedyBestFirst(grid);
                result = greedy.execute(start, goal);
            } else if ("dls".equalsIgnoreCase(algorithm)) {
                algorithmName = "DLS";
                DLS dls = new DLS(grid);
                dls.setDepthLimit(25);
                result = dls.execute(start, goal);
            } else if ("iddfs".equalsIgnoreCase(algorithm)) {
                algorithmName = "IDDFS";
                IDDFS iddfs = new IDDFS(grid);
                result = iddfs.execute(start, goal);
            } else {
                return gson.toJson(createErrorResponse("Unknown algorithm: " + algorithm));
            }

            // Build response
            return buildPathfindingResponse(algorithmName, result, grid);

        } catch (Exception e) {
            return gson.toJson(createErrorResponse("Error: " + e.getMessage()));
        }
    }

    /**
     * Build pathfinding response JSON.
     */
    private String buildPathfindingResponse(String algorithm, PathfindingResult result, Grid grid) {
        JsonObject response = new JsonObject();
        response.addProperty("algorithm", algorithm);
        response.addProperty("success", result.success);
        response.addProperty("hasSolution", result.success);
        response.addProperty("visitedCount", result.visitCount);

        // Add path
        JsonArray pathArray = new JsonArray();
        for (Node node : result.path) {
            JsonObject pathNode = new JsonObject();
            pathNode.addProperty("x", node.x);
            pathNode.addProperty("y", node.y);
            pathArray.add(pathNode);
        }
        response.add("path", pathArray);

        // Add grid state
        JsonArray gridState = new JsonArray();
        for (int y = 0; y < grid.getHeight(); y++) {
            for (int x = 0; x < grid.getWidth(); x++) {
                Node node = grid.getNode(x, y);
                if (!node.walkable || node.inClosedSet || node.inOpenSet || node.inPath) {
                    JsonObject nodeState = new JsonObject();
                    nodeState.addProperty("x", x);
                    nodeState.addProperty("y", y);
                    nodeState.addProperty("status", getNodeStatus(node));
                    if (node.visitOrder >= 0) {
                        nodeState.addProperty("visitOrder", node.visitOrder);
                    }
                    if (!Double.isInfinite(node.gCost)) {
                        nodeState.addProperty("g", node.gCost);
                        nodeState.addProperty("h", node.hCost);
                        nodeState.addProperty("f", node.fCost);
                    }
                    gridState.add(nodeState);
                }
            }
        }
        response.add("gridState", gridState);

        // Add steps
        JsonArray stepsArray = new JsonArray();
        for (ExecutionStep step : result.steps) {
            JsonObject stepObj = new JsonObject();
            stepObj.addProperty("type", step.type);
            stepObj.addProperty("x", step.nodeX);
            stepObj.addProperty("y", step.nodeY);
            if (step.gCost > 0) {
                stepObj.addProperty("g", step.gCost);
                stepObj.addProperty("h", step.hCost);
                stepObj.addProperty("f", step.fCost);
            }
            stepsArray.add(stepObj);
        }
        response.add("steps", stepsArray);

        return gson.toJson(response);
    }

    /**
     * Get status string for a node.
     */
    private String getNodeStatus(Node node) {
        if (!node.walkable) return "wall";
        if (node.inPath) return "path";
        if (node.inClosedSet) return "closed";
        if (node.inOpenSet) return "open";
        return "unvisited";
    }

    /**
     * Create error response.
     */
    private JsonObject createErrorResponse(String message) {
        JsonObject error = new JsonObject();
        error.addProperty("error", message);
        return error;
    }
}
