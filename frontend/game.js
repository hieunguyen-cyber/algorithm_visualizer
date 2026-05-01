/**
 * Game (Tic-Tac-Toe) visualization module
 */

class GameVisualizer {
    constructor() {
        this.boardWidth = CONFIG.GAME.BOARD_WIDTH;
        this.boardHeight = CONFIG.GAME.BOARD_HEIGHT;
        this.winCondition = CONFIG.GAME.WIN_CONDITION;
        this.board = Array(this.boardHeight).fill(null).map(() => Array(this.boardWidth).fill(CONFIG.GAME.EMPTY));
        this.gameActive = false;
        this.humanTurn = true;
        this.lastAIMoves = { treeNodes: 0, prunedNodes: 0 };
        this.aiMoveInProgress = false;
        this.isThinking = false;  // Flag to prevent re-renders during AI thinking
        this.lastRenderTime = 0;  // Track last render to prevent race conditions
        
        this.setupEventListeners();
        this.renderBoard();
    }

    setupEventListeners() {
        const boardElement = document.getElementById('game-board');
        boardElement.addEventListener('click', (e) => this.handleBoardClick(e));
    }

    async initNewGame() {
        console.log('🎮 initNewGame() called');
        console.log(`📐 Board dimensions: ${this.boardWidth}x${this.boardHeight}, Win condition: ${this.winCondition}`);
        
        // Constrain win condition: k must be >= 3 and <= 5, and <= min(width, height)
        const maxK = Math.min(this.boardWidth, this.boardHeight);
        this.winCondition = Math.max(3, Math.min(5, Math.min(this.winCondition, maxK)));
        
        console.log('📡 Calling API.gameInit...');
        const result = await API.gameInit(this.boardWidth, this.boardHeight, this.winCondition);
        console.log('📥 API response:', result);
        
        if (result && result.success) {
            console.log('✅ API call successful, initializing board...');
            this.board = Array(this.boardHeight).fill(null).map(() => Array(this.boardWidth).fill(CONFIG.GAME.EMPTY));
            this.gameActive = true;
            this.humanTurn = true;
            this.aiMoveInProgress = false;
            this.updateStatus(`Game started on ${this.boardWidth}x${this.boardHeight} board (${this.winCondition}-in-a-row). Your turn (X)`);
            
            console.log('🎨 Calling renderBoard()...');
            this.renderBoard();
            console.log('✅ renderBoard() completed');
            
            document.getElementById('moves-count').textContent = '0';
        } else {
            console.error('❌ API call failed:', result);
        }
    }

    async handleBoardClick(e) {
        if (!this.gameActive || !this.humanTurn || this.aiMoveInProgress) return;

        const rect = e.currentTarget.getBoundingClientRect();
        const x = Math.floor((e.clientX - rect.left) / (rect.width / this.boardWidth));
        const y = Math.floor((e.clientY - rect.top) / (rect.height / this.boardHeight));

        if (x >= 0 && x < this.boardWidth && y >= 0 && y < this.boardHeight) {
            await this.makeMove(y, x, CONFIG.GAME.HUMAN);
        }
    }

    async makeMove(row, col, player) {
        // Prevent multiple simultaneous moves
        if (this.isThinking && player === CONFIG.GAME.HUMAN) {
            console.warn('⚠️  Ignoring move - AI is thinking');
            return;
        }

        const result = await API.gameMove(this.board, row, col, player, this.winCondition);
        console.log('Move result:', result);
        
        if (result && result.success) {
            // Update board state immediately
            this.board = result.board;
            document.getElementById('moves-count').textContent = result.board.flat().filter(c => c !== 0).length;
            
            if (result.gameOver) {
                // Game is over - render final state
                this.gameActive = false;
                this.aiMoveInProgress = false;
                this.isThinking = false;
                
                if (result.winner === 'AI') {
                    this.updateStatus('Game Over. AI Won! 🤖');
                } else if (result.winner === 'Human') {
                    this.updateStatus('Game Over. You Won! 🎉');
                } else {
                    this.updateStatus('Game Over. Draw! 🤝');
                }
                this.renderBoard();
            } else if (player === CONFIG.GAME.HUMAN) {
                // Human just moved - set thinking state and schedule AI
                this.humanTurn = false;
                this.isThinking = true;
                this.updateStatus('AI thinking...');
                // DO NOT render here - let renderBoard only be called when state is complete
                // Render the board to show human move immediately
                this.renderBoard();
                
                // Schedule AI move with short delay
                setTimeout(() => this.triggerAIMove(), 300);
                return;  // Don't render again here
            } else {
                // AI just moved - back to human turn
                this.humanTurn = true;
                this.isThinking = false;
                this.updateStatus('Your turn');
                this.renderBoard();
            }
        } else {
            console.error('Move failed:', result);
        }
    }

