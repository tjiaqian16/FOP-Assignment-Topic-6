import java.io.PrintWriter;

public abstract class Player {
    /**
     * Records the current positions of all pieces into the moves.txt file.
     * This ensures all player types (Human, Random, AI) log data the same way.
     */
    public void recordMove(int[] positions, PrintWriter writer) {
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