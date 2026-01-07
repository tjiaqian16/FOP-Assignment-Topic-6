import java.io.*;
import java.util.*;

public class GameMain {
    public static void main(String[] args) throws Exception {
        Scanner input = new Scanner(System.in);
        
        // 1. Prompt user for player mode with validation
        int mode = -1;
        while (true) {
            System.out.print("Select Mode (1: Human Player, 2: Random Player, 3: AI Player): ");
            if (input.hasNextInt()) {
                mode = input.nextInt();
                if (mode >= 1 && mode <= 3) {
                    break; 
                }
            } else {
                input.next(); // Clear invalid non-integer input
            }
            System.out.println("Invalid choice. Please enter 1, 2, or 3.");
        }
        
        // 2. Prompt for player name based on mode with alphanumeric validation
        String playerName;
        if (mode == 1) {
            while (true) {
                System.out.print("Enter the name of the human player (letters and digits only): ");
                playerName = input.next();
                // Regex ensures only alphanumeric characters are used
                if (playerName.matches("^[a-zA-Z0-9]+$")) {
                    break;
                } else {
                    System.out.println("Invalid name! Please use only alphabets and digits.");
                }
            }
        } else if (mode == 2) {
            playerName = "RandomPlayer";
        } else {
            playerName = "AIPlayer";
        }

        // 3. Prompt user for level number with validation
        int levelNum = -1;
        while (true) {
            System.out.print("Enter the level number (1, 2, 3, or 4): ");
            if (input.hasNextInt()) {
                levelNum = input.nextInt();
                if (levelNum >= 1 && levelNum <= 4) {
                    break; 
                }
            } else {
                input.next(); 
            }
            System.out.println("Invalid level. Please enter a number between 1 and 4.");
        }
        
        String levelFile = "level" + levelNum + ".txt";

        // Load game data
        GameLoader loader = new GameLoader(levelFile);
        GameState game = new GameState();
        game.targetPiece = loader.targetPiece;
        int[] currentPositions = loader.initialPositions.clone();

        // Start Announcement
        System.out.println("\n========================================");
        System.out.println("GAME STARTING: Target Piece is " + game.targetPiece);
        System.out.println("Goal: Reach Square 0 in 30 moves.");
        System.out.println("========================================\n");

        // Initialize output file
        PrintWriter writer = new PrintWriter(new FileWriter("moves.txt"));
        loader.printGameDetails(playerName, writer);

        boolean won = false;
        boolean targetCaptured = false;
        int maxMoves = 30;

        // Game loop: Max 30 moves
        for (int turn = 0; turn < maxMoves && turn < loader.diceSequence.size(); turn++) {
            int remainingMoves = maxMoves - turn;
            System.out.println("--- Turn " + (turn + 1) + " | Moves Remaining: " + remainingMoves + " ---");

            // Pre-turn check: is target still on board?
            if (currentPositions[game.targetPiece - 1] == -1) {
                targetCaptured = true;
                break;
            }

            int dice = loader.diceSequence.get(turn);
            List<Integer> moves = game.generatePossibleMoves(dice, currentPositions);
            
            if (moves.isEmpty()) {
                System.out.println("No possible moves available!");
                break;
            }

            int chosenMove = -1;

            // 4. Obtain moves from selected player
            if (mode == 1) {
                HumanPlayer hp = new HumanPlayer();
                chosenMove = hp.chooseMove(moves);
            } else if (mode == 2) {
                RandomPlayer rp = new RandomPlayer();
                chosenMove = rp.chooseMove(moves);
                System.out.println("Random Player chose Piece " + (chosenMove / 100) + " to Square " + (chosenMove % 100));
                System.out.println();
            } else if (mode == 3) {
                AIPlayer ai = new AIPlayer(game.targetPiece);
                chosenMove = ai.chooseMove(moves, currentPositions);
                System.out.println("AI chose Piece " + (chosenMove / 100) + " to Square " + (chosenMove % 100));
                System.out.println();
            }

            // 5. Execute move logic
            int pieceToMove = chosenMove / 100;
            int destination = chosenMove % 100;

            // Capture logic
            for (int i = 0; i < 6; i++) {
                if (currentPositions[i] == destination) {
                    currentPositions[i] = -1; 
                }
            }
            currentPositions[pieceToMove - 1] = destination;

            // 6. Log positions to moves.txt
            for (int i = 0; i < 6; i++) {
                writer.print(currentPositions[i] + (i == 5 ? "" : " "));
            }
            writer.println();
            writer.flush();

            // 7. Check winning condition
            if (game.isWinning(currentPositions)) {
                won = true;
                break;
            }

            // 8. Post-move check: was target just captured?
            if (currentPositions[game.targetPiece - 1] == -1) {
                targetCaptured = true;
                break;
            }
        }
        
        writer.close();

        // 9. Show final results
        System.out.println("========================================");
        if (won) {
            System.out.println("CONGRATULATIONS! Puzzle solved successfully.");
        } else if (targetCaptured) {
            System.out.println("FAILED! Target piece " + game.targetPiece + " was captured.");
        } else {
            System.out.println("FAILED! Puzzle not solved within 30 moves.");
        }
        System.out.println("========================================\n");
        
        input.close();
    }
}