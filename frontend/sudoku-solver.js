/**
 * Sudoku Solver Module
 * Displays solver activity step-by-step by mutating a single shared board state.
 */
class SudokuSolver {
    constructor() {
        this.currentBoard = Array(81).fill(0);
        this.solutionBoard = Array(81).fill(0);
        this.userInputs = new Set();

        this.selectedCell = null;
        this.isSolving = false;
        this.steps = [];
        this.stepCount = 0;
        this.currentStep = 0;
        this.elapsedTimeMs = 0;
        this.animationSpeed = 100;
        this.activeCell = null;
        this.activeAction = null;
        this.solved = false;
        this.boardElement = null;

        this.init();
    }

    init() {
        this.boardElement = document.getElementById('sudoku-board');
        this.renderBoard();
        this.setupEventListeners();
    }

    renderBoard() {
        if (!this.boardElement) {
            this.boardElement = document.getElementById('sudoku-board');
            if (!this.boardElement) return;
        }

        this.boardElement.classList.toggle('solved', this.solved);
        this.boardElement.innerHTML = '';

        for (let i = 0; i < 81; i++) {
            const cell = document.createElement('div');
            cell.className = 'sudoku-cell';
            cell.id = `sudoku-cell-${i}`;
            cell.dataset.index = i;

            const value = this.currentBoard[i];
            cell.textContent = value !== 0 ? value : '';

            if (this.userInputs.has(i)) {
                cell.classList.add('user-input');
            }

            if (i === this.selectedCell) {
                cell.classList.add('selected');
            }

            if (i === this.activeCell && this.activeAction) {
                const actionClass = this.getActionClass(this.activeAction);
                if (actionClass) cell.classList.add(actionClass);
            }

            if (this.solved && value !== 0) {
                cell.classList.add('solved');
            }

            cell.addEventListener('click', () => this.selectCell(i));
            this.boardElement.appendChild(cell);
        }
    }

    getActionClass(action) {
        if (action === 'placed' || action === 'trying') return 'solving';
        if (action === 'backtrack') return 'backtrack';
        return null;
    }

    selectCell(index) {
        if (this.isSolving) return;
        this.selectedCell = index;
        this.renderBoard();
    }

    setCellValue(value) {
        if (this.selectedCell === null || this.isSolving) return;
        if (typeof value !== 'number' || value < 1 || value > 9) return;

        this.currentBoard[this.selectedCell] = value;
        this.userInputs.add(this.selectedCell);
        this.renderBoard();
    }

    clearCell() {
        if (this.selectedCell === null || this.isSolving) return;

        this.currentBoard[this.selectedCell] = 0;
        this.userInputs.delete(this.selectedCell);
        this.renderBoard();
    }

    resetBoard() {
        this.currentBoard = Array(81).fill(0);
        this.solutionBoard = Array(81).fill(0);
        this.userInputs.clear();
        this.steps = [];
        this.stepCount = 0;
        this.currentStep = 0;
        this.elapsedTimeMs = 0;
        this.selectedCell = null;
        this.isSolving = false;
        this.activeCell = null;
        this.activeAction = null;
        this.solved = false;

        if (this.boardElement) {
            this.boardElement.classList.remove('solved');
        }

        this.updateStatus('Ready');
        this.updateStats();
        this.renderBoard();
    }

    validateBoard() {
        if (this.currentBoard.every(v => v === 0)) {
            this.updateStatus('❌ Board is empty');
            return false;
        }

        for (let index = 0; index < 81; index++) {
            const value = this.currentBoard[index];
            if (value === 0) continue;

            const row = Math.floor(index / 9);
            const col = index % 9;

            for (let c = 0; c < 9; c++) {
                if (c !== col && this.currentBoard[row * 9 + c] === value) {
                    this.markCellError(index);
                    this.updateStatus(`❌ Duplicate ${value} in row ${row + 1}`);
                    return false;
                }
            }

            for (let r = 0; r < 9; r++) {
                if (r !== row && this.currentBoard[r * 9 + col] === value) {
                    this.markCellError(index);
                    this.updateStatus(`❌ Duplicate ${value} in column ${col + 1}`);
                    return false;
                }
            }

            const boxRow = Math.floor(row / 3) * 3;
            const boxCol = Math.floor(col / 3) * 3;
            for (let r = boxRow; r < boxRow + 3; r++) {
                for (let c = boxCol; c < boxCol + 3; c++) {
                    if ((r * 9 + c) !== index && this.currentBoard[r * 9 + c] === value) {
                        this.markCellError(index);
                        this.updateStatus(`❌ Duplicate ${value} in 3x3 box`);
                        return false;
                    }
                }
            }
        }

        return true;
    }

    markCellError(index) {
        const cell = document.getElementById(`sudoku-cell-${index}`);
        if (!cell) return;
        cell.classList.add('error');
        setTimeout(() => cell.classList.remove('error'), 1200);
    }

