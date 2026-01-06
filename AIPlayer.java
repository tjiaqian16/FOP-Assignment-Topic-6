import java.util.*;

public class AIPlayer {
    private int targetPiece;

    public AIPlayer(int targetPiece) {
        this.targetPiece = targetPiece; //[cite: 140]
    }

    /**
     * Chooses the best move based on a Greedy Search algorithm.
     * It prioritizes moving the target piece toward square 0.
     */
    public int chooseMove(List<Integer> possibleMoves, int[] currentPositions) { //[cite: 140]
        int bestMove = possibleMoves.get(0);
        double minDistance = Double.MAX_VALUE;

        for (int move : possibleMoves) {
            int pieceNum = move / 100;
            int destination = move % 100;

            // Calculate distance of the move's destination to square 0
            double distance = calculateDistance(destination, 0);

            // HEURISTIC: Prioritize moving the target piece [cite: 144]
            if (pieceNum == targetPiece) {
                // Give a 'bonus' to target piece moves to encourage solving the puzzle
                distance -= 50; 
            }

            if (distance < minDistance) {
                minDistance = distance;
                bestMove = move;
            }
        }
        return bestMove; //[cite: 146]
    }

    private double calculateDistance(int pos1, int pos2) {
        int r1 = pos1 / 10, c1 = pos1 % 10;
        int r2 = pos2 / 10, c2 = pos2 % 10;
        // Euclidean distance formula [cite: 141]
        return Math.sqrt(Math.pow(r1 - r2, 2) + Math.pow(c1 - c2, 2));
    }
}