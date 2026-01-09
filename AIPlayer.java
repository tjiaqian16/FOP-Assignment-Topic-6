import java.util.*;

public class AIPlayer extends Player {
    private int targetPiece;
    private int[] distanceMap; 
    private static final int GOAL_POS = 0;
    private static final int OBSTACLE_POS = 22;
    private static final int SEARCH_DEPTH = 7; 

    public AIPlayer(int targetPiece) {
        this.targetPiece = targetPiece;
        initDistanceMap();
    }

    private void initDistanceMap() {
        distanceMap = new int[100];
        Arrays.fill(distanceMap, -1);
        Queue<Integer> queue = new LinkedList<>();
        
        distanceMap[GOAL_POS] = 0;
        queue.add(GOAL_POS);
        
        int[] dr = {-1, -1, -1, 0, 0, 1, 1, 1};
        int[] dc = {-1, 0, 1, -1, 1, -1, 0, 1};

        while (!queue.isEmpty()) {
            int curr = queue.poll();
            int r = curr / 10;
            int c = curr % 10;
            int dist = distanceMap[curr];

            for (int i = 0; i < 8; i++) {
                int nr = r + dr[i];
                int nc = c + dc[i];
                if (nr >= 0 && nr < 10 && nc >= 0 && nc < 10) {
                    int nextPos = nr * 10 + nc;
                    if (nextPos != OBSTACLE_POS && distanceMap[nextPos] == -1) {
                        distanceMap[nextPos] = dist + 1;
                        queue.add(nextPos);
                    }
                }
            }
        }
    }

    @Override
    public int chooseMove(List<Integer> possibleMoves, int[] currentPositions) {
        int bestMove = -1;
        double maxVal = Double.NEGATIVE_INFINITY;

        for (int move : possibleMoves) {
            int[] nextPos = simulateMove(currentPositions, move);
            double val = expectNode(nextPos, SEARCH_DEPTH);
            if (val > maxVal) {
                maxVal = val;
                bestMove = move;
            }
        }
        return bestMove;
    }

    private double expectNode(int[] positions, int depth) {
        if (depth == 0 || isGameOver(positions)) {
            return evaluate(positions);
        }
        double sum = 0;
        for (int dice = 1; dice <= 6; dice++) {
            sum += maxNode(positions, dice, depth - 1);
        }
        return sum / 6.0;
    }

    private double maxNode(int[] positions, int dice, int depth) {
        if (depth == 0 || isGameOver(positions)) {
            return evaluate(positions);
        }
        List<Integer> moves = generatePossibleMoves(dice, positions);
        if (moves.isEmpty()) {
            return evaluate(positions);
        }
        double maxVal = Double.NEGATIVE_INFINITY;
        for (int move : moves) {
            int[] nextPos = simulateMove(positions, move);
            double val = expectNode(nextPos, depth - 1);
            if (val > maxVal) {
                maxVal = val;
            }
        }
        return maxVal;
    }

    private List<Integer> generatePossibleMoves(int diceNumber, int[] positions) {
        List<Integer> possiblePieces = new ArrayList<>();
        int pieceIdx = diceNumber - 1;

        if (positions[pieceIdx] != -1) {
            possiblePieces.add(diceNumber);
        } else {
            for (int i = pieceIdx - 1; i >= 0; i--) {
                if (positions[i] != -1) { possiblePieces.add(i + 1); break; }
            }
            for (int i = pieceIdx + 1; i < 6; i++) {
                if (positions[i] != -1) { possiblePieces.add(i + 1); break; }
            }
        }

        List<Integer> moves = new ArrayList<>();
        int[] dr = {-1, -1, -1, 0, 0, 1, 1, 1};
        int[] dc = {-1, 0, 1, -1, 1, -1, 0, 1};

        for (int pNum : possiblePieces) {
            int pos = positions[pNum - 1];
            int r = pos / 10;
            int c = pos % 10;

            for (int i = 0; i < 8; i++) {
                int nr = r + dr[i];
                int nc = c + dc[i];
                if (nr >= 0 && nr < 10 && nc >= 0 && nc < 10) {
                    int nPos = nr * 10 + nc;
                    if (nPos != OBSTACLE_POS) {
                        moves.add(pNum * 100 + nPos);
                    }
                }
            }
        }
        return moves;
    }

    private int[] simulateMove(int[] positions, int move) {
        int[] newPos = positions.clone();
        int pieceIdx = (move / 100) - 1;
        int dest = move % 100;

        for (int i = 0; i < 6; i++) {
            if (newPos[i] == dest) {
                newPos[i] = -1; 
            }
        }
        newPos[pieceIdx] = dest;
        return newPos;
    }

    private boolean isGameOver(int[] positions) {
        return positions[targetPiece - 1] == GOAL_POS || positions[targetPiece - 1] == -1;
    }

    private double evaluate(int[] positions) {
        int targetIdx = targetPiece - 1;
        int targetPos = positions[targetIdx];

        if (targetPos == GOAL_POS) return 100000.0;
        if (targetPos == -1) return -100000.0;

        int dist = distanceMap[targetPos];
        if (dist == -1) return -90000.0;

        return -dist * 100.0; 
    }
}