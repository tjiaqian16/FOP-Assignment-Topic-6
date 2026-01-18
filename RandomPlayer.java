import java.util.List;
import java.util.Random;

public class RandomPlayer extends Player {
    private Random random;

    public RandomPlayer() {
        this.random = new Random();
    }

    public int chooseMove(List<Integer> possibleMoves) {
        if (possibleMoves == null || possibleMoves.isEmpty()) {
            return -1;
        }
        int index = random.nextInt(possibleMoves.size());
        return possibleMoves.get(index);
    }

    @Override
    public int chooseMove(List<Integer> possibleMoves, int[] currentPositions) {
        return chooseMove(possibleMoves);
    }
}