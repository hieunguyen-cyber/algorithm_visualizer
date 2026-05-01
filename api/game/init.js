export default function handler(req, res) {
    if (req.method !== 'POST') return res.status(405).json({ error: 'Method not allowed' });
    const { boardWidth, boardHeight, winCondition } = req.body;
    // Just return success, as board state is held by frontend
    res.status(200).json({ success: true, message: 'Game initialized' });
}
