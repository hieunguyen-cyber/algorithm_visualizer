/**
 * Pathfinding visualization module
 */

class PathfindingVisualizer {
    constructor() {
        this.canvas = document.getElementById('pathfinding-canvas');
        if (!this.canvas) {
            console.error('Canvas element not found!');
            return;
        }
        this.ctx = this.canvas.getContext('2d');
        
        this.gridWidth = CONFIG.PATHFINDING.DEFAULT_GRID_SIZE;
        this.gridHeight = CONFIG.PATHFINDING.DEFAULT_GRID_SIZE;
        this.cellSize = 20;
        
        this.grid = null;
        this.start = { x: 1, y: 1 };
        this.goal = { x: this.gridWidth - 2, y: this.gridHeight - 2 };
        this.obstacles = new Set();
        this.path = [];
        this.closedSet = new Set();
        this.openSet = new Set();
        this.currentNode = null;
        
        this.isRunning = false;
        this.isPaused = false;
        this.isEditable = true;
        this.currentStep = 0;
        this.allSteps = [];
        this.animationSpeed = 100;
        
        this.nodeStates = new Map();
        
        // Grid editing
        this.editMode = CONFIG.PATHFINDING.EDIT_MODES.NONE;
        this.isDrawing = false;
        
        // Panning and interaction
        this.spacePressed = false;
        this.panStart = null;
        
        this.resizeCanvas();
        window.addEventListener('resize', () => this.resizeCanvas());
        this.setupCanvasEvents();
        this.generateMaze();
        this.render();
    }

    setupCanvasEvents() {
        // Mouse events for drawing
        this.canvas.addEventListener('mousedown', (e) => this.onCanvasMouseDown(e));
        this.canvas.addEventListener('mousemove', (e) => this.onCanvasMouseMove(e));
        this.canvas.addEventListener('mouseup', (e) => this.onCanvasMouseUp(e));
        this.canvas.addEventListener('mouseleave', (e) => this.onCanvasMouseLeave(e));
        
        // Touch events for drawing and scrolling
        this.canvas.addEventListener('touchstart', (e) => this.onCanvasTouchStart(e));
        this.canvas.addEventListener('touchmove', (e) => this.onCanvasTouchMove(e));
        this.canvas.addEventListener('touchend', (e) => this.onCanvasTouchEnd(e));
        
        // Space bar for pan mode
        document.addEventListener('keydown', (e) => this.onKeyDown(e));
        document.addEventListener('keyup', (e) => this.onKeyUp(e));
    }

    onCanvasMouseDown(e) {
        if (!this.isEditable || this.isRunning) return;
        
        // Only start drawing with left mouse button (button 0)
        if (e.button !== 0) return;
        
        // Check if space is held (pan mode)
        if (this.spacePressed) {
            this.panStart = { x: e.clientX, y: e.clientY };
            return;
        }
        
        this.isDrawing = true;
        const [x, y] = this.getMouseGridPosition(e);
        this.applyGridEdit(x, y);
    }

    onCanvasMouseMove(e) {
        if (!this.isEditable || this.isRunning) return;
        
        // Handle panning with space bar
        if (this.spacePressed && this.panStart) {
            const container = this.canvas.parentElement;
            const deltaX = e.clientX - this.panStart.x;
            const deltaY = e.clientY - this.panStart.y;
            
            container.scrollLeft -= deltaX;
            container.scrollTop -= deltaY;
            
            this.panStart = { x: e.clientX, y: e.clientY };
            return;
        }
        
        // Update cursor based on mode
        const container = this.canvas.parentElement;
        if (this.spacePressed) {
            this.canvas.style.cursor = 'grab';
        } else if (this.editMode !== CONFIG.PATHFINDING.EDIT_MODES.NONE) {
            this.canvas.style.cursor = 'crosshair';
        } else {
            this.canvas.style.cursor = 'default';
        }
        
        // Only draw if mouse button is held down and in drawing mode
        if (this.isDrawing && e.buttons === 1) {
            const [x, y] = this.getMouseGridPosition(e);
            this.applyGridEdit(x, y);
        }
    }

    onCanvasMouseUp(e) {
        this.isDrawing = false;
        this.panStart = null;
        this.canvas.style.cursor = 'default';
    }

    onCanvasMouseLeave(e) {
        this.isDrawing = false;
        this.panStart = null;
        this.canvas.style.cursor = 'default';
    }
    
