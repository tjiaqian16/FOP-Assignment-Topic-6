import java.util.*;

public class AIPlayer extends Player {
    private int targetPiece;
    private static final int GOAL_POS = 0;
    private static final int OBSTACLE_POS = 22;
    
    // Stores the winning move sequence
    private Queue<Integer> winningMoves = new LinkedList<>();
    private List<Integer> diceSequence; 
    private boolean solved = false;
    
    // Heuristic Map: Stores true distance from every square to 0
    private int[] distanceMap; 

    public AIPlayer(int targetPiece) {
        this.targetPiece = targetPiece;
        initDistanceMap(); // Pre-calculate distances for the Heuristic
    }

    public void setDiceSequence(List<Integer> diceSequence) {
        this.diceSequence = diceSequence;
    }

    @Override
    public int chooseMove(List<Integer> possibleMoves, int[] currentPositions) {
        if (!solved && diceSequence != null) {
            System.out.println("[AI] Running A* Solver...");
            long startTime = System.currentTimeMillis();
            solveGameAStar(currentPositions);
            long endTime = System.currentTimeMillis();
            System.out.println("[AI] Solved in " + (endTime - startTime) + "ms");
            solved = true;
        }

        if (!winningMoves.isEmpty()) {
            int bestMove = winningMoves.poll();
            if (possibleMoves.contains(bestMove)) return bestMove;
        }
        return possibleMoves.get(0);
    }

    // --- A* SOLVER ---
    private void solveGameAStar(int[] startPositions) {
        // Priority Queue orders states by 'f' score (Cost + Heuristic)
        // This ensures we always explore the most promising path first
        PriorityQueue<Node> openSet = new PriorityQueue<>(Comparator.comparingInt(n -> n.f));
        Set<String> closedSet = new HashSet<>();

        Node startNode = new Node(startPositions, 0, null, -1, 0);
        startNode.f = startNode.g + calculateHeuristic(startPositions);
        openSet.add(startNode);

        while (!openSet.isEmpty()) {
            Node current = openSet.poll();

            // CHECK VICTORY
            if (current.positions[targetPiece - 1] == GOAL_POS) {
                reconstructPath(current);
                return;
            }

            // Stop if search goes too deep (failsafe)
            if (current.turnIndex >= diceSequence.size() || current.turnIndex >= 30) continue;

            // Generate unique key for Visited Set
            String stateKey = Arrays.toString(current.positions) + "|" + current.turnIndex;
            if (closedSet.contains(stateKey)) continue;
            closedSet.add(stateKey);

            int dice = diceSequence.get(current.turnIndex);
            List<Integer> moves = generatePossibleMoves(dice, current.positions);

            for (int move : moves) {
                int[] nextPos = simulateMove(current.positions, move);
                
                // g = cost so far (turns taken)
                // h = heuristic (estimated turns remaining)
                int g = current.g + 1;
                int h = calculateHeuristic(nextPos); 
                
                Node neighbor = new Node(nextPos, current.turnIndex + 1, current, move, g);
                neighbor.f = g + h; 

                openSet.add(neighbor);
            }
        }
        System.out.println("[AI] No solution found.");
    }

    // --- HEURISTIC FUNCTION ---
    // Estimates how close we are to winning. 
    // This is the "Brain" that prevents Memory Overflow.
    private int calculateHeuristic(int[] positions) {
        int targetPos = positions[targetPiece - 1];
        if (targetPos == -1) return 9999; // Lost state
        if (targetPos == GOAL_POS) return 0; // Won state
        
        int dist = distanceMap[targetPos];
        if (dist == -1) return 9999; 
        
        // Return BFS distance. This is an "Admissible Heuristic" (never overestimates)
        // guaranteeing the shortest path is found.
        return dist; 
    }

    private void reconstructPath(Node endNode) {
        LinkedList<Integer> path = new LinkedList<>();
        Node curr = endNode;
        while (curr.parent != null) {
            path.addFirst(curr.moveUsed);
            curr = curr.parent;
        }
        winningMoves.addAll(path);
        System.out.println("[AI] Optimal Path Found: " + winningMoves.size() + " moves.");
    }

    // --- DATA STRUCTURES ---
    private class Node {
        int[] positions;
        int turnIndex;
        Node parent;
        int moveUsed;
        int g; // Cost from start
        int f; // Total estimated cost (f = g + h)

        Node(int[] p, int t, Node par, int m, int g) {
            this.positions = p.clone();
            this.turnIndex = t;
            this.parent = par;
            this.moveUsed = m;
            this.g = g;
        }
    }

    // --- HELPER: Pre-calculate Distance Map for Heuristic ---
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
            int r = curr / 10, c = curr % 10;
            for (int i = 0; i < 8; i++) {
                int nr = r + dr[i], nc = c + dc[i];
                if (nr >= 0 && nr < 10 && nc >= 0 && nc < 10) {
                    int nPos = nr * 10 + nc;
                    if (nPos != OBSTACLE_POS && distanceMap[nPos] == -1) {
                        distanceMap[nPos] = distanceMap[curr] + 1;
                        queue.add(nPos);
                    }
                }
            }
        }
    }

    private int[] simulateMove(int[] positions, int move) {
        int[] newPos = positions.clone();
        int pieceIdx = (move / 100) - 1;
        int dest = move % 100;
        for (int i = 0; i < 6; i++) if (newPos[i] == dest) newPos[i] = -1;
        newPos[pieceIdx] = dest;
        return newPos;
    }

    private List<Integer> generatePossibleMoves(int diceNumber, int[] positions) {
        List<Integer> possiblePieces = new ArrayList<>();
        int pieceIdx = diceNumber - 1;
        if (positions[pieceIdx] != -1) possiblePieces.add(diceNumber);
        else {
            for (int i = pieceIdx - 1; i >= 0; i--) if (positions[i] != -1) { possiblePieces.add(i + 1); break; }
            for (int i = pieceIdx + 1; i < 6; i++) if (positions[i] != -1) { possiblePieces.add(i + 1); break; }
        }
        List<Integer> moves = new ArrayList<>();
        int[] dr = {-1, -1, -1, 0, 0, 1, 1, 1};
        int[] dc = {-1, 0, 1, -1, 1, -1, 0, 1};
        for (int pNum : possiblePieces) {
            int pos = positions[pNum - 1];
            int r = pos / 10, c = pos % 10;
            for (int i = 0; i < 8; i++) {
                int nr = r + dr[i], nc = c + dc[i];
                if (nr >= 0 && nr < 10 && nc >= 0 && nc < 10) {
                    int nPos = nr * 10 + nc;
                    if (nPos != OBSTACLE_POS) moves.add(pNum * 100 + nPos);
                }
            }
        }
        return moves;
    }
}