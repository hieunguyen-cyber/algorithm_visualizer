/**
 * Configuration constants
 */

const CONFIG = {
    API_BASE: '/api',
    
    PATHFINDING: {
        DEFAULT_GRID_SIZE: 20,
        MIN_GRID_SIZE: 10,
        MAX_GRID_SIZE: 60,
        DEFAULT_SPEED: 100,
        
        // Colors
        COLORS: {
            START: '#10b981',      // Green
            GOAL: '#ff6b6b',       // Red
            WALL: '#1f2937',       // Dark gray
            OPEN: '#60a5fa',       // Blue
            CLOSED: '#8b5cf6',     // Purple
            PATH: '#f59e0b',       // Amber
            CURRENT: '#fbbf24',    // Gold (current node being examined)
            EMPTY: '#ffffff',      // White
        },
        
        // Grid editing modes
        EDIT_MODES: {
            NONE: 'none',
            DRAW_WALL: 'draw_wall',
            ERASE: 'erase',
            SET_START: 'set_start',
            SET_GOAL: 'set_goal',
        },
        
        DEFAULT_OBSTACLES: [
            // Create a simple maze pattern
            ...Array.from({length: 10}, (_, i) => [5, i + 2]),
            ...Array.from({length: 10}, (_, i) => [15, i + 8]),
            [8, 8], [8, 9], [8, 10], [8, 11],
            [12, 12], [12, 13], [12, 14],
        ],
    },
    
    GAME: {
        BOARD_WIDTH: 3,
        BOARD_HEIGHT: 3,
        WIN_CONDITION: 3,  // k-in-a-row
        
        // Constraints
        MIN_BOARD_SIZE: 3,
        MAX_BOARD_SIZE: 10,
        MIN_WIN_K: 3,
        
        // Player values
        EMPTY: 0,
        HUMAN: 1,
        AI: 2,
        
        // Display
        HUMAN_SYMBOL: 'X',
        AI_SYMBOL: 'O',
    },
    
    SPEED_LEVELS: {
        10: 'Slowest',
        50: 'Slow',
        100: 'Medium',
        200: 'Fast',
        500: 'Fastest',
    },
};

// Utility functions
const API = {
    async pathfind(algorithm, gridWidth, gridHeight, startX, startY, goalX, goalY, obstacles) {
        try {
            console.log(`\ud83d\udd0d Calling API: POST /api/pathfinding`);
            const response = await fetch(CONFIG.API_BASE + '/pathfinding', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    algorithm,
                    gridWidth,
                    gridHeight,
                    startX,
                    startY,
                    goalX,
                    goalY,
                    obstacles,
                }),
            });
            
            if (!response.ok) {
                console.error(`\u274c HTTP Error: ${response.status}`, response.statusText);
                throw new Error(`HTTP ${response.status}: ${response.statusText}`);
            }
            
            const data = await response.json();
            console.log(`\u2705 API Response received:`, data);
            return data;
        } catch (error) {
            console.error('\u274c API Error:', error);
            
            // Check if backend is running
            if (error.message.includes('Failed to fetch') || error.message.includes('CORS')) {
                alert('❌ Cannot connect to backend server!\n\n' +
                      'Make sure the backend is running:\n' +
                      '  ./run-backend.sh\n\n' +
                      'Backend should be accessible at: ' + CONFIG.API_BASE);
            } else {
                alert('Server error: ' + error.message);
            }
            return null;
        }
    },

    async gameInit(boardWidth = 3, boardHeight = 3, winCondition = 3) {
        try {
            const response = await fetch(CONFIG.API_BASE + '/game/init', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({ boardWidth, boardHeight, winCondition }),
            });
            return await response.json();
        } catch (error) {
            console.error('API Error:', error);
            return null;
        }
    },

    async gameMove(board, row, col, player, winCondition) {
        try {
            const response = await fetch(CONFIG.API_BASE + '/game/move', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({ board, row, col, player, winCondition }),
            });
            return await response.json();
        } catch (error) {
            console.error('API Error:', error);
            return null;
        }
    },

    async gameAIMove(board, algorithm = 'minimax', depth = 10, winCondition = 3) {
        try {
            const response = await fetch(CONFIG.API_BASE + '/game/ai-move', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({ board, algorithm, depth, winCondition }),
            });
            return await response.json();
        } catch (error) {
            console.error('API Error:', error);
            return null;
        }
    },

    async gameGetState() {
        try {
            const response = await fetch(CONFIG.API_BASE + '/game/state');
            return await response.json();
        } catch (error) {
            console.error('API Error:', error);
            return null;
        }
    },

    async gameReset() {
        try {
            const response = await fetch(CONFIG.API_BASE + '/game/reset', {
                method: 'POST',
            });
            return await response.json();
        } catch (error) {
            console.error('API Error:', error);
            return null;
        }
    },

    // ==================== Sudoku API ====================

    async sudokuValidate(board) {
        try {
            const response = await fetch(CONFIG.API_BASE + '/sudoku/validate', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({ board }),
            });
            return await response.json();
        } catch (error) {
            console.error('API Error:', error);
            return null;
        }
    },

    async sudokuCheckSolvable(board) {
        try {
            const response = await fetch(CONFIG.API_BASE + '/sudoku/check-solvable', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({ board }),
            });
            return await response.json();
        } catch (error) {
            console.error('API Error:', error);
            return null;
        }
    },

    async sudokuSolve(board) {
        try {
            const response = await fetch(CONFIG.API_BASE + '/sudoku/solve', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({ board }),
            });
            return await response.json();
        } catch (error) {
            console.error('API Error:', error);
            return null;
        }
    },
};
