# Algorithm Visualizer 🎓

A comprehensive educational application that visualizes classic search and game AI algorithms with beautiful animations and interactive controls.

## Features ✨

### Pathfinding Algorithms
- **A* Search**: Combines actual cost (g) and heuristic cost (h) for optimal pathfinding
- **Breadth-First Search (BFS)**: Explores nodes level-by-level
- **Depth-First Search (DFS)**: Explores deep paths before backtracking

**Visualization:**
- Real-time node exploration with color coding
- Step-by-step animation with controllable speed
- Display of g(n), h(n), f(n) costs for A*
- Final shortest path highlight
- Customizable grid size and obstacles

### Game AI Algorithms
- **Minimax**: Complete game tree evaluation for optimal moves
- **Alpha-Beta Pruning**: Optimized minimax with branch pruning

**Visualization:**
- Interactive Tic-Tac-Toe gameplay
- Human vs AI or AI vs AI modes
- Real-time algorithm statistics
- Game tree node count and pruned node count display

## Technical Stack

- **Backend**: Java (Pure Java, no Spring Boot)
- **Frontend**: HTML5 + CSS3 + Vanilla JavaScript
- **API**: REST with JSON communication
- **Build**: Maven
- **Server**: Java HTTP Server (com.sun.net.httpserver)

## Architecture

```
project/
├── backend/
│   ├── pom.xml
│   └── src/main/java/com/algo/
│       ├── models/           # Node, Grid, GameState
│       ├── algorithms/       # A*, BFS, DFS, Minimax, AlphaBeta
│       ├── controllers/      # API endpoints
│       └── server/           # HTTP server
├── frontend/
│   ├── index.html           # Main HTML
│   ├── styles.css           # Beautiful styling
│   ├── config.js            # Configuration & API client
│   ├── pathfinding.js       # Pathfinding visualization
│   ├── game.js              # Game visualization
│   └── app.js               # Main controller
└── README.md
```

## Installation & Setup

### Prerequisites
- Java 11 or higher
- Maven 3.6 or higher
- Modern web browser (Chrome, Firefox, Safari, Edge)

### Backend Setup

1. **Navigate to backend directory:**
   ```bash
   cd backend
   ```

2. **Build the project:**
   ```bash
   mvn clean package -DskipTests
   ```

3. **Run the server:**
   ```bash
   java -jar target/algorithm-visualizer-1.0.0-jar-with-dependencies.jar
   ```

   Or directly with Maven:
   ```bash
   mvn exec:java -Dexec.mainClass="com.algo.server.AlgorithmServer"
   ```

The server will start on `http://localhost:8080`

### Frontend Setup

1. **Navigate to frontend directory:**
   ```bash
   cd frontend
   ```

2. **Start a simple HTTP server:**

   **Using Python 3:**
   ```bash
   python3 -m http.server 8000
   ```

   **Using Python 2:**
   ```bash
   python -m SimpleHTTPServer 8000
   ```

   **Using Node.js with http-server:**
   ```bash
   npx http-server -p 8000
   ```

   **Using Ruby:**
   ```bash
   ruby -run -ehttpd . -p8000
   ```

3. **Open in browser:**
   Navigate to `http://localhost:8000`

## Usage

### Pathfinding Demo

1. Select an algorithm (A*, BFS, or DFS)
2. Adjust grid size (10-30)
3. Set animation speed
4. Click **Start** to run the algorithm
5. Use **Pause/Resume** to control animation
6. Click **Step** for step-by-step execution
7. Click **Reset** to clear the board

**Controls:**
- **Speed Range**: Slow → Fast (10ms to 500ms delay)
- **Grid Size**: Small 10×10 to Large 30×30
- **Pause**: Pause and resume animation
- **Step**: Execute one step at a time

### Game AI Demo

1. Select AI algorithm (Minimax or Alpha-Beta Pruning)
2. Choose game mode (Human vs AI or AI vs AI)
3. Click **New Game**
4. For Human vs AI: Click on grid cells to make moves
5. For AI vs AI: Click **AI Move** to advance simulation
6. View algorithm statistics (tree nodes, pruned nodes)

## Algorithm Details

### A* Search
- **Time Complexity**: O(b^d) where b is branching factor, d is depth
- **Space Complexity**: O(b^d)
- **Optimality**: Guaranteed with admissible heuristic
- **Heuristic Used**: Manhattan distance

**Key Metrics Displayed:**
- g(n): Cost from start to node n
- h(n): Estimated cost from n to goal
- f(n): Total estimated cost (g(n) + h(n))

### BFS
- **Time Complexity**: O(V + E)
- **Space Complexity**: O(V)
- **Optimality**: Optimal for unweighted graphs
- **Use Case**: Finding shortest path in unweighted grids

### DFS
- **Time Complexity**: O(V + E)
- **Space Complexity**: O(h) where h is height
- **Optimality**: Not guaranteed
- **Use Case**: Finding any path (not necessarily shortest)

### Minimax
- **Time Complexity**: O(b^d) where b is branching factor, d is depth
- **Space Complexity**: O(b·d)
- **Optimality**: Guaranteed
- **Best For**: Perfect information games

