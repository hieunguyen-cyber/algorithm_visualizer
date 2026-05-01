const EMPTY = 0;
const HUMAN = 1;
const AI = 2;

function checkWin(board, winCondition, player) {
    const height = board.length;
    const width = board[0].length;

    // Check rows and cols
    for (let i = 0; i < height; i++) {
        for (let j = 0; j < width; j++) {
            if (board[i][j] !== player) continue;

            // Check horizontal
            if (j + winCondition <= width) {
                let win = true;
                for (let k = 1; k < winCondition; k++) {
                    if (board[i][j + k] !== player) { win = false; break; }
                }
                if (win) return true;
            }
            
            // Check vertical
            if (i + winCondition <= height) {
                let win = true;
                for (let k = 1; k < winCondition; k++) {
                    if (board[i + k][j] !== player) { win = false; break; }
                }
                if (win) return true;
            }

            // Check diagonal (down-right)
            if (i + winCondition <= height && j + winCondition <= width) {
                let win = true;
                for (let k = 1; k < winCondition; k++) {
                    if (board[i + k][j + k] !== player) { win = false; break; }
                }
                if (win) return true;
            }

            // Check diagonal (down-left)
            if (i + winCondition <= height && j - winCondition >= -1) {
                let win = true;
                for (let k = 1; k < winCondition; k++) {
                    if (board[i + k][j - k] !== player) { win = false; break; }
                }
                if (win) return true;
            }
        }
    }
    return false;
}

function isFull(board) {
    for (let r of board) {
        for (let c of r) {
            if (c === EMPTY) return false;
        }
    }
    return true;
}

function evaluate(board, winCondition) {
    if (checkWin(board, winCondition, AI)) return 1000;
    if (checkWin(board, winCondition, HUMAN)) return -1000;
    return 0; // Simple heuristic for now, deep heuristic can be added
}

function getAvailableMoves(board) {
    const moves = [];
    const height = board.length;
    const width = board[0].length;
    // Optimization: Only check cells near existing pieces if board is large
    let hasPieces = false;
    for (let i = 0; i < height; i++) {
        for (let j = 0; j < width; j++) {
            if (board[i][j] !== EMPTY) hasPieces = true;
        }
    }

    if (!hasPieces) {
        moves.push({r: Math.floor(height/2), c: Math.floor(width/2)});
        return moves;
    }

    for (let i = 0; i < height; i++) {
        for (let j = 0; j < width; j++) {
            if (board[i][j] === EMPTY) {
                moves.push({r: i, c: j});
            }
        }
    }
    return moves;
}

let nodesExplored = 0;
let prunedNodes = 0;

function minimax(board, depth, alpha, beta, isMaximizing, winCondition) {
    nodesExplored++;
    const score = evaluate(board, winCondition);
    
    if (score === 1000) return score - depth; // Prefer faster wins
    if (score === -1000) return score + depth; // Prefer slower losses
    if (isFull(board)) return 0;
    if (depth >= 4) return score; // Hard depth limit for serverless timeout safety

    const moves = getAvailableMoves(board);
    
    if (isMaximizing) {
        let best = -Infinity;
        for (const move of moves) {
            board[move.r][move.c] = AI;
            best = Math.max(best, minimax(board, depth + 1, alpha, beta, false, winCondition));
            board[move.r][move.c] = EMPTY;
            alpha = Math.max(alpha, best);
            if (beta <= alpha) {
                prunedNodes++;
                break; // Alpha Beta Pruning
            }
        }
        return best;
    } else {
        let best = Infinity;
        for (const move of moves) {
            board[move.r][move.c] = HUMAN;
            best = Math.min(best, minimax(board, depth + 1, alpha, beta, true, winCondition));
            board[move.r][move.c] = EMPTY;
            beta = Math.min(beta, best);
            if (beta <= alpha) {
                prunedNodes++;
                break;
            }
        }
        return best;
    }
}

export function findBestMove(board, winCondition, depthLimit = 4) {
    nodesExplored = 0;
    prunedNodes = 0;
    let bestVal = -Infinity;
    let bestMove = null;
    
    const moves = getAvailableMoves(board);
    
    for (const move of moves) {
        board[move.r][move.c] = AI;
        const moveVal = minimax(board, 0, -Infinity, Infinity, false, winCondition);
        board[move.r][move.c] = EMPTY;
        
        if (moveVal > bestVal) {
            bestMove = move;
            bestVal = moveVal;
        }
    }
    
    if (!bestMove && moves.length > 0) {
        bestMove = moves[0];
    }
    
    return { bestMove, treeNodeCount: nodesExplored, prunedCount: prunedNodes };
}

export function checkGameState(board, winCondition) {
    if (checkWin(board, winCondition, AI)) return { gameOver: true, winner: 'AI' };
    if (checkWin(board, winCondition, HUMAN)) return { gameOver: true, winner: 'Human' };
    if (isFull(board)) return { gameOver: true, winner: 'Draw' };
    return { gameOver: false, winner: null };
}
