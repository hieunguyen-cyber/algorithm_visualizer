import { isValidSudoku, solveSudoku } from './_logic.js';

export default function handler(req, res) {
    if (req.method !== 'POST') return res.status(405).json({ error: 'Method not allowed' });
    const { board } = req.body;
    
    if (!isValidSudoku(board)) {
        return res.status(200).json({ solvable: false });
    }
    
    // Test if solvable without recording steps
    const result = solveSudoku(board, false);
    res.status(200).json({ solvable: result.success });
}
