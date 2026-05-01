import { checkGameState } from './_logic.js';

export default function handler(req, res) {
    if (req.method !== 'POST') return res.status(405).json({ error: 'Method not allowed' });
    const { board, row, col, player, winCondition } = req.body;
    
    if (board[row][col] === 0) {
        board[row][col] = player;
    }
    
    const state = checkGameState(board, winCondition);
    
    res.status(200).json({ 
        success: true, 
        board, 
        gameOver: state.gameOver, 
        winner: state.winner 
    });
}