    async triggerAIMove() {
        if (!this.gameActive || this.aiMoveInProgress) {
            console.warn('⚠️  Ignoring AI move - game not active or already in progress');
            return;
        }
        
        this.aiMoveInProgress = true;
        const algorithm = document.getElementById('game-algo').value || 'minimax';
        const depth = this.calculateDepthLimit();
        
        console.log(`🤖 AI move starting (${algorithm}, depth=${depth})`);
        try {
            const result = await API.gameAIMove(this.board, algorithm, depth, this.winCondition);
            console.log('AI move result:', result);
            
            this.aiMoveInProgress = false;
            
            if (result && result.success) {
                // Update board state from AI move
                this.board = result.board;
                this.lastAIMoves.treeNodes = result.treeNodeCount || 0;
                this.lastAIMoves.prunedNodes = result.prunedCount || 0;
                
                document.getElementById('tree-nodes').textContent = result.treeNodeCount || 0;
                document.getElementById('pruned-nodes').textContent = result.prunedCount || 0;
                document.getElementById('moves-count').textContent = result.board.flat().filter(c => c !== 0).length;
                
                if (result.gameOver) {
                    // Game is over
                    this.gameActive = false;
                    this.isThinking = false;
                    
                    if (result.winner === 'AI') {
                        this.updateStatus('Game Over. AI Won! 🤖');
                    } else if (result.winner === 'Human') {
                        this.updateStatus('Game Over. You Won! 🎉');
                    } else {
                        this.updateStatus('Game Over. Draw! 🤝');
                    }
                } else {
                    // Back to human turn
                    this.humanTurn = true;
                    this.isThinking = false;
                    this.updateStatus('Your turn');
                }
                
                // Render board ONCE after AI move is complete and all state is updated
                this.renderBoard();
            } else {
                console.error('AI move failed:', result);
                this.aiMoveInProgress = false;
                this.isThinking = false;
                this.humanTurn = true;
                this.updateStatus('AI move failed, your turn');
                this.renderBoard();
            }
        } catch (error) {
            console.error('❌ AI move error:', error);
            this.aiMoveInProgress = false;
            this.isThinking = false;
            this.humanTurn = true;
            this.updateStatus('Error during AI move, your turn');
            this.renderBoard();
        }
    }

    calculateDepthLimit() {
        // Adaptive depth limit based on board size
        // CRITICAL: Must limit depth to prevent exponential AI freezes
        const boardArea = this.boardWidth * this.boardHeight;
        if (boardArea <= 9) return 5;     // 3x3: depth 5 (deep but safe)
        if (boardArea <= 16) return 4;    // 4x4: depth 4
        if (boardArea <= 25) return 3;    // 5x5: depth 3
        if (boardArea <= 36) return 3;    // 6x6: depth 3
        return 2;                         // larger boards: depth 2 maximum
    }

    async getAIMove() {
        const algorithm = document.getElementById('game-algo').value;
        const result = await API.gameAIMove(this.board, algorithm, this.calculateDepthLimit(), this.winCondition);
        
        if (result && result.success) {
            this.board = result.board;
            this.lastAIMoves.treeNodes = result.treeNodeCount || 0;
            this.lastAIMoves.prunedNodes = result.prunedCount || 0;
            
            document.getElementById('tree-nodes').textContent = result.treeNodeCount || 0;
            document.getElementById('pruned-nodes').textContent = result.prunedCount || 0;
            document.getElementById('moves-count').textContent = result.board.flat().filter(c => c !== 0).length;
            
            if (result.gameOver) {
                this.gameActive = false;
                if (result.winner === 'AI') {
                    this.updateStatus('Game Over. AI Won! 🤖');
                } else if (result.winner === 'Human') {
                    this.updateStatus('Game Over. You Won! 🎉');
                } else {
                    this.updateStatus('Game Over. Draw! 🤝');
                }
            } else {
                this.humanTurn = true;
                this.updateStatus('Your turn');
            }
            
            this.renderBoard();
        }
    }

    async resetGame() {
        const result = await API.gameReset();
        if (result && result.success) {
            this.board = result.board;
            this.gameActive = true;
            this.humanTurn = true;
            this.updateStatus('Game reset. Your turn (X)');
            this.renderBoard();
            document.getElementById('moves-count').textContent = '0';
            document.getElementById('tree-nodes').textContent = '0';
            document.getElementById('pruned-nodes').textContent = '0';
        }
    }