    async solvePuzzle() {
        if (!this.validateBoard()) {
            return;
        }

        if (this.isSolving) {
            return;
        }

        this.isSolving = true;
        this.solved = false;
        this.activeCell = null;
        this.activeAction = null;
        this.steps = [];
        this.stepCount = 0;
        this.currentStep = 0;
        this.elapsedTimeMs = 0;

        if (this.boardElement) {
            this.boardElement.classList.remove('solved');
        }

        this.updateStatus('📡 Solving...');
        this.updateStats();

        const board2D = [];
        for (let row = 0; row < 9; row++) {
            board2D.push(this.currentBoard.slice(row * 9, row * 9 + 9));
        }

        try {
            const response = await fetch(CONFIG.API_BASE + '/sudoku/solve-with-steps', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ board: board2D })
            });

            if (!response.ok) {
                throw new Error('Backend error: ' + response.status);
            }

            const result = await response.json();
            if (!result.success) {
                this.updateStatus('❌ ' + (result.error || 'Cannot solve puzzle'));
                return;
            }

            this.steps = result.steps || [];
            this.stepCount = this.steps.length;
            this.elapsedTimeMs = result.elapsedMs || 0;
            this.solutionBoard = Array.isArray(result.board)
                ? result.board.flat().map(val => Number(val) || 0)
                : Array(81).fill(0);

            this.updateStats();
            await this.animateSolving();

            this.updateStatus(`✅ Solved in ${this.elapsedTimeMs}ms (${this.stepCount} steps)`, 'success');
        } catch (error) {
            this.updateStatus('❌ ' + error.message);
        } finally {
            this.isSolving = false;
        }
    }

    async animateSolving() {
        this.currentStep = 0;

        if (!this.steps.length) {
            this.solved = true;
            this.renderBoard();
            this.updateStats();
            return;
        }

        for (const step of this.steps) {
            if (!this.isSolving) break;
            if (step.row < 0 || step.col < 0) continue;

            this.activeCell = step.row * 9 + step.col;
            this.activeAction = step.action;

            this.applyStep(step);

            this.currentStep++;
            this.updateStats();

            await this.sleep(this.animationSpeed);
        }

        this.activeCell = null;
        this.activeAction = null;

        if (this.solutionBoard.length === 81) {
            this.currentBoard = [...this.solutionBoard];
        }

        this.solved = true;
        this.renderBoard();
        this.updateStats();
    }

    applyStep(step) {
        const index = step.row * 9 + step.col;
        if (step.action === 'backtrack') {
            this.currentBoard[index] = 0;
        } else if (step.action === 'placed' && step.value > 0) {
            this.currentBoard[index] = step.value;
        }

        this.renderBoard();
    }

    sleep(ms) {
        return new Promise(resolve => setTimeout(resolve, ms));
    }

    updateStatus(msg, type = 'default') {
        const status = document.getElementById('sudoku-status');
        if (status) {
            status.textContent = msg;
            status.className = 'status ' + type;
        }
    }

    updateStats() {
        const stepsEl = document.getElementById('sudoku-steps');
        const timeEl = document.getElementById('sudoku-time');

        if (stepsEl) {
            const display = this.stepCount > 0 ? `${this.currentStep}/${this.stepCount}` : '0/0';
            stepsEl.textContent = display;
        }

        if (timeEl) {
            timeEl.textContent = `${this.elapsedTimeMs}ms`;
        }
    }

    setupEventListeners() {
        for (let i = 1; i <= 9; i++) {
            const btn = document.getElementById(`sudoku-num-${i}`);
            if (btn) {
                btn.addEventListener('click', () => this.setCellValue(i));
            }
        }

        const deleteBtn = document.getElementById('sudoku-btn-delete');
        if (deleteBtn) {
            deleteBtn.addEventListener('click', () => this.clearCell());
        }

        const solveBtn = document.getElementById('sudoku-btn-solve');
        if (solveBtn) {
            solveBtn.addEventListener('click', () => this.solvePuzzle());
        }

        const resetBtn = document.getElementById('sudoku-btn-reset');
        if (resetBtn) {
            resetBtn.addEventListener('click', () => this.resetBoard());
        }

        const speedSelect = document.getElementById('sudoku-speed');
        if (speedSelect) {
            speedSelect.addEventListener('change', (e) => {
                const speeds = { slow: 500, normal: 100, fast: 10 };
                this.animationSpeed = speeds[e.target.value] || 100;
            });
        }

        document.addEventListener('keydown', (e) => {
            if (this.selectedCell === null || this.isSolving) return;

            if (e.key >= '1' && e.key <= '9') {
                this.setCellValue(parseInt(e.key, 10));
            } else if (e.key === 'Delete' || e.key === 'Backspace') {
                this.clearCell();
            } else if (['ArrowUp', 'ArrowDown', 'ArrowLeft', 'ArrowRight'].includes(e.key)) {
                e.preventDefault();
                const row = Math.floor(this.selectedCell / 9);
                const col = this.selectedCell % 9;
                let newIndex = this.selectedCell;

                if (e.key === 'ArrowUp') newIndex = Math.max(0, this.selectedCell - 9);
                else if (e.key === 'ArrowDown') newIndex = Math.min(80, this.selectedCell + 9);
                else if (e.key === 'ArrowLeft') newIndex = col > 0 ? this.selectedCell - 1 : this.selectedCell;
                else if (e.key === 'ArrowRight') newIndex = col < 8 ? this.selectedCell + 1 : this.selectedCell;

                this.selectCell(newIndex);
            }
        });
    }
}

if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', () => {
        window.sudokuSolver = new SudokuSolver();
    });
} else {
    window.sudokuSolver = new SudokuSolver();
}
