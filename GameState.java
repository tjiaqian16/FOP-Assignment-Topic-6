import java.util.*;

public class GameState {
    int[] piecePositions; 
    int targetPiece;
    int boardSize = 10;

    public List<Integer> generatePossibleMoves(int diceNumber, int[] currentPositions) {
        List<Integer> possiblePieces = new ArrayList<>();
        int pieceIdx = diceNumber - 1;

        if (currentPositions[pieceIdx] != -1) {
            possiblePieces.add(diceNumber);
        } else {
            int smaller = -1, bigger = -1;
            for (int i = pieceIdx - 1; i >= 0; i--) {
                if (currentPositions[i] != -1) { smaller = i + 1; break; }
            }
            for (int i = pieceIdx + 1; i < 6; i++) {
                if (currentPositions[i] != -1) { bigger = i + 1; break; }
            }
            if (smaller != -1) possiblePieces.add(smaller);
            if (bigger != -1) possiblePieces.add(bigger);
        }

        List<Integer> moves = new ArrayList<>();
        for (int pNum : possiblePieces) {
            int pos = currentPositions[pNum - 1];
            int row = pos / 10;
            int col = pos % 10;

            for (int dr = -1; dr <= 1; dr++) {
                for (int dc = -1; dc <= 1; dc++) {
                    if (dr == 0 && dc == 0) continue;
                    int nr = row + dr, nc = col + dc;
                    int nPos = nr * 10 + nc;
                    if (nr >= 0 && nr < 10 && nc >= 0 && nc < 10 && nPos != 22) {
                        moves.add(pNum * 100 + nPos); 
                    }
                }
            }
        }
        return moves;
    }

    public boolean isWinning(int[] currentPositions) {
    return currentPositions[targetPiece - 1] == 0;
}
}