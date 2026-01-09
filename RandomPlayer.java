import java.util.List;
import java.util.Random;

public class RandomPlayer extends Player {
    private Random random;

    public RandomPlayer() {
        this.random = new Random();
    }

    /**
     * Randomly selects one move from the list of possible moves.
     */
    public int chooseMove(List<Integer> possibleMoves) {
        if (possibleMoves == null || possibleMoves.isEmpty()) {
            return -1;
        }
        int index = random.nextInt(possibleMoves.size());
        return possibleMoves.get(index);
    }
}