import { findBestMove, checkGameState } from './_logic.js';

export default function handler(req, res) {
    if (req.method !== 'POST') return res.status(405).json({ error: 'Method not allowed' });
    const { board, algorithm, depth, winCondition } = req.body;
    
    // Check if game already over before AI moves
    let state = checkGameState(board, winCondition);
    if (state.gameOver) {
        return res.status(200).json({ success: true, board, gameOver: state.gameOver, winner: state.winner });
    }

    const { bestMove, treeNodeCount, prunedCount } = findBestMove(board, winCondition, depth);
    
    if (bestMove) {
        board[bestMove.r][bestMove.c] = 2; // AI
    }
    
    state = checkGameState(board, winCondition);
    
    res.status(200).json({ 
        success: true, 
        board, 
        treeNodeCount,
        prunedCount,
        gameOver: state.gameOver, 
        winner: state.winner 
    });
}
