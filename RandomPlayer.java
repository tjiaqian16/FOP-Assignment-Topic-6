import java.util.List;
import java.util.Random;

public class RandomPlayer extends Player {
    private Random random;

    public RandomPlayer() {
        this.random = new Random();
    }

    // Keep this for GUI compatibility (GamePanel calls this)
    public int chooseMove(List<Integer> possibleMoves) {
        if (possibleMoves == null || possibleMoves.isEmpty()) {
            return -1;
        }
        int index = random.nextInt(possibleMoves.size());
        return possibleMoves.get(index);
    }

    // Add this for Console Simulator (GameMain calls this via Player interface)
    @Override
    public int chooseMove(List<Integer> possibleMoves, int[] currentPositions) {
        return chooseMove(possibleMoves);
    }
}