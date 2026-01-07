import java.io.*;
import java.util.*;

public class GameMain {
    public static void main(String[] args) throws Exception {
        Scanner input = new Scanner(System.in);
        
        // 1. Prompt user for player mode [cite: 84]
        System.out.println("Select Mode (1: Human Player, 2: Random Player, 3: AI Player): ");
        int mode = input.nextInt();
        
        // 2. Prompt for player name based on mode [cite: 86]
        String playerName;
        if (mode == 1) {
            System.out.print("Enter the name of the human player: ");
            playerName = input.next();
        } else if (mode == 2) {
            playerName = "Random Player";
        } else {
            playerName = "AI Player";
        }

        // 3. Prompt user for level NUMBER 
        System.out.print("Enter the level number (1, 2, 3, or 4): ");
        int levelNum = input.nextInt();
        String levelFile = "level" + levelNum + ".txt"; // Automatically adds the filename format [cite: 147-151]

        // Load game data [cite: 64, 70]
        GameLoader loader = new GameLoader(levelFile);
        GameState game = new GameState();
        game.targetPiece = loader.targetPiece;
        int[] currentPositions = loader.initialPositions.clone();

        // Initialize output file [cite: 72]
        PrintWriter writer = new PrintWriter(new FileWriter("moves.txt"));
        loader.printGameDetails(playerName, writer);

        // Initialize AI player outside loop if needed
        AIPlayer ai = null;
        if (mode == 3) {
            ai = new AIPlayer(game.targetPiece, loader);
        }

        boolean won = false;
        // Game loop: Max 30 moves [cite: 58, 102]
        for (int turn = 0; turn < 30 && turn < loader.diceSequence.size(); turn++) {
            int dice = loader.diceSequence.get(turn);
            List<Integer> moves = game.generatePossibleMoves(dice, currentPositions);
            
            if (moves.isEmpty()) break;

            int chosenMove = -1;

            // 4. Obtain moves from selected player [cite: 65, 88]
            if (mode == 1) {
                HumanPlayer hp = new HumanPlayer();
                chosenMove = hp.chooseMove(moves);
            } else if (mode == 2) {
                RandomPlayer rp = new RandomPlayer();
                chosenMove = rp.chooseMove(moves);
            } else if (mode == 3) {
                chosenMove = ai.chooseMove(moves, currentPositions, turn);
            }

            // 5. Execute move logic [cite: 53, 54]
            int pieceToMove = chosenMove / 100;
            int destination = chosenMove % 100;

            // Capture logic [cite: 55, 56]
            for (int i = 0; i < 6; i++) {
                if (currentPositions[i] == destination) {
                    currentPositions[i] = -1;
                }
            }
            currentPositions[pieceToMove - 1] = destination;

            // 6. Log positions to moves.txt [cite: 81, 82, 102]
            for (int i = 0; i < 6; i++) {
                writer.print(currentPositions[i] + (i == 5 ? "" : " "));
            }
            writer.println();

            // 7. Check winning condition [cite: 75, 76]
            if (game.isWinning(currentPositions)) {
                won = true;
                break;
            }
        }
        
        writer.close();
        // 8. Show result [cite: 66, 89]
        if (won) {
            System.out.println("Puzzle solved successfully!");
        } else {
            System.out.println("Puzzle not solved.");
        }
    }
}