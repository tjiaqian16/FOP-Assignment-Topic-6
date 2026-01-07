import java.util.*;

public class AIPlayer {
    private int targetPiece;
    private int[] distanceMap; // Precomputed BFS distances to goal
    private static final int GOAL_POS = 0;
    private static final int OBSTACLE_POS = 22;
    private static final int SEARCH_DEPTH = 3; // Lookahead depth (Me -> Dice -> Me -> Dice -> Eval)

    public AIPlayer(int targetPiece) {
        this.targetPiece = targetPiece;
        initDistanceMap();
    }

    /**
     * Initializes a lookup table for the shortest path distance from every square to the goal (0),
     * accounting for the obstacle at 22.
     */
    private void initDistanceMap() {
        distanceMap = new int[100];
        Arrays.fill(distanceMap, -1);
        Queue<Integer> queue = new LinkedList<>();
        
        // Start BFS from the goal
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

    /**
     * Uses Expectimax search to choose the best move.
     * Root node: We choose the move with the highest expected value.
     */
    public int chooseMove(List<Integer> possibleMoves, int[] currentPositions) {
        int bestMove = -1;
        double maxVal = Double.NEGATIVE_INFINITY;

        for (int move : possibleMoves) {
            int[] nextPos = simulateMove(currentPositions, move);
            // After our move, it's a "Chance" node (Dice roll)
            double val = expectNode(nextPos, SEARCH_DEPTH);
            if (val > maxVal) {
                maxVal = val;
                bestMove = move;
            }
        }
        return bestMove;
    }

    /**
     * Chance Node: Calculates the average value over all possible dice rolls (1-6).
     */
    private double expectNode(int[] positions, int depth) {
        if (depth == 0 || isGameOver(positions)) {
            return evaluate(positions);
        }

        double sum = 0;
        // Average over all 6 possible dice outcomes
        for (int dice = 1; dice <= 6; dice++) {
            sum += maxNode(positions, dice, depth - 1);
        }
        return sum / 6.0;
    }

    /**
     * Max Node: Simulates the AI choosing the best move for a specific dice roll.
     */
    private double maxNode(int[] positions, int dice, int depth) {
        if (depth == 0 || isGameOver(positions)) {
            return evaluate(positions);
        }

        List<Integer> moves = generatePossibleMoves(dice, positions);
        if (moves.isEmpty()) {
            // No moves possible (pass), just evaluate current state
            return evaluate(positions);
        }

        double maxVal = Double.NEGATIVE_INFINITY;
        for (int move : moves) {
            int[] nextPos = simulateMove(positions, move);
            // After this move, it goes back to a Chance node (next turn's dice)
            // Note: We decrement depth in expectNode, but we can treat (Max->Expect) as one layer
            double val = expectNode(nextPos, depth - 1);
            if (val > maxVal) {
                maxVal = val;
            }
        }
        return maxVal;
    }

    /**
     * Replicates the game logic to generate valid moves for a given dice roll.
     */
    private List<Integer> generatePossibleMoves(int diceNumber, int[] positions) {
        List<Integer> possiblePieces = new ArrayList<>();
        int pieceIdx = diceNumber - 1;

        if (positions[pieceIdx] != -1) {
            possiblePieces.add(diceNumber);
        } else {
            // Find next smallest available
            for (int i = pieceIdx - 1; i >= 0; i--) {
                if (positions[i] != -1) { possiblePieces.add(i + 1); break; }
            }
            // Find next biggest available
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

    /**
     * Applies a move to a state and returns the new state.
     */
    private int[] simulateMove(int[] positions, int move) {
        int[] newPos = positions.clone();
        int pieceIdx = (move / 100) - 1;
        int dest = move % 100;

        // Capture logic: If any piece is at destination, remove it
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

    /**
     * Heuristic Evaluation Function.
     * Higher score = Better state.
     */
    private double evaluate(int[] positions) {
        int targetIdx = targetPiece - 1;
        int targetPos = positions[targetIdx];

        // 1. Win/Loss Condition
        if (targetPos == GOAL_POS) return 100000.0;
        if (targetPos == -1) return -100000.0; // Target captured -> LOSS

        // 2. Distance Heuristic
        // Use precomputed BFS distance (accounts for obstacle 22)
        int dist = distanceMap[targetPos];
        // If dist is -1 (unreachable), massive penalty
        if (dist == -1) return -90000.0;

        double score = -dist * 100.0; 

        // 3. Mobility Bonus
        // Slightly prefer states where we have more pieces alive (more options)
        int pieceCount = 0;
        for (int p : positions) {
            if (p != -1) pieceCount++;
        }
        score += pieceCount * 5.0;

        return score;
    }
}