    renderBoard() {
        // Prevent race conditions: skip if rendering too quickly
        const now = Date.now();
        if (now - this.lastRenderTime < 100) {
            console.log('⚠️  Render skipped (too frequent)');
            return;
        }
        this.lastRenderTime = now;
        
        console.log(`🎨 renderBoard() called (isThinking=${this.isThinking})`);
        const boardElement = document.getElementById('game-board');
        
        if (!boardElement) {
            console.error('❌ game-board element not found in DOM');
            return;
        }
        
        const maxWidth = boardElement.parentElement.clientWidth - 40;
        const maxHeight = 500; // Max height constraint
        
        // Calculate optimal size while maintaining aspect ratio
        const width = Math.min(maxWidth, 450);
        const aspectRatio = this.boardHeight / this.boardWidth;
        const height = Math.min(maxHeight, width * aspectRatio);
        
        boardElement.style.width = width + 'px';
        boardElement.style.height = height + 'px';
        
        // Clear DOM completely before rebuilding
        boardElement.innerHTML = '';
        console.log('🧹 Cleared board innerHTML');
        
        boardElement.style.display = 'grid';
        boardElement.style.gridTemplateColumns = `repeat(${this.boardWidth}, 1fr)`;
        boardElement.style.gridTemplateRows = `repeat(${this.boardHeight}, 1fr)`;
        boardElement.style.gap = '3px';
        boardElement.style.backgroundColor = '#1f2937';
        boardElement.style.padding = '10px';
        boardElement.style.borderRadius = '8px';
        boardElement.style.pointerEvents = 'auto';
        
        const cellWidth = width / this.boardWidth;
        const cellHeight = height / this.boardHeight;
        const fontSize = Math.min(cellWidth, cellHeight) * 0.5;
        
        let cellCount = 0;
        for (let row = 0; row < this.boardHeight; row++) {
            for (let col = 0; col < this.boardWidth; col++) {
                const cell = document.createElement('div');
                cell.className = 'game-cell';
                cell.dataset.row = row;
                cell.dataset.col = col;
                
                const value = this.board[row][col];
                let content = '';
                let bgColor = '#f3f4f6';
                
                if (value === CONFIG.GAME.HUMAN) {
                    content = CONFIG.GAME.HUMAN_SYMBOL;
                    bgColor = '#60a5fa'; // Blue for human
                } else if (value === CONFIG.GAME.AI) {
                    content = CONFIG.GAME.AI_SYMBOL;
                    bgColor = '#10b981'; // Green for AI
                }
                
                const isClickable = this.gameActive && this.humanTurn && value === CONFIG.GAME.EMPTY;
                
                cell.style.cssText = `
                    background-color: ${bgColor};
                    border: 2px solid #4b5563;
                    border-radius: 4px;
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    font-size: ${fontSize}px;
                    font-weight: bold;
                    cursor: ${isClickable ? 'pointer' : 'default'};
                    color: #1f2937;
                    user-select: none;
                    transition: all 0.2s ease;
                    padding: 0;
                    pointer-events: auto;
                `;
                
                cell.textContent = content;
                cell.style.minWidth = '20px';
                cell.style.minHeight = '20px';
                
                // Attach click handler
                cell.addEventListener('click', async (e) => {
                    e.stopPropagation();
                    if (this.gameActive && this.humanTurn && value === CONFIG.GAME.EMPTY) {
                        await this.makeMove(row, col, CONFIG.GAME.HUMAN);
                    }
                });
                
                // Hover effects
                cell.addEventListener('mouseenter', () => {
                    if (isClickable) {
                        cell.style.transform = 'scale(0.95)';
                        cell.style.boxShadow = '0 0 12px rgba(96, 165, 250, 0.6)';
                        cell.style.backgroundColor = '#3b82f6';
                    }
                });
                
                cell.addEventListener('mouseleave', () => {
                    cell.style.transform = 'scale(1)';
                    cell.style.boxShadow = 'none';
                    cell.style.backgroundColor = bgColor;
                });
                
                boardElement.appendChild(cell);
                cellCount++;
            }
        }
        
        // Verify complete board was rendered
        const expectedCells = this.boardWidth * this.boardHeight;
        if (cellCount !== expectedCells) {
            console.error(`❌ ERROR: Expected ${expectedCells} cells but created ${cellCount}`);
        } else {
            console.log(`✅ renderBoard() completed: ${cellCount} cells (${this.boardWidth}x${this.boardHeight})`);
        }
    }

    updateStatus(message) {
        document.getElementById('status-text').textContent = message;
    }
}

// Initialize game visualizer
let gameViz = null;

function initGame() {
    if (!gameViz) {
        try {
            gameViz = new GameVisualizer();
            console.log('✅ Game visualizer initialized');
        } catch (error) {
            console.error('❌ Failed to initialize game visualizer:', error);
            throw error;
        }
    }
}

// Expose globally for HTML event handlers
window.initGame = initGame;
window.gameViz = gameViz;