**Game Tree Statistics:**
- Tree Nodes: Total nodes evaluated
- This shows complete exploration without pruning

### Alpha-Beta Pruning
- **Time Complexity**: O(b^(d/2)) best case, O(b^d) worst case
- **Optimality**: Same as Minimax
- **Improvement**: 50-97% faster than Minimax (depending on move order)

**Game Tree Statistics:**
- Tree Nodes: Nodes actually evaluated
- Pruned Nodes: Branches cut off early
- Shows the efficiency improvement

## File Descriptions

### Backend Files

- **Node.java**: Represents a grid cell with pathfinding state
- **Grid.java**: 2D grid management
- **GameState.java**: Tic-Tac-Toe game state management
- **AStar.java**: A* algorithm implementation
- **BFS.java**: Breadth-first search implementation
- **DFS.java**: Depth-first search implementation
- **Minimax.java**: Minimax algorithm for game AI
- **AlphaBeta.java**: Alpha-Beta pruning algorithm
- **AlgorithmController.java**: REST API for pathfinding
- **GameController.java**: REST API for game AI
- **AlgorithmServer.java**: HTTP server entry point

### Frontend Files

- **index.html**: Main HTML structure with tabs and canvas
- **styles.css**: Beautiful modern styling with animations
- **config.js**: Constants, configuration, and API client
- **pathfinding.js**: Grid visualization and animation
- **game.js**: Tic-Tac-Toe board and game logic
- **app.js**: Main application controller and event handlers

## API Endpoints

### Pathfinding
- **POST** `/api/pathfinding`
  
  Request:
  ```json
  {
    "algorithm": "astar|bfs|dfs",
    "gridWidth": 20,
    "gridHeight": 20,
    "startX": 0,
    "startY": 0,
    "goalX": 19,
    "goalY": 19,
    "obstacles": [[1,1], [2,2], ...]
  }
  ```
  
  Response includes path, grid state, and execution steps for animation

### Game AI
- **POST** `/api/game/init`: Initialize new game
- **POST** `/api/game/move`: Make a player move
- **POST** `/api/game/ai-move`: Get AI move (Minimax or Alpha-Beta)
- **GET** `/api/game/state`: Get current game state
- **POST** `/api/game/reset`: Reset game board

## Performance Tips

1. **For Large Grids**: Use BFS/DFS instead of A* for faster computation
2. **For Game AI**: Alpha-Beta pruning is significantly faster than Minimax
3. **Animation Performance**: Reduce animation speed for large datasets
4. **Browser Performance**: Use Chrome or Firefox for best performance

## Customization

### Modifying Maze/Obstacles
Edit `frontend/config.js`:
```javascript
DEFAULT_OBSTACLES: [
    // Add your own obstacle coordinates
]
```

### Changing Colors
Edit `frontend/config.js` under `COLORS`:
```javascript
COLORS: {
    START: '#10b981',    // Change colors here
    GOAL: '#ff6b6b',
    // ...
}
```

### Algorithm Parameters
Edit respective algorithm files in `backend/src/main/java/com/algo/algorithms/`

### Grid Size Limits
Edit `frontend/config.js`:
```javascript
MIN_GRID_SIZE: 10,
MAX_GRID_SIZE: 30
```

## Troubleshooting

### Port Already in Use
- Backend: Change port in `AlgorithmServer.java` (default: 8080)
- Frontend: Use different port with http-server: `-p 7000`

### CORS Errors
- Backend already includes CORS headers
- Make sure frontend is on different port

### Canvas Not Rendering
- Ensure browser supports HTML5 Canvas
- Check browser console for errors

### Algorithm Not Running
- Check browser console for JavaScript errors
- Verify backend is running with `/health` endpoint
- Check network tab for API calls

## Learning Resources

Articles about the algorithms implemented:
- A* Search
- Breadth-First Search
- Depth-First Search
- Minimax Algorithm
- Alpha-Beta Pruning

## Future Enhancements

- [ ] Dijkstra's algorithm visualization
- [ ] More complex game AI (Chess)
- [ ] Graph visualization for game tree
- [ ] Multiple pathfinding on same grid
- [ ] Custom obstacle drawing tool
- [ ] Weighted edges support
- [ ] Statistics export (CSV/JSON)
- [ ] Crossword solver
- [ ] Dark/light theme toggle
- [ ] Sound effects

## Building from Scratch

To rebuild the entire project:

```bash
# Build backend
cd backend
mvn clean package -DskipTests

# Run backend
java -jar target/algorithm-visualizer-1.0.0-jar-with-dependencies.jar

# In another terminal, run frontend
cd frontend
python3 -m http.server 8000
```

Then navigate to `http://localhost:8000`

## Code Quality

- Clean, modular Java code with clear separation of concerns
- Well-commented functions explaining algorithm logic
- Object-oriented design with appropriate abstraction
- No external dependencies except GSON for JSON
- Pure JavaScript frontend without frameworks

## License

Educational use only. Feel free to use, modify, and distribute.

## Contact & Support

For issues, questions, or suggestions, please open an issue on the GitHub repository.

---

**Happy Learning! 🚀** Enjoy visualizing these classic algorithms!
