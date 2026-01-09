import java.io.*;
import java.util.*;

public class GameLoader {
    int targetPiece;
    int[] initialPositions = new int[6];
    List<Integer> diceSequence = new ArrayList<>();

    public GameLoader(String filename) throws Exception {
        Scanner sc = new Scanner(new File(filename));
        this.targetPiece = sc.nextInt(); // Line 1: Target 
        for (int i = 0; i < 6; i++) {
            this.initialPositions[i] = sc.nextInt(); // Line 2: Initial Pos 
        }
        while (sc.hasNextInt()) {
            this.diceSequence.add(sc.nextInt()); // Line 3: Dice Sequence 
        }
        sc.close();
    }

    public void printGameDetails(String playerName, PrintWriter out) {
        out.println(playerName); 
        for (int i = 0; i < diceSequence.size(); i++) {
            out.print(diceSequence.get(i) + (i == diceSequence.size() - 1 ? "" : " "));
        }
        out.println(); 
        out.println(targetPiece); 
        for (int i = 0; i < 6; i++) {
            out.print(initialPositions[i] + (i == 5 ? "" : " "));
        }
        out.println(); 
    }
}