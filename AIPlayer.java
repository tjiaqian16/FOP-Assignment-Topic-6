import java.util.*;

public class AIPlayer {
    private int targetPiece;
    private GameLoader loader;
    private static final int GOAL_POS = 0;
    private static final int OBSTACLE_POS = 22;

    public AIPlayer(int targetPiece, GameLoader loader) {
        this.targetPiece = targetPiece;
        this.loader = loader;
    }

    /**
     * A* Search Node representing a game state
     */
    private class SearchNode implements Comparable<SearchNode> {
        int[] positions;
        int turn;
        int gCost; // Cost from start (number of moves)
        int fCost; // gCost + heuristic
        SearchNode parent;
        int move; // The move that led to this state

        public SearchNode(int[] positions, int turn, int gCost, SearchNode parent, int move) {
            this.positions = positions.clone();
            this.turn = turn;
            this.gCost = gCost;
            this.parent = parent;
            this.move = move;
            this.fCost = gCost + heuristic(positions);
        }

        @Override
        public int compareTo(SearchNode other) {
            return Integer.compare(this.fCost, other.fCost);
        }
    }

    /**
     * Heuristic: Chebyshev distance of target piece to goal (0,0)
     */
    private int heuristic(int[] positions) {
        int targetPos = positions[targetPiece - 1];
        if (targetPos == -1) return 1000000; // Target captured = very bad
        if (targetPos == GOAL_POS) return 0; // Goal reached
        
        int row = targetPos / 10;
        int col = targetPos % 10;
        // Chebyshev distance: max of absolute differences
        return Math.max(Math.abs(row), Math.abs(col));
    }

    /**
     * Uses A* search to choose the best move.
     * Looks ahead using the predetermined dice sequence from GameLoader.
     */
    public int chooseMove(List<Integer> possibleMoves, int[] currentPositions, int currentTurn) {
        // Perform A* search to find the optimal path
        PriorityQueue<SearchNode> openSet = new PriorityQueue<>();
        Map<String, Integer> bestCosts = new HashMap<>();
        
        // Start node: current state
        SearchNode startNode = new SearchNode(currentPositions, currentTurn, 0, null, -1);
        openSet.add(startNode);
        bestCosts.put(stateKey(currentPositions, currentTurn), 0);
        
        SearchNode goalNode = null;
        int maxDepth = Math.min(30, loader.diceSequence.size()) - currentTurn; // Don't exceed dice sequence
        
        while (!openSet.isEmpty()) {
            SearchNode current = openSet.poll();
            
            // Check if we've reached the goal
            if (current.positions[targetPiece - 1] == GOAL_POS) {
                goalNode = current;
                break;
            }
            
            // Don't search beyond available dice or reasonable depth
            if (current.turn >= loader.diceSequence.size() || current.gCost >= maxDepth) {
                continue;
            }
            
            String currentKey = stateKey(current.positions, current.turn);
            if (current.gCost > bestCosts.getOrDefault(currentKey, Integer.MAX_VALUE)) {
                continue; // We've found a better path to this state already
            }
            
            // Get the next dice value from the sequence
            int nextDice = loader.diceSequence.get(current.turn);
            
            // Generate all possible successor states
            List<Integer> moves = getSimulatedMoves(nextDice, current.positions);
            
            for (int move : moves) {
                int[] nextPositions = simulateMove(current.positions, move);
                int nextTurn = current.turn + 1;
                int nextGCost = current.gCost + 1;
                
                String nextKey = stateKey(nextPositions, nextTurn);
                
                // Only add if this is a better path to this state
                if (nextGCost < bestCosts.getOrDefault(nextKey, Integer.MAX_VALUE)) {
                    bestCosts.put(nextKey, nextGCost);
                    SearchNode nextNode = new SearchNode(nextPositions, nextTurn, nextGCost, current, move);
                    openSet.add(nextNode);
                }
            }
        }
        
        // If we found a goal, backtrack to find the first move
        if (goalNode != null) {
            SearchNode node = goalNode;
            while (node.parent != null && node.parent.parent != null) {
                node = node.parent;
            }
            return node.move;
        }
        
        // If no path to goal found, use greedy heuristic: pick move that minimizes heuristic
        int bestMove = possibleMoves.get(0);
        int bestHeuristic = Integer.MAX_VALUE;
        
        for (int move : possibleMoves) {
            int[] nextPos = simulateMove(currentPositions, move);
            int h = heuristic(nextPos);
            if (h < bestHeuristic) {
                bestHeuristic = h;
                bestMove = move;
            }
        }
        
        return bestMove;
    }
    
    /**
     * Creates a unique key for a game state (positions + turn)
     */
    private String stateKey(int[] positions, int turn) {
        return Arrays.toString(positions) + ":" + turn;
    }

    /**
     * Replicates the game logic to generate valid moves for a given dice roll.
     * Similar to GameState.generatePossibleMoves.
     */
    private List<Integer> getSimulatedMoves(int diceNumber, int[] positions) {
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
}