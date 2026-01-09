import java.io.PrintWriter;
import java.util.List;

public abstract class Player {
    
    // Abstract method to ensure all players have a common move method
    // We include currentPositions because AI needs it, even if Human/Random don't.
    public abstract int chooseMove(List<Integer> possibleMoves, int[] currentPositions);

    /**
     * Records the current positions of all pieces into the moves.txt file.
     * REQUIREMENT 7: This method must be used to print moves.
     */
    public void printMove(int[] positions, PrintWriter writer) {
        for (int i = 0; i < positions.length; i++) {
            writer.print(positions[i]);
            if (i < positions.length - 1) {
                writer.print(" ");
            }
        }
        writer.println();
        writer.flush(); // Ensures data is actually written to the file
    }
}