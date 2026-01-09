import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.io.File;

public class GamePanel extends JPanel {
    private MainInterface mainApp;
    private JPanel boardPanel;
    private JLabel statusLabel, infoLabel;
    private JButton nextTurnButton, quitButton;
    private JButton[] gridButtons;
    
    // Logic
    private GameState gameState;
    private GameLoader loader;
    private int[] currentPositions;
    private int currentTurn = 0;
    private int targetPiece;

    // Player Info for Leaderboard
    private String currentPlayerName;
    private int currentLevel;
    private boolean gameEnded = false;

    private static final int BOARD_SIZE = 10;

    public GamePanel(MainInterface app) {
        this.mainApp = app;
        setLayout(new BorderLayout(10, 10));

        // --- Top Control Panel ---
        JPanel controlPanel = new JPanel(new BorderLayout());
        
        quitButton = new JButton("Quit");
        quitButton.addActionListener(e -> {
            SoundManager.getInstance().playSound("click.wav");
            mainApp.showView("HOME");
        });
        
        // Center Controls Container
        JPanel centerControls = new JPanel(new FlowLayout(FlowLayout.CENTER));
        
        statusLabel = new JLabel("Waiting...");
        statusLabel.setFont(new Font("Arial", Font.BOLD, 14));
        
        nextTurnButton = new JButton("Next Move");
        nextTurnButton.addActionListener(e -> playNextTurn());

        centerControls.add(statusLabel);
        centerControls.add(nextTurnButton);

        controlPanel.add(quitButton, BorderLayout.WEST);
        controlPanel.add(centerControls, BorderLayout.CENTER);
        
        add(controlPanel, BorderLayout.NORTH);

        // --- Bottom Info Panel ---
        infoLabel = new JLabel("Game Status");
        infoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(infoLabel, BorderLayout.SOUTH);

        // --- Board Grid ---
        boardPanel = new JPanel(new GridLayout(BOARD_SIZE, BOARD_SIZE, 2, 2));
        gridButtons = new JButton[BOARD_SIZE * BOARD_SIZE];

        for (int i = 0; i < gridButtons.length; i++) {
            JButton btn = new JButton();
            btn.setBackground(Color.LIGHT_GRAY);
            btn.setEnabled(false); // Grid is just for display
            gridButtons[i] = btn;
            boardPanel.add(btn);
        }
        add(boardPanel, BorderLayout.CENTER);
    }

    public void startLevel(int levelNum, String playerName) {
        try {
            String filename = "level" + levelNum + ".txt";
            if (!new File(filename).exists()) {
                JOptionPane.showMessageDialog(this, "File " + filename + " not found!");
                return;
            }

            loader = new GameLoader(filename);
            gameState = new GameState();
            gameState.targetPiece = loader.targetPiece;
            this.targetPiece = loader.targetPiece;
            currentPositions = loader.initialPositions.clone();
            currentTurn = 0;
            
            // Store info for leaderboard
            this.currentPlayerName = playerName;
            this.currentLevel = levelNum;
            this.gameEnded = false;

            statusLabel.setText(playerName + " | Level " + levelNum + " | Target: " + targetPiece);
            infoLabel.setText("Game Started. Good Luck!");
            nextTurnButton.setEnabled(true);
            updateBoard();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void playNextTurn() {
        if (gameEnded) return;

        SoundManager.getInstance().playSound("click.wav");

        // Check Turn Limit Loss
        if (currentTurn >= 30 || currentTurn >= loader.diceSequence.size()) {
            endGame(false, "Game Over! Turn limit reached.");
            return;
        }

        int dice = loader.diceSequence.get(currentTurn);
        List<Integer> moves = gameState.generatePossibleMoves(dice, currentPositions);

        if (moves.isEmpty()) {
            infoLabel.setText("Turn " + (currentTurn + 1) + " | Dice: " + dice + " | No moves possible.");
        } else {
            // Using AI logic for move selection (as per your existing code)
            AIPlayer ai = new AIPlayer(gameState.targetPiece);
            int chosenMove = ai.chooseMove(moves, currentPositions);

            int pieceToMove = chosenMove / 100;
            int destination = chosenMove % 100;

            // Execute Move and Capture
            for (int i = 0; i < 6; i++) {
                if (currentPositions[i] == destination) currentPositions[i] = -1; // Capture
            }
            currentPositions[pieceToMove - 1] = destination;
            
            infoLabel.setText("Moved P" + pieceToMove + " to " + destination);
        }

        updateBoard();

        // Check Win Condition
        if (gameState.isWinning(currentPositions)) {
            endGame(true, "You Win!");
            return;
        }

        // Check Loss Condition: Target Captured
        if (currentPositions[targetPiece - 1] == -1) {
            endGame(false, "Game Over! Target Captured.");
            return;
        }

        currentTurn++;
    }

    private void endGame(boolean won, String message) {
        gameEnded = true;
        infoLabel.setText(message);
        JOptionPane.showMessageDialog(this, message);
        nextTurnButton.setEnabled(false);
        
        // Save to Leaderboard
        mainApp.recordGameResult(currentPlayerName, currentLevel, won ? "Won" : "Lost");
    }

    private void updateBoard() {
        for (JButton btn : gridButtons) {
            btn.setText("");
            btn.setBackground(Color.LIGHT_GRAY);
        }
        for (int i = 0; i < currentPositions.length; i++) {
            int pos = currentPositions[i];
            if (pos != -1) {
                int index = (pos / 10) * BOARD_SIZE + (pos % 10);
                if (index >= 0 && index < gridButtons.length) {
                    gridButtons[index].setText("P" + (i + 1));
                    gridButtons[index].setBackground(i + 1 == targetPiece ? new Color(255, 100, 100) : new Color(100, 200, 255));
                }
            }
        }
    }
}

  
    