    onCanvasTouchStart(e) {
        if (!this.isEditable || this.isRunning) return;
        
        // 2-finger touch for panning (don't prevent default)
        if (e.touches.length === 2) {
            return; // Let touchmove handle panning
        }
        
        // 1-finger touch for drawing
        if (e.touches.length === 1) {
            e.preventDefault();
            this.isDrawing = true;
            const touch = e.touches[0];
            const [x, y] = this.getMouseGridPosition({ 
                clientX: touch.clientX, 
                clientY: touch.clientY 
            });
            this.applyGridEdit(x, y);
        }
    }
    
    onCanvasTouchMove(e) {
        if (!this.isEditable || this.isRunning) return;
        
        // 2-finger touch for panning - don't preventDefault
        if (e.touches.length === 2) {
            return; // Let browser handle native pinch/scroll
        }
        
        // 1-finger touch for drawing
        if (this.isDrawing && e.touches.length === 1) {
            e.preventDefault();
            const touch = e.touches[0];
            const [x, y] = this.getMouseGridPosition({ 
                clientX: touch.clientX, 
                clientY: touch.clientY 
            });
            this.applyGridEdit(x, y);
        }
    }
    
    onCanvasTouchEnd(e) {
        this.isDrawing = false;
    }
    
    onKeyDown(e) {
        if (e.code === 'Space') {
            e.preventDefault();
            this.spacePressed = true;
            this.canvas.style.cursor = 'grab';
        }
    }
    
    onKeyUp(e) {
        if (e.code === 'Space') {
            e.preventDefault();
            this.spacePressed = false;
            this.panStart = null;
            this.canvas.style.cursor = 'default';
        }
    }

    getMouseGridPosition(e) {
        // Get canvas display size and internal resolution
        const rect = this.canvas.getBoundingClientRect();
        
        // Calculate scale factors between display size and internal resolution
        const scaleX = this.canvas.width / rect.width;
        const scaleY = this.canvas.height / rect.height;
        
        // Convert mouse position from viewport to canvas internal coordinates
        const canvasX = (e.clientX - rect.left) * scaleX;
        const canvasY = (e.clientY - rect.top) * scaleY;
        
        // Convert canvas coordinates to grid cell indices
        let col = Math.floor(canvasX / this.cellSize);
        let row = Math.floor(canvasY / this.cellSize);
        
        // Clamp to grid bounds
        col = Math.max(0, Math.min(col, this.gridWidth - 1));
        row = Math.max(0, Math.min(row, this.gridHeight - 1));
        
        return [col, row];
    }

    applyGridEdit(x, y) {
        const key = `${x},${y}`;
        
        switch (this.editMode) {
            case CONFIG.PATHFINDING.EDIT_MODES.DRAW_WALL:
                // Don't place walls on start or goal
                if ((x !== this.start.x || y !== this.start.y) &&
                    (x !== this.goal.x || y !== this.goal.y)) {
                    this.obstacles.add(key);
                }
                break;
            
            case CONFIG.PATHFINDING.EDIT_MODES.ERASE:
                this.obstacles.delete(key);
                break;
            
            case CONFIG.PATHFINDING.EDIT_MODES.SET_START:
                // Remove old start position from obstacles if it was there
                const oldStartKey = `${this.start.x},${this.start.y}`;
                this.obstacles.delete(oldStartKey);
                
                // Set new start position
                this.start = { x, y };
                this.obstacles.delete(key); // Ensure new position is not an obstacle
                
                // Keep tool active for repositioning - do NOT set editMode to NONE
                break;
            
            case CONFIG.PATHFINDING.EDIT_MODES.SET_GOAL:
                // Remove old goal position from obstacles if it was there
                const oldGoalKey = `${this.goal.x},${this.goal.y}`;
                this.obstacles.delete(oldGoalKey);
                
                // Set new goal position
                this.goal = { x, y };
                this.obstacles.delete(key); // Ensure new position is not an obstacle
                
                // Keep tool active for repositioning - do NOT set editMode to NONE
                break;
        }
        
        this.render();
    }

    setEditMode(mode) {
        if (!this.isEditable || this.isRunning) return;
        
        // Toggle: clicking same mode again deactivates it
        if (this.editMode === mode) {
            this.editMode = CONFIG.PATHFINDING.EDIT_MODES.NONE;
        } else {
            this.editMode = mode;
        }
        
        this.updateToolButtonStates();
    }

