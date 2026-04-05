/**
 * Main application controller
 */

document.addEventListener('DOMContentLoaded', () => {
    console.log('🚀 Application initializing...');
    console.log('📡 Backend API:', CONFIG.API_BASE);
    
    try {
        setupTabNavigation();
        setupPathfindingControls();
        setupGameControls();
        setupSudokuControls();
        
        // Initialize visualizations
        console.log('Initializing pathfinding visualizer...');
        if (typeof initPathfinding === 'function') {
            initPathfinding();
        } else {
            console.error('❌ initPathfinding is not defined');
        }
        
        console.log('Initializing game visualizer...');
        if (typeof initGame === 'function') {
            initGame();
        } else {
            console.error('❌ initGame is not defined');
        }

        console.log('Initializing Sudoku visualizer...');
        if (typeof initSudoku === 'function') {
            initSudoku();
        } else {
            console.error('❌ initSudoku is not defined');
        }
        
        console.log('✅ Application initialized successfully');
    } catch (error) {
        console.error('❌ Initialization error:', error);
        alert('Error initializing application: ' + error.message);
    }
});

function setupTabNavigation() {
    const navButtons = document.querySelectorAll('.nav-btn');
    const sections = document.querySelectorAll('.section');

    navButtons.forEach(btn => {
        btn.addEventListener('click', () => {
            const tabName = btn.getAttribute('data-tab');
            
            // Update active button
            navButtons.forEach(b => b.classList.remove('active'));
            btn.classList.add('active');
            
            // Update active section
            sections.forEach(section => section.classList.remove('active'));
            document.getElementById(tabName + '-section').classList.add('active');
            
            // Trigger resize for visualizations
            setTimeout(() => {
                window.dispatchEvent(new Event('resize'));
            }, 100);
        });
    });
}

function setupPathfindingControls() {
    const algoSelect = document.getElementById('algo-select');
    const gridWidthInput = document.getElementById('grid-width');
    const gridWidthDisplay = document.getElementById('grid-width-display');
    const gridHeightInput = document.getElementById('grid-height');
    const gridHeightDisplay = document.getElementById('grid-height-display');
    const speedControl = document.getElementById('speed-control');
    const speedDisplay = document.getElementById('speed-display');
    
    const startBtn = document.getElementById('start-btn');
    const pauseBtn = document.getElementById('pause-btn');
    const resetBtn = document.getElementById('reset-btn');
    const stepBtn = document.getElementById('step-btn');
    
    // Grid editing tools
    const toolDraw = document.getElementById('tool-draw');
    const toolErase = document.getElementById('tool-erase');
    const toolStart = document.getElementById('tool-start');
    const toolGoal = document.getElementById('tool-goal');

    // Grid width control
    gridWidthInput.addEventListener('change', () => {
        const width = parseInt(gridWidthInput.value);
        const height = parseInt(gridHeightInput.value);
        gridWidthDisplay.textContent = width;
        if (pathfindingViz) {
            pathfindingViz.setGridSize(width, height);
        }
    });

    // Grid height control
    gridHeightInput.addEventListener('change', () => {
        const width = parseInt(gridWidthInput.value);
        const height = parseInt(gridHeightInput.value);
        gridHeightDisplay.textContent = height;
        if (pathfindingViz) {
            pathfindingViz.setGridSize(width, height);
        }
    });

    // Speed control
    speedControl.addEventListener('change', () => {
        const speed = parseInt(speedControl.value);
        speedDisplay.textContent = getSpeedLabel(speed);
        if (pathfindingViz) {
            pathfindingViz.setAnimationSpeed(speed);
        }
    });

    // Grid editing tools
    toolDraw.addEventListener('click', () => {
        if (pathfindingViz) {
            pathfindingViz.setEditMode(CONFIG.PATHFINDING.EDIT_MODES.DRAW_WALL);
        }
    });

    toolErase.addEventListener('click', () => {
        if (pathfindingViz) {
            pathfindingViz.setEditMode(CONFIG.PATHFINDING.EDIT_MODES.ERASE);
        }
    });

    toolStart.addEventListener('click', () => {
        if (pathfindingViz) {
            pathfindingViz.setEditMode(CONFIG.PATHFINDING.EDIT_MODES.SET_START);
        }
    });

    toolGoal.addEventListener('click', () => {
        if (pathfindingViz) {
            pathfindingViz.setEditMode(CONFIG.PATHFINDING.EDIT_MODES.SET_GOAL);
        }
    });

    // Start button
    startBtn.addEventListener('click', async () => {
        const algorithm = algoSelect.value;
        startBtn.disabled = true;
        pauseBtn.disabled = false;
        stepBtn.disabled = true;
        algoSelect.disabled = true;
        gridWidthInput.disabled = true;
        gridHeightInput.disabled = true;
        toolDraw.disabled = true;
        toolErase.disabled = true;
        toolStart.disabled = true;
        toolGoal.disabled = true;
        
        await pathfindingViz.run(algorithm);
        
        startBtn.disabled = false;
        pauseBtn.disabled = true;
        stepBtn.disabled = false;
        algoSelect.disabled = false;
        gridWidthInput.disabled = false;
        gridHeightInput.disabled = false;
        toolDraw.disabled = false;
        toolErase.disabled = false;
        toolStart.disabled = false;
        toolGoal.disabled = false;
        pauseBtn.textContent = '⏸ Pause';
    });

    // Pause button
    pauseBtn.addEventListener('click', () => {
        if (pathfindingViz.isPaused) {
            pathfindingViz.resume();
            pauseBtn.textContent = '⏸ Pause';
        } else {
            pathfindingViz.pause();
            pauseBtn.textContent = '▶ Resume';
        }
    });

    // Reset button
    resetBtn.addEventListener('click', () => {
        pathfindingViz.reset();
        startBtn.disabled = false;
        pauseBtn.disabled = true;
        stepBtn.disabled = false;
        algoSelect.disabled = false;
        gridSizeInput.disabled = false;
        pauseBtn.textContent = '⏸ Pause';
        document.getElementById('visited-count').textContent = '0';
        document.getElementById('path-length').textContent = '0';
    });

    // Step button
    stepBtn.addEventListener('click', () => {
        pathfindingViz.step();
    });
}

