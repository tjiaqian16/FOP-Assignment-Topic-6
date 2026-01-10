import javax.swing.*;
import java.io.*;
import java.util.*;

public class GameMain {
    public static void main(String[] args) throws Exception {
        // 1. Prompt user to choose the mode
        Object[] modeOptions = {"Human Player", "Random Player", "AI Player"};
        int modeChoice = JOptionPane.showOptionDialog(
                null,
                "Select Game Mode:",
                "Game Setup",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                modeOptions,
                modeOptions[0]
        );

        // Default to 1 (Human) if closed
        int mode = (modeChoice == -1) ? 1 : modeChoice + 1;

        // 2. Prompt for Player Name
        String playerName;
        if (mode == 1) {
            while (true) {
                playerName = JOptionPane.showInputDialog(null, "Enter Human Player Name:", "Player Setup", JOptionPane.QUESTION_MESSAGE);
                
                if (playerName == null || playerName.trim().isEmpty()) {
                    playerName = "Player"; // Default fallback
                    break;
                }
                
                if (playerName.matches("^[a-zA-Z0-9 ]+$")) {
                    break;
                } else {
                    JOptionPane.showMessageDialog(null, "Invalid name! Alphanumeric only.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else if (mode == 2) {
            playerName = "RandomPlayer";
        } else {
            playerName = "AIPlayer";
        }

        // 3. Prompt to Select Level
        Object[] levelOptions = {1, 2, 3, 4};
        int levelChoice = JOptionPane.showOptionDialog(
                null,
                "Select Level:",
                "Level Setup",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                levelOptions,
                levelOptions[0]
        );
        int levelNum = (levelChoice == -1) ? 1 : (int) levelOptions[levelChoice];

        // Load Game Data
        String levelFile = "level" + levelNum + ".txt";
        // Ensure GameLoader and level files exist in the project directory
        GameLoader loader = new GameLoader(levelFile);
        GameState game = new GameState();
        game.targetPiece = loader.targetPiece;
        int[] currentPositions = loader.initialPositions.clone();

        // 4. Create Player Object
        Player player;
        if (mode == 1) {
            // Anonymous inner class to override chooseMove with Popup Input
            player = new HumanPlayer() {
                @Override
                public int chooseMove(List<Integer> possibleMoves, int[] currentPositions) {
                    if (possibleMoves.isEmpty()) return -1;

                    String[] options = new String[possibleMoves.size()];
                    for (int i = 0; i < possibleMoves.size(); i++) {
                        int m = possibleMoves.get(i);
                        options[i] = "Piece " + (m / 100) + " -> Square " + (m % 100);
                    }

                    String selected = (String) JOptionPane.showInputDialog(
                            null,
                            "Your Turn! Select a move:",
                            "Make a Move",
                            JOptionPane.PLAIN_MESSAGE,
                            null,
                            options,
                            options[0]
                    );

                    if (selected == null) return possibleMoves.get(0); // Default if cancelled

                    for (int i = 0; i < options.length; i++) {
                        if (options[i].equals(selected)) return possibleMoves.get(i);
                    }
                    return possibleMoves.get(0);
                }
            };
        } else if (mode == 2) {
            player = new RandomPlayer();
        } else {
            player = new AIPlayer(game.targetPiece);
        }

        PrintWriter writer = new PrintWriter(new FileWriter("moves.txt"));
        loader.printGameDetails(playerName, writer);

        boolean won = false;
        boolean targetCaptured = false;
        int maxMoves = 30;

        // --- Game Loop ---
        for (int turn = 0; turn < maxMoves && turn < loader.diceSequence.size(); turn++) {
            if (currentPositions[game.targetPiece - 1] == -1) {
                targetCaptured = true;
                break;
            }

            int dice = loader.diceSequence.get(turn);
            List<Integer> moves = game.generatePossibleMoves(dice, currentPositions);

            if (moves.isEmpty()) continue;

            // 5. Call chooseMove
            int chosenMove = player.chooseMove(moves, currentPositions);

            // Execute Move
            int pieceToMove = chosenMove / 100;
            int destination = chosenMove % 100;
            for (int i = 0; i < 6; i++) {
                if (currentPositions[i] == destination) currentPositions[i] = -1;
            }
            currentPositions[pieceToMove - 1] = destination;

            // 6. Record Move
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

        // 7. Show Result
        String resultMessage;
        int msgType;
        if (won) {
            resultMessage = "CONGRATULATIONS!\nPuzzle solved successfully.";
            msgType = JOptionPane.INFORMATION_MESSAGE;
        } else if (targetCaptured) {
            resultMessage = "FAILED!\nTarget piece " + game.targetPiece + " was captured.";
            msgType = JOptionPane.ERROR_MESSAGE;
        } else {
            resultMessage = "FAILED!\nPuzzle not solved within 30 moves.";
            msgType = JOptionPane.WARNING_MESSAGE;
        }

        JOptionPane.showMessageDialog(null, resultMessage, "Game Over", msgType);
        System.exit(0);
    }
}