    updateToolButtonStates() {
        const tools = document.querySelectorAll('.tool-btn');
        tools.forEach(btn => btn.classList.remove('active'));
        
        if (this.editMode !== CONFIG.PATHFINDING.EDIT_MODES.NONE) {
            const modeMap = {
                [CONFIG.PATHFINDING.EDIT_MODES.DRAW_WALL]: '#tool-draw',
                [CONFIG.PATHFINDING.EDIT_MODES.ERASE]: '#tool-erase',
                [CONFIG.PATHFINDING.EDIT_MODES.SET_START]: '#tool-start',
                [CONFIG.PATHFINDING.EDIT_MODES.SET_GOAL]: '#tool-goal',
            };
            const btnId = modeMap[this.editMode];
            if (btnId) {
                document.querySelector(btnId).classList.add('active');
            }
        }
    }

    resizeCanvas() {
        // Target cell size - will be adjusted if needed
        let targetCellSize = 20;
        
        // Target dimensions
        let targetWidth = this.gridWidth * targetCellSize;
        let targetHeight = this.gridHeight * targetCellSize;
        
        // For very small grids, ensure minimum size
        if (targetWidth < 200) targetWidth = 200;
        if (targetHeight < 200) targetHeight = 200;
        
        // Recalculate cell size based on actual target dimensions
        this.cellSize = Math.floor(targetWidth / this.gridWidth);
        
        // Set canvas dimensions (pixel units)
        this.canvas.width = this.gridWidth * this.cellSize;
        this.canvas.height = this.gridHeight * this.cellSize;
        
        this.render();
    }

    generateMaze() {
        this.obstacles.clear();
        this.nodeStates.clear();
        
        // Add border walls
        for (let i = 0; i < this.gridWidth; i++) {
            this.obstacles.add(`${i},0`);
            this.obstacles.add(`${i},${this.gridHeight - 1}`);
        }
        for (let i = 0; i < this.gridHeight; i++) {
            this.obstacles.add(`0,${i}`);
            this.obstacles.add(`${this.gridWidth - 1},${i}`);
        }
        
        // Add default obstacles
        CONFIG.PATHFINDING.DEFAULT_OBSTACLES.forEach(([x, y]) => {
            if (x >= 0 && x < this.gridWidth && y >= 0 && y < this.gridHeight) {
                this.obstacles.add(`${x},${y}`);
            }
        });
    }

    setGridSize(width, height) {
        this.gridWidth = width;
        this.gridHeight = height;
        this.goal = { x: Math.max(1, width - 2), y: Math.max(1, height - 2) };
        this.resizeCanvas();
        this.reset();
    }

    setAnimationSpeed(speed) {
        this.animationSpeed = 600 - speed + 10; // Invert: higher speed = lower delay
    }

    async run(algorithm) {
        this.isRunning = true;
        this.isEditable = false;
        this.isPaused = false;
        this.currentStep = 0;
        this.nodeStates.clear();
        this.path = [];
        this.closedSet.clear();
        this.openSet.clear();
        this.currentNode = null;
        this.editMode = CONFIG.PATHFINDING.EDIT_MODES.NONE;
        this.updateToolButtonStates();
        
        console.log(`🔍 Running ${algorithm.toUpperCase()} on ${this.gridWidth}x${this.gridHeight} grid`);
        
        // Call backend API
        const obstacles = Array.from(this.obstacles).map(key => {
            const [x, y] = key.split(',').map(Number);
            return [x, y];
        });
        
        console.log('📤 Sending request to backend...');
        const result = await API.pathfind(
            algorithm,
            this.gridWidth,
            this.gridHeight,
            this.start.x,
            this.start.y,
            this.goal.x,
            this.goal.y,
            obstacles
        );
        console.log('📥 Response received:', result);

        if (!result || result.error) {
            alert('Error running algorithm: ' + (result?.error || 'Unknown error'));
            this.isRunning = false;
            this.isPaused = false;
            this.isEditable = true;
            return;
        }

        // Check if no solution found
        if (result.hasSolution === false) {
            console.log('⚠️ No solution found');
            document.getElementById('visited-count').textContent = result.visitedCount || 0;
            document.getElementById('path-length').textContent = '0 (No solution)';
            this.allSteps = result.steps || [];
            this.path = [];
            
            // Animate the explored nodes
            await this.animateSteps();
            
            this.isRunning = false;
            this.isEditable = true;
            alert('No path found from start to goal!');
            return;
        }

        this.allSteps = result.steps || [];
        this.path = result.path || [];
        
        // Update UI
        document.getElementById('visited-count').textContent = result.visitedCount || 0;
        document.getElementById('path-length').textContent = this.path.length;
        
        // Animate steps
        await this.animateSteps();
        
        this.isRunning = false;
        this.isEditable = true;
    }

    async animateSteps() {
        while (this.currentStep < this.allSteps.length && this.isRunning) {
            if (!this.isPaused) {
                const step = this.allSteps[this.currentStep];
                this.processStep(step);
                this.render();
                this.currentStep++;
                
                await new Promise(resolve => setTimeout(resolve, this.animationSpeed));
            } else {
                await new Promise(resolve => setTimeout(resolve, 100));
            }
        }
        
        // Render final path
        if (this.isRunning) {
            this.path.forEach(node => {
                if (!(node.x === this.start.x && node.y === this.start.y) &&
                    !(node.x === this.goal.x && node.y === this.goal.y)) {
                    this.nodeStates.set(`${node.x},${node.y}`, 'path');
                }
            });
            this.render();
        }
    }

    processStep(step) {
        const key = `${step.x},${step.y}`;
        
        switch (step.type) {
            case 'visit':
                this.closedSet.add(key);
                this.openSet.delete(key);
                this.nodeStates.set(key, 'closed');
                break;
            case 'open':
                this.openSet.add(key);
                if (!this.closedSet.has(key)) {
                    this.nodeStates.set(key, 'open');
                }
                break;
            case 'path':
                this.nodeStates.set(key, 'path');
                break;
        }
    }

    step() {
        if (this.currentStep < this.allSteps.length) {
            const step = this.allSteps[this.currentStep];
            this.processStep(step);
            this.render();
            this.currentStep++;
        }
    }

    pause() {
        this.isPaused = true;
    }

    resume() {
        this.isPaused = false;
    }

    reset() {
        this.isRunning = false;
        this.isPaused = false;
        this.isEditable = true;
        this.currentStep = 0;
        this.allSteps = [];
        this.nodeStates.clear();
        this.path = [];
        this.closedSet.clear();
        this.openSet.clear();
        this.currentNode = null;
        this.editMode = CONFIG.PATHFINDING.EDIT_MODES.NONE;
        this.updateToolButtonStates();
        this.generateMaze();
        this.render();
    }

    render() {
        // Clear canvas
        this.ctx.fillStyle = CONFIG.PATHFINDING.COLORS.EMPTY;
        this.ctx.fillRect(0, 0, this.canvas.width, this.canvas.height);

        // Draw grid cells
        for (let y = 0; y < this.gridHeight; y++) {
            for (let x = 0; x < this.gridWidth; x++) {
                const key = `${x},${y}`;
                let color = CONFIG.PATHFINDING.COLORS.EMPTY;

                if (this.obstacles.has(key)) {
                    color = CONFIG.PATHFINDING.COLORS.WALL;
                } else if (x === this.start.x && y === this.start.y) {
                    color = CONFIG.PATHFINDING.COLORS.START;
                } else if (x === this.goal.x && y === this.goal.y) {
                    color = CONFIG.PATHFINDING.COLORS.GOAL;
                } else {
                    const state = this.nodeStates.get(key);
                    if (state === 'path') {
                        color = CONFIG.PATHFINDING.COLORS.PATH;
                    } else if (state === 'current') {
                        color = CONFIG.PATHFINDING.COLORS.CURRENT;
                    } else if (state === 'closed') {
                        color = CONFIG.PATHFINDING.COLORS.CLOSED;
                    } else if (state === 'open') {
                        color = CONFIG.PATHFINDING.COLORS.OPEN;
                    }
                }

                const x0 = x * this.cellSize;
                const y0 = y * this.cellSize;
                
                this.ctx.fillStyle = color;
                this.ctx.fillRect(x0, y0, this.cellSize, this.cellSize);
                
                // Draw border only for very small cell sizes for performance
                if (this.cellSize > 4) {
                    this.ctx.strokeStyle = '#e5e7eb';
                    this.ctx.lineWidth = 0.5;
                    this.ctx.strokeRect(x0, y0, this.cellSize, this.cellSize);
                }
            }
        }
    }
}

// Initialize visualizer
let pathfindingViz = null;

function initPathfinding() {
    if (!pathfindingViz) {
        try {
            pathfindingViz = new PathfindingVisualizer();
            console.log('✅ Pathfinding visualizer initialized');
        } catch (error) {
            console.error('❌ Failed to initialize pathfinding visualizer:', error);
            throw error;
        }
    }
}

// Expose globally for HTML event handlers
window.initPathfinding = initPathfinding;
window.pathfindingViz = pathfindingViz;
