export default function handler(req, res) {
    if (req.method !== 'POST') {
        return res.status(405).json({ error: 'Method not allowed' });
    }

    const { algorithm, gridWidth, gridHeight, startX, startY, goalX, goalY, obstacles } = req.body;

    const obstacleSet = new Set(obstacles.map(o => `${o[0]},${o[1]}`));
    
    // Grid bounds check
    const isValid = (x, y) => x >= 0 && x < gridWidth && y >= 0 && y < gridHeight && !obstacleSet.has(`${x},${y}`);

    const getNeighbors = (x, y) => {
        const neighbors = [];
        const dirs = [[0, -1], [1, 0], [0, 1], [-1, 0]]; // Up, Right, Down, Left
        for (const [dx, dy] of dirs) {
            if (isValid(x + dx, y + dy)) {
                neighbors.push({ x: x + dx, y: y + dy });
            }
        }
        return neighbors;
    };

    const heuristic = (x1, y1, x2, y2) => Math.abs(x1 - x2) + Math.abs(y1 - y2);

    let steps = [];
    let visitCount = 0;
    
    const runSearch = () => {
        // We will use an array and sort it manually as a simple priority queue
        let openSet = [];
        let closedSet = new Set();
        let cameFrom = new Map();
        
        let startNode = { x: startX, y: startY, g: 0, h: heuristic(startX, startY, goalX, goalY), f: 0 };
        startNode.f = startNode.g + startNode.h;
        
        openSet.push(startNode);
        
        const popNext = () => {
            if (algorithm === 'bfs') {
                return openSet.shift();
            } else if (algorithm === 'dfs' || algorithm === 'dls') {
                return openSet.pop();
            } else {
                // Priority queue based algorithms
                let bestIdx = 0;
                for (let i = 1; i < openSet.length; i++) {
                    if (algorithm === 'astar' || algorithm === 'astar-dijkstra') {
                        if (openSet[i].f < openSet[bestIdx].f || (openSet[i].f === openSet[bestIdx].f && openSet[i].h < openSet[bestIdx].h)) {
                            bestIdx = i;
                        }
                    } else if (algorithm === 'dijkstra' || algorithm === 'ucs') {
                        if (openSet[i].g < openSet[bestIdx].g) bestIdx = i;
                    } else if (algorithm === 'greedy') {
                        if (openSet[i].h < openSet[bestIdx].h) bestIdx = i;
                    }
                }
                return openSet.splice(bestIdx, 1)[0];
            }
        };

        const depthLimit = algorithm === 'dls' ? 15 : (algorithm === 'iddfs' ? 1 : Infinity);
        
        // For IDDFS
        let currentLimit = algorithm === 'iddfs' ? 0 : depthLimit;
        let maxLimit = gridWidth * gridHeight;
        
        const runIteration = (limit) => {
            openSet = [{ ...startNode }];
            closedSet = new Set();
            cameFrom = new Map();
            let iterationSteps = [];
            
            while (openSet.length > 0) {
                if (visitCount > 5000) break; // Safety timeout
                
                let current = popNext();
                let key = `${current.x},${current.y}`;
                
                if (closedSet.has(key)) continue;
                closedSet.add(key);
                visitCount++;
                
                iterationSteps.push({ type: 'visit', x: current.x, y: current.y, gCost: current.g, hCost: current.h, fCost: current.f });
                
                if (current.x === goalX && current.y === goalY) {
                    let path = [];
                    let currKey = key;
                    while (cameFrom.has(currKey)) {
                        const [cx, cy] = currKey.split(',').map(Number);
                        path.unshift({ x: cx, y: cy });
                        currKey = cameFrom.get(currKey);
                    }
                    path.unshift({ x: startX, y: startY });
                    steps.push(...iterationSteps);
                    return { success: true, visitCount, steps, path };
                }
                
                // Depth limit for DFS variants
                if (current.g >= limit) continue;

                let neighbors = getNeighbors(current.x, current.y);
                // Reverse for DFS to explore naturally
                if (algorithm === 'dfs' || algorithm === 'dls' || algorithm === 'iddfs') {
                    neighbors.reverse();
                }

                for (let n of neighbors) {
                    let nKey = `${n.x},${n.y}`;
                    if (closedSet.has(nKey)) continue;
                    
                    let gCost = current.g + 1;
                    let hCost = heuristic(n.x, n.y, goalX, goalY);
                    let fCost = gCost + hCost;
                    
                    // Simple logic for non-A* algorithms
                    if (algorithm === 'greedy') {
                        gCost = 0; fCost = hCost;
                    } else if (algorithm === 'dijkstra' || algorithm === 'ucs') {
                        hCost = 0; fCost = gCost;
                    }
                    
                    let existing = openSet.find(o => o.x === n.x && o.y === n.y);
                    if (!existing || gCost < existing.g) {
                        if (existing) {
                            existing.g = gCost;
                            existing.h = hCost;
                            existing.f = fCost;
                        } else {
                            openSet.push({ x: n.x, y: n.y, g: gCost, h: hCost, f: fCost });
                        }
                        cameFrom.set(nKey, key);
                        iterationSteps.push({ type: 'open', x: n.x, y: n.y, gCost, hCost, fCost });
                    }
                }
            }
            steps.push(...iterationSteps);
            return null;
        };

        if (algorithm === 'iddfs') {
            for (let limit = 0; limit < maxLimit; limit++) {
                let res = runIteration(limit);
                if (res) return res;
            }
        } else {
            let res = runIteration(depthLimit);
            if (res) return res;
        }

        return { success: false, visitCount, steps, path: [] };
    };

    const result = runSearch();
    res.status(200).json(result);
}
