export function isValidSudoku(board) {
    for (let i = 0; i < 9; i++) {
        let row = new Set();
        let col = new Set();
        let box = new Set();
        for (let j = 0; j < 9; j++) {
            let r = board[i][j];
            let c = board[j][i];
            let b = board[3 * Math.floor(i / 3) + Math.floor(j / 3)][3 * (i % 3) + (j % 3)];
            
            if (r !== 0) { if (row.has(r)) return false; row.add(r); }
            if (c !== 0) { if (col.has(c)) return false; col.add(c); }
            if (b !== 0) { if (box.has(b)) return false; box.add(b); }
        }
    }
    return true;
}

export function isValidMove(board, row, col, val) {
    for (let i = 0; i < 9; i++) {
        if (board[row][i] === val) return false;
        if (board[i][col] === val) return false;
        let boxRow = 3 * Math.floor(row / 3) + Math.floor(i / 3);
        let boxCol = 3 * Math.floor(col / 3) + (i % 3);
        if (board[boxRow][boxCol] === val) return false;
    }
    return true;
}

export function solveSudoku(board, recordSteps = false) {
    let steps = [];
    
    function solve() {
        for (let row = 0; row < 9; row++) {
            for (let col = 0; col < 9; col++) {
                if (board[row][col] === 0) {
                    for (let val = 1; val <= 9; val++) {
                        if (isValidMove(board, row, col, val)) {
                            board[row][col] = val;
                            if (recordSteps) steps.push({ type: 'set', row, col, val });
                            
                            if (solve()) return true;
                            
                            board[row][col] = 0;
                            if (recordSteps) steps.push({ type: 'remove', row, col, val });
                        }
                    }
                    return false;
                }
            }
        }
        return true;
    }
    
    let clone = JSON.parse(JSON.stringify(board));
    let success = solve();
    return { success, steps, board };
}
