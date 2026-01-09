import java.io.*;
import java.util.*;

public class GameMain {
    public static void main(String[] args) throws Exception {
        Scanner input = new Scanner(System.in);
        
        // 1. Prompt user for player mode
        int mode = -1;
        while (true) {
            System.out.print("Select Mode (1: Human Player, 2: Random Player, 3: AI Player): ");
            if (input.hasNextInt()) {
                mode = input.nextInt();
                if (mode >= 1 && mode <= 3) {
                    input.nextLine(); 
                    break; 
                }
            } else {
                input.next(); 
            }
            System.out.println("Invalid choice. Please enter 1, 2, or 3.");
        }
        
        // 2. Prompt for player name 
        String playerName;
        if (mode == 1) {
            while (true) {
                System.out.print("Enter the name of the human player (letters, digits and spaces only): ");
                playerName = input.nextLine();
                if (playerName.matches("^[a-zA-Z0-9 ]+$")) {
                    break;
                } else {
                    System.out.println("Invalid name! Please use only alphabets, digits and spaces.");
                }
            }
        } else if (mode == 2) {
            playerName = "RandomPlayer";
        } else {
            playerName = "AIPlayer";
        }

        // 3. Prompt for level
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

        // 4. Create Player Object (Polymorphism)
        // This fulfills Requirement 8 explicitly
        Player player;
        if (mode == 1) {
            player = new HumanPlayer();
        } else if (mode == 2) {
            player = new RandomPlayer();
        } else {
            player = new AIPlayer(game.targetPiece);
        }

        System.out.println("\n========================================");
        System.out.println("GAME STARTING: Target Piece is " + game.targetPiece);
        System.out.println("Goal: Reach Square 0 in 30 moves.");
        System.out.println("========================================\n");

        PrintWriter writer = new PrintWriter(new FileWriter("moves.txt"));
        loader.printGameDetails(playerName, writer);

        boolean won = false;
        boolean targetCaptured = false;
        int maxMoves = 30;

        // Game loop
        for (int turn = 0; turn < maxMoves && turn < loader.diceSequence.size(); turn++) {
            int remainingMoves = maxMoves - turn;
            System.out.println("--- Turn " + (turn + 1) + " | Moves Remaining: " + remainingMoves + " ---");

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

            // 5. Call chooseMove via Player interface
            // This works for Human, Random, and AI uniformly
            int chosenMove = player.chooseMove(moves, currentPositions);

            if (mode != 1) {
                System.out.println((mode == 2 ? "Random Player" : "AI") + " chose Piece " + (chosenMove / 100) + " to Square " + (chosenMove % 100));
                System.out.println();
            }

            // Execute move
            int pieceToMove = chosenMove / 100;
            int destination = chosenMove % 100;

            for (int i = 0; i < 6; i++) {
                if (currentPositions[i] == destination) {
                    currentPositions[i] = -1; 
                }
            }
            currentPositions[pieceToMove - 1] = destination;

            // 6. Log positions using player.printMove()
            // This fulfills Requirement 7 explicitly
            player.printMove(currentPositions, writer);

            if (game.isWinning(currentPositions)) {
                won = true;
                break;
            }

            if (currentPositions[game.targetPiece - 1] == -1) {
                targetCaptured = true;
                break;
            }
        }
        
        writer.close();

        // Show results
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