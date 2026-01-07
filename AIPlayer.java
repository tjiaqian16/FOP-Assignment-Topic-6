import java.util.*;

public class AIPlayer {
    private int targetPiece;
    private GameLoader loader;
    private static final int GOAL_POS = 0;
    private static final int OBSTACLE_POS = 22;

    public AIPlayer(GameLoader loader) {
        this.loader = loader;
        this.targetPiece = loader.targetPiece;
    }

    /**
     * A* Node represents a state in the search tree
     */
    private static class AStarNode implements Comparable<AStarNode> {
        int[] positions;
        int turn;
        int gCost; // Actual cost from start (number of moves)
        double hCost; // Heuristic cost to goal
        int moveFromParent; // The move that led to this state
        AStarNode parent;

        AStarNode(int[] positions, int turn, int gCost, double hCost, int moveFromParent, AStarNode parent) {
            this.positions = positions.clone();
            this.turn = turn;
            this.gCost = gCost;
            this.hCost = hCost;
            this.moveFromParent = moveFromParent;
            this.parent = parent;
        }

        double fCost() {
            return gCost + hCost;
        }

        @Override
        public int compareTo(AStarNode other) {
            int fCompare = Double.compare(this.fCost(), other.fCost());
            if (fCompare != 0) return fCompare;
            // Tie-breaking: prefer lower h-cost (closer to goal)
            return Double.compare(this.hCost, other.hCost);
        }

        String stateKey() {
            return Arrays.toString(positions) + ":" + turn;
        }
    }

    /**
     * Uses A* search to choose the best move.
     * Returns the first move in the optimal path to the goal.
     */
    public int chooseMove(List<Integer> possibleMoves, int[] currentPositions, int currentTurn) {
        // Priority queue for A* (ordered by f-cost)
        PriorityQueue<AStarNode> openSet = new PriorityQueue<>();
        Map<String, Integer> gScores = new HashMap<>(); // Best g-score found for each state
        
        // Start with each possible move as initial nodes
        for (int move : possibleMoves) {
            int[] nextPos = simulateMove(currentPositions, move);
            double hCost = calculateHeuristic(nextPos);
            AStarNode node = new AStarNode(nextPos, currentTurn + 1, 1, hCost, move, null);
            openSet.add(node);
            gScores.put(node.stateKey(), 1);
        }
        
        // A* search
        while (!openSet.isEmpty()) {
            AStarNode current = openSet.poll();
            
            String stateKey = current.stateKey();
            
            // Skip if we've found a better path to this state
            if (gScores.containsKey(stateKey) && gScores.get(stateKey) < current.gCost) {
                continue;
            }
            
            // Check if we've reached the goal
            if (isWinningState(current.positions)) {
                // Return the first solution found (A* guarantees optimality)
                return getFirstMove(current);
            }
            
            // Check if we've exceeded the dice sequence
            if (current.turn >= loader.diceSequence.size()) {
                continue;
            }
            
            // Get next dice roll from sequence
            int nextDice = loader.diceSequence.get(current.turn);
            
            // Generate successor states
            List<Integer> nextMoves = getSimulatedMoves(nextDice, current.positions);
            for (int move : nextMoves) {
                int[] nextPos = simulateMove(current.positions, move);
                int newGCost = current.gCost + 1;
                String nextKey = Arrays.toString(nextPos) + ":" + (current.turn + 1);
                
                // Only add if we haven't found a better path to this state
                if (!gScores.containsKey(nextKey) || gScores.get(nextKey) > newGCost) {
                    double hCost = calculateHeuristic(nextPos);
                    AStarNode successor = new AStarNode(
                        nextPos, 
                        current.turn + 1, 
                        newGCost, 
                        hCost, 
                        move, 
                        current
                    );
                    openSet.add(successor);
                    gScores.put(nextKey, newGCost);
                }
            }
        }
        
        // If no winning path found, return the first possible move
        return possibleMoves.isEmpty() ? -1 : possibleMoves.get(0);
    }

    /**
     * Traces back the path to find the first move from the initial state.
     */
    private int getFirstMove(AStarNode node) {
        while (node.parent != null) {
            if (node.parent.parent == null) {
                // This is the first move after the initial state
                return node.moveFromParent;
            }
            node = node.parent;
        }
        return node.moveFromParent;
    }
    
    /**
     * Calculates the Chebyshev distance heuristic for the target piece to reach (0,0).
     * Chebyshev distance = max(|x1 - x2|, |y1 - y2|)
     */
    private double calculateHeuristic(int[] positions) {
        int targetIdx = targetPiece - 1;
        int targetPos = positions[targetIdx];
        
        // If target is captured, it's a losing state
        if (targetPos == -1) {
            return 10000.0;
        }
        
        // If target is at goal, winning state
        if (targetPos == GOAL_POS) {
            return 0.0;
        }
        
        // Calculate Chebyshev distance
        int row = targetPos / 10;
        int col = targetPos % 10;
        int goalRow = GOAL_POS / 10;
        int goalCol = GOAL_POS % 10;
        
        return Math.max(Math.abs(row - goalRow), Math.abs(col - goalCol));
    }
    
    /**
     * Generates all possible moves for a given dice roll and position state.
     * This replicates GameState.generatePossibleMoves logic.
     */
    public List<Integer> getSimulatedMoves(int diceNumber, int[] currentPositions) {
        List<Integer> possiblePieces = new ArrayList<>();
        int pieceIdx = diceNumber - 1;

        // Rule: If the dice piece exists, only it moves
        if (currentPositions[pieceIdx] != -1) {
            possiblePieces.add(diceNumber);
        } else {
            // Rule: Find smallest bigger and biggest smaller
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
        int[] dr = {-1, -1, -1, 0, 0, 1, 1, 1};
        int[] dc = {-1, 0, 1, -1, 1, -1, 0, 1};

        for (int pNum : possiblePieces) {
            int pos = currentPositions[pNum - 1];
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

    private boolean isWinningState(int[] positions) {
        return positions[targetPiece - 1] == GOAL_POS;
    }
}