function setupGameControls() {
    const gameNewBtn = document.getElementById('game-new-btn');
    const gameResetBtn = document.getElementById('game-reset-btn');
    const gameWidthInput = document.getElementById('game-width');
    const gameHeightInput = document.getElementById('game-height');
    const gameWinKInput = document.getElementById('game-win-k');

    gameNewBtn.addEventListener('click', async () => {
        console.log('🎮 "New Game" button clicked');
        if (gameViz) {
            console.log('📊 Current gameViz:', gameViz);
            gameViz.boardWidth = parseInt(gameWidthInput.value);
            gameViz.boardHeight = parseInt(gameHeightInput.value);
            console.log(`📐 New board size: ${gameViz.boardWidth}x${gameViz.boardHeight}`);
            
            // Constrain k to [3, 5]
            let k = parseInt(gameWinKInput.value);
            k = Math.max(3, Math.min(5, k));
            k = Math.min(k, Math.min(gameViz.boardWidth, gameViz.boardHeight));
            gameViz.winCondition = k;
            
            gameWinKInput.value = k;
            document.getElementById('game-win-k-display').textContent = k;
            
            console.log('🎯 Calling initNewGame()...');
            await gameViz.initNewGame();
            console.log('✅ initNewGame() completed');
        } else {
            console.error('❌ gameViz is not defined');
        }
    });

    gameResetBtn.addEventListener('click', async () => {
        if (gameViz) {
            await gameViz.resetGame();
        }
    });

    // Board size controls
    gameWidthInput.addEventListener('input', (e) => {
        document.getElementById('game-width-display').textContent = e.target.value;
        // Update max k to match board dimensions
        const width = parseInt(e.target.value);
        const height = parseInt(gameHeightInput.value);
        const maxK = Math.min(width, height);
        gameWinKInput.max = maxK;
    });

    gameHeightInput.addEventListener('input', (e) => {
        document.getElementById('game-height-display').textContent = e.target.value;
        // Update max k to match board dimensions
        const width = parseInt(gameWidthInput.value);
        const height = parseInt(e.target.value);
        const maxK = Math.min(width, height);
        gameWinKInput.max = maxK;
    });

    gameWinKInput.addEventListener('input', (e) => {
        let k = Math.max(3, Math.min(5, parseInt(e.target.value)));
        document.getElementById('game-win-k-display').textContent = k;
        gameWinKInput.value = k;
    });
}

function getSpeedLabel(speed) {
    if (speed <= 50) return 'Slow';
    if (speed <= 150) return 'Medium';
    if (speed <= 300) return 'Fast';
    return 'Very Fast';
}

function setupSudokuControls() {
    // Sudoku controls are handled by SudokuVisualizer class
    // This function is a placeholder for potential future enhancements
}

// Add smooth scrolling and animations
window.addEventListener('load', () => {
    document.body.style.opacity = '0';
    setTimeout(() => {
        document.body.style.transition = 'opacity 0.5s ease';
        document.body.style.opacity = '1';
    }, 100);
});
