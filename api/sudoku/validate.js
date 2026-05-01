import { isValidSudoku } from './_logic.js';

export default function handler(req, res) {
    if (req.method !== 'POST') return res.status(405).json({ error: 'Method not allowed' });
    const { board } = req.body;
    res.status(200).json({ valid: isValidSudoku(board) });
}
