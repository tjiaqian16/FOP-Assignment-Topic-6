import java.util.*;

public class AIPlayer {
    private int targetPiece;
    private GameLoader loader;
    private static final int GOAL_POS = 0;
    private static final int OBSTACLE_POS = 22;
    private List<Integer> optimalPath = null;
    private int pathIndex = 0;

    public AIPlayer(int targetPiece, GameLoader loader) {
        this.targetPiece = targetPiece;
        this.loader = loader;
    }

    /**
     * A* State representation for search
     */
    private static class State implements Comparable<State> {
        int[] positions;
        int turn;
        int gCost; // Actual cost from start (number of moves)
        double fCost; // gCost + heuristic
        int lastMove;
        State parent;

        State(int[] positions, int turn, int gCost, double hCost, int lastMove, State parent) {
            this.positions = positions.clone(); // Defensive copy
            this.turn = turn;
            this.gCost = gCost;
            this.fCost = gCost + hCost;
            this.lastMove = lastMove;
            this.parent = parent;
        }

        @Override
        public int compareTo(State other) {
            return Double.compare(this.fCost, other.fCost);
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof State)) return false;
            State s = (State) o;
            return this.turn == s.turn && Arrays.equals(this.positions, s.positions);
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(positions) * 31 + turn;
        }
    }

    /**
     * Chooses the best move using A* search algorithm.
     * Uses the predetermined dice sequence from GameLoader for lookahead.
     * Computes the path once and follows it.
     */
    public int chooseMove(List<Integer> possibleMoves, int[] currentPositions, int currentTurn) {
        // If we don't have a path yet, or we've deviated from it, recompute
        if (optimalPath == null || pathIndex >= optimalPath.size()) {
            optimalPath = computeOptimalPath(currentPositions, currentTurn);
            pathIndex = 0;
        }
        
        // If we found a path, follow it
        if (optimalPath != null && pathIndex < optimalPath.size()) {
            int plannedMove = optimalPath.get(pathIndex);
            pathIndex++;
            
            // Verify this move is valid
            if (possibleMoves.contains(plannedMove)) {
                return plannedMove;
            } else {
                // Path is invalid, recompute
                optimalPath = computeOptimalPath(currentPositions, currentTurn);
                pathIndex = 0;
                if (optimalPath != null && !optimalPath.isEmpty()) {
                    int move = optimalPath.get(0);
                    pathIndex = 1;
                    if (possibleMoves.contains(move)) {
                        return move;
                    }
                }
            }
        }
        
        // Fallback: Choose move with best heuristic
        int bestMove = possibleMoves.get(0);
        double bestScore = Double.POSITIVE_INFINITY;
        
        for (int move : possibleMoves) {
            int[] nextPos = simulateMove(currentPositions, move);
            double heuristic = calculateHeuristic(nextPos);
            
            if (heuristic < bestScore) {
                bestScore = heuristic;
                bestMove = move;
            }
        }
        
        return bestMove;
    }
    
    /**
     * Computes the optimal path from current state to goal using A*
     */
    private List<Integer> computeOptimalPath(int[] startPositions, int startTurn) {
        State goalState = aStarSearch(startPositions, startTurn);
        
        if (goalState != null) {
            // Backtrack to build the path
            List<Integer> path = new ArrayList<>();
            State current = goalState;
            while (current.parent != null) {
                path.add(0, current.lastMove);
                current = current.parent;
            }
            return path;
        }
        
        return null;
    }

    /**
     * Performs A* search to find optimal path to goal
     */
    private State aStarSearch(int[] startPositions, int startTurn) {
        PriorityQueue<State> openSet = new PriorityQueue<>();
        Map<String, Double> bestCosts = new HashMap<>();
        
        double initialH = calculateHeuristic(startPositions);
        State initialState = new State(startPositions, startTurn, 0, initialH, -1, null);
        
        openSet.add(initialState);
        bestCosts.put(getStateKey(startPositions, startTurn), 0.0);
        
        int maxIterations = 200000;
        int iterations = 0;
        
        while (!openSet.isEmpty() && iterations < maxIterations) {
            iterations++;
            State current = openSet.poll();
            
            // Check if we've reached the goal
            if (current.positions[targetPiece - 1] == GOAL_POS) {
                return current;
            }
            
            // Skip if we've found a better path to this state
            String currentKey = getStateKey(current.positions, current.turn);
            if (bestCosts.containsKey(currentKey) && bestCosts.get(currentKey) < current.gCost) {
                continue;
            }
            
            // Don't search beyond available dice
            if (current.turn >= loader.diceSequence.size()) {
                continue;
            }
            
            // Expand successors using the next dice from sequence
            int nextDice = loader.diceSequence.get(current.turn);
            List<Integer> nextMoves = getSimulatedMoves(nextDice, current.positions);
            
            for (int move : nextMoves) {
                int[] nextPos = simulateMove(current.positions, move);
                int nextTurn = current.turn + 1;
                int nextG = current.gCost + 1;
                
                String nextKey = getStateKey(nextPos, nextTurn);
                
                // Only add if this is a better path
                if (!bestCosts.containsKey(nextKey) || bestCosts.get(nextKey) > nextG) {
                    double nextH = calculateHeuristic(nextPos);
                    State successor = new State(nextPos, nextTurn, nextG, nextH, move, current);
                    
                    openSet.add(successor);
                    bestCosts.put(nextKey, (double) nextG);
                }
            }
        }
        
        return null; // No path found
    }

    /**
     * Creates a unique key for a state (positions + turn)
     */
    private String getStateKey(int[] positions, int turn) {
        return Arrays.toString(positions) + "_" + turn;
    }

    /**
     * Generates possible moves for a given dice roll and position state.
     * Logic mirrors GameState.generatePossibleMoves.
     */
    private List<Integer> getSimulatedMoves(int diceNumber, int[] positions) {
        List<Integer> possiblePieces = new ArrayList<>();
        int pieceIdx = diceNumber - 1;

        if (positions[pieceIdx] != -1) {
            possiblePieces.add(diceNumber);
        } else {
            // Find next smallest available
            for (int i = pieceIdx - 1; i >= 0; i--) {
                if (positions[i] != -1) { 
                    possiblePieces.add(i + 1); 
                    break; 
                }
            }
            // Find next biggest available
            for (int i = pieceIdx + 1; i < 6; i++) {
                if (positions[i] != -1) { 
                    possiblePieces.add(i + 1); 
                    break; 
                }
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

    /**
     * Calculates Chebyshev distance heuristic for the target piece to goal (0,0).
     * Chebyshev distance = max(|dx|, |dy|)
     */
    private double calculateHeuristic(int[] positions) {
        int targetPos = positions[targetPiece - 1];
        
        // Target captured or at goal
        if (targetPos == -1) return Double.MAX_VALUE;
        if (targetPos == GOAL_POS) return 0;
        
        int targetRow = targetPos / 10;
        int targetCol = targetPos % 10;
        int goalRow = GOAL_POS / 10;
        int goalCol = GOAL_POS % 10;
        
        // Chebyshev distance
        int chebyshev = Math.max(Math.abs(targetRow - goalRow), Math.abs(targetCol - goalCol));
        
        return chebyshev;
    }
}