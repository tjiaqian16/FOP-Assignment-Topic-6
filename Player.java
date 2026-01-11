import java.io.PrintWriter;
import java.util.List;

public abstract class Player {
    public abstract int chooseMove(List<Integer> possibleMoves, int[] currentPositions);

    public void printMove(int[] positions, PrintWriter writer) {
        for (int i = 0; i < positions.length; i++) {
            writer.print(positions[i]);
            if (i < positions.length - 1) {
                writer.print(" ");
            }
        }
        writer.println();
        writer.flush(); 
    }
}