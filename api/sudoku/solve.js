import { isValidSudoku, solveSudoku } from './_logic.js';

export default function handler(req, res) {
    if (req.method !== 'POST') return res.status(405).json({ error: 'Method not allowed' });
    const { board } = req.body;
    
    if (!isValidSudoku(board)) {
        return res.status(200).json({ success: false, steps: [], board });
    }
    
    const result = solveSudoku(board, true);
    res.status(200).json(result);
}
