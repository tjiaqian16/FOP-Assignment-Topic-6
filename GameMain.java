import java.io.*;
import java.util.*;

public class GameMain {
    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);

        // 1. Choice Validation (1 to 3)
        int mode = 0;
        System.out.println("--- Einstein Würfelt Nicht ---");
        System.out.println("Select Game Mode:");
        System.out.println("1: Human Player 2: Random Player 3: AI Player");
        while (true) {
            System.out.print("Your choice (1-3): ");
            if (scanner.hasNextInt()) {
                mode = scanner.nextInt();
                if (mode >= 1 && mode <= 3) 
                    break;
            } else {
                scanner.next(); 
            }
            System.out.println("Invalid choice! Please type 1, 2, or 3.");
        }
        scanner.nextLine(); 

        // 2. Name Entry
        String playerName = "Player";
        if (mode == 1) {
            while (true) { 
                System.out.print("Enter Human Player Name: ");
                playerName = scanner.nextLine().trim();
                
                if (playerName.isEmpty()) {
                    System.out.println("Error: Name cannot be empty.");
                } else if (!playerName.matches("^[a-zA-Z0-9 ]+$")) {
                    System.out.println("Error: Use only letters, digits, and spaces.");
                } else {
                    break; 
                }
            }
        } else {
            playerName = (mode == 2) ? "RandomPlayer" : "AIPlayer";
        }

        // 3. Level Validation (1 to 4)
        int levelNum = 0;
        while (true) {
            System.out.print("Select Level (1-4): ");
            if (scanner.hasNextInt()) {
                levelNum = scanner.nextInt();
                if (levelNum >= 1 && levelNum <= 4) 
                    break;
            } else {
                scanner.next(); 
            }
            System.out.println("Error: You must choose between 1 until 4!");
        }

        // 4. Initialization & Inform Target Piece
        GameLoader loader = new GameLoader("level" + levelNum + ".txt");
        System.out.println("\n--- GAME START ---");
        System.out.println("Target Piece: P" + loader.targetPiece); 
        System.out.println("Goal: Move P" + loader.targetPiece + " to Square 0");

        GameState game = new GameState();
        game.targetPiece = loader.targetPiece;
        int[] currentPositions = loader.initialPositions.clone();

        // Inside Step 4: Create Player Object
        Player player;
        if (mode == 1) {
            player = new HumanPlayer(); // Or your Anonymous Inner Class for Console
        } else if (mode == 2) {
            player = new RandomPlayer();
        } else {
            AIPlayer ai = new AIPlayer(game.targetPiece);
            ai.setDiceSequence(loader.diceSequence); // CRITICAL
            player = ai;
        }

        PrintWriter writer = new PrintWriter(new FileWriter("moves.txt"));
        loader.printGameDetails(playerName, writer);

        // --- Game Loop ---
        boolean won = false;
        boolean targetCaptured = false;
        int maxMoves = 30;

        for (int turn = 0; turn < maxMoves && turn < loader.diceSequence.size(); turn++) {
            int movesLeft = maxMoves - turn; 
            int dice = loader.diceSequence.get(turn);
            
            System.out.println("\n------------------------------");
            System.out.println("Remaining Moves: " + movesLeft);
            System.out.println("Current Turn: " + (turn + 1) + " | Dice Rolled: " + dice);

            List<Integer> moves = game.generatePossibleMoves(dice, currentPositions);

            if (moves.isEmpty()) {
                System.out.println("No moves possible. Skipping turn.");
                continue;
            }

            int chosenMove = player.chooseMove(moves, currentPositions);
            
            int pieceToMove = chosenMove / 100;
            int destination = chosenMove % 100;
            System.out.println(playerName + " moves Piece " + pieceToMove + " to Square " + destination);

            // Execute Logic
            for (int i = 0; i < 6; i++) {
                if (currentPositions[i] == destination) 
                    currentPositions[i] = -1;
            }
            currentPositions[pieceToMove - 1] = destination;

            player.printMove(currentPositions, writer);

            if (game.isWinning(currentPositions)) { 
                won = true; 
                break; }
            if (currentPositions[game.targetPiece - 1] == -1) { 
                targetCaptured = true; 
                break; }
        }
        
        writer.close();
        
        // Final Result Display
        if (won) 
            System.out.println("\nCONGRATULATIONS! Puzzle solved successfully.");
        else if (targetCaptured) 
            System.out.println("\nFAILED! Target piece P" + game.targetPiece + " was captured.");
        else 
            System.out.println("\nFAILED! Not solved within 30 moves.");
        
        scanner.close();
    }
}