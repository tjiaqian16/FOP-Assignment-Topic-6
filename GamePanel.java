import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.io.File;
import java.util.ArrayList;

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

    // Player Info
    private String currentPlayerName;
    private int currentLevel;
    private boolean gameEnded = false;

    // --- PLAYER LOGIC CONNECTORS ---
    private HumanPlayer humanPlayer;   
    private RandomPlayer randomPlayer; 
    private AIPlayer aiPlayer;         
    
    private boolean isHumanTurn = false;

    private static final int BOARD_SIZE = 10;

    public GamePanel(MainInterface app) {
        this.mainApp = app;
        
        // Initialize Player Helpers
        this.humanPlayer = new HumanPlayer();
        this.randomPlayer = new RandomPlayer(); 
        
        setLayout(new BorderLayout(10, 10));

        // --- Top Control Panel ---
        JPanel controlPanel = new JPanel(new BorderLayout());
        
        quitButton = new JButton("Quit");
        quitButton.addActionListener(e -> {
            SoundManager.getInstance().playSound("click.wav");
            mainApp.showView("HOME");
        });
        
        JPanel centerControls = new JPanel(new FlowLayout(FlowLayout.CENTER));
        statusLabel = new JLabel("Waiting...");
        statusLabel.setFont(new Font("Arial", Font.BOLD, 14));
        
        nextTurnButton = new JButton("Next Turn");
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
            btn.setEnabled(false);
            
            int finalIndex = i;
            btn.addActionListener(e -> handleGridClick(finalIndex)); 
            
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
            
            this.currentPlayerName = playerName;
            this.currentLevel = levelNum;
            this.gameEnded = false;
            this.isHumanTurn = false;

            statusLabel.setText(playerName + " | Level " + levelNum + " | Target: " + targetPiece);
            infoLabel.setText("Click 'Next Turn' to start!");
            nextTurnButton.setEnabled(true);
            updateBoard();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void playNextTurn() {
        if (gameEnded) return;
        SoundManager.getInstance().playSound("click.wav");

        // Check Turn Limit
        if (currentTurn >= 30 || currentTurn >= loader.diceSequence.size()) {
            endGame(false, "Game Over! Turn limit reached.");
            return;
        }

        int dice = loader.diceSequence.get(currentTurn);
        List<Integer> moves = gameState.generatePossibleMoves(dice, currentPositions);

        if (moves.isEmpty()) {
            infoLabel.setText("Turn " + (currentTurn + 1) + " | Dice: " + dice + " | No moves possible.");
            currentTurn++;
            updateBoard();
            return;
        }

        int mode = mainApp.getGameMode();

        // --- MODE 1: HUMAN PLAYER ---
        if (mode == 1) { 
            startHumanTurn(dice, moves);
            return; 
        } 
        
        // --- MODE 2 & 3: COMPUTER (AI/RANDOM) ---
        int chosenMove = -1;
        String playerName = (mode == 2) ? "Random Player" : "AI";

        if (mode == 2) {
            chosenMove = randomPlayer.chooseMove(moves);
        } else {
            aiPlayer = new AIPlayer(gameState.targetPiece);
            chosenMove = aiPlayer.chooseMove(moves, currentPositions);
        }

        // --- VISUALIZATION STEP (SHOW THE MOVE) ---
        if (chosenMove != -1) {
            int pieceId = chosenMove / 100;
            int destination = chosenMove % 100;
            int currentPos = currentPositions[pieceId - 1];

            // 1. Highlight the Piece and Destination BEFORE moving
            highlightComputerMove(currentPos, destination);
            infoLabel.setText(playerName + " (Dice " + dice + ") is moving P" + pieceId + "...");
            
            // Disable button so user can't click while animation plays
            nextTurnButton.setEnabled(false); 

            // 2. Create a Timer to delay the actual move 
            // CHANGED: Delay increased to 2500ms (2.5 seconds)
            Timer animationTimer = new Timer(2500, e -> {
                executeMove(pieceId, destination);
                finishTurn();
            });
            animationTimer.setRepeats(false); // Run only once
            animationTimer.start();
        }
    }

    private void highlightComputerMove(int start, int end) {
        // Reset board first to clear old colors
        updateBoard(); 
        
        // Highlight Start (Orange)
        if (start >= 0 && start < gridButtons.length) {
            gridButtons[start].setBackground(new Color(255, 165, 0)); // Orange
        }
        
        // Highlight Destination (Green)
        if (end >= 0 && end < gridButtons.length) {
            gridButtons[end].setBackground(new Color(50, 205, 50)); // Lime Green
        }
    }

    // --- HUMAN PLAYER LOGIC ---
    private void startHumanTurn(int dice, List<Integer> moves) {
        this.isHumanTurn = true;
        humanPlayer.setCurrentMoves(moves);
        
        infoLabel.setText("Your Turn! Dice: " + dice + ". Select a highlighted Piece.");
        nextTurnButton.setEnabled(false);
        highlightValidPieces();
    }

    private void highlightValidPieces() {
        updateBoard();
        for (int i = 0; i < currentPositions.length; i++) {
            int pos = currentPositions[i];
            int pieceId = i + 1;
            
            if (pos != -1 && humanPlayer.canPieceMove(pieceId)) {
                gridButtons[pos].setBackground(new Color(255, 255, 100)); // Yellow
                gridButtons[pos].setEnabled(true);
            }
        }
    }

    private void handleGridClick(int index) {
        if (!isHumanTurn) return;

        int clickedPieceId = getPieceAt(index);

        // 1. Select Piece
        if (clickedPieceId != -1 && humanPlayer.selectPiece(clickedPieceId)) {
            highlightValidPieces();
            gridButtons[index].setBackground(new Color(255, 200, 0)); // Orange Selected
            
            // Show Destinations
            for (int r = 0; r < BOARD_SIZE * BOARD_SIZE; r++) {
                if (humanPlayer.isValidDestination(r)) {
                    gridButtons[r].setBackground(new Color(100, 255, 100)); // Green
                    gridButtons[r].setEnabled(true);
                }
            }
            infoLabel.setText("Selected P" + clickedPieceId + ". Click a Green Square.");
            SoundManager.getInstance().playSound("click.wav");
            return;
        }

        // 2. Move Piece
        if (humanPlayer.isValidDestination(index)) {
            executeMove(humanPlayer.getSelectedPiece(), index);
            isHumanTurn = false;
            finishTurn();
        }
    }

    // --- UTILITIES ---
    private int getPieceAt(int index) {
        for (int i = 0; i < currentPositions.length; i++) {
            if (currentPositions[i] == index) return i + 1;
        }
        return -1;
    }

    private void executeMove(int pieceToMove, int destination) {
        for (int i = 0; i < 6; i++) {
            if (currentPositions[i] == destination) currentPositions[i] = -1;
        }
        currentPositions[pieceToMove - 1] = destination;
        infoLabel.setText("Moved P" + pieceToMove + " to " + destination);
    }

    private void finishTurn() {
        updateBoard();
        
        // Re-enable the button (it might have been disabled by the animation timer)
        nextTurnButton.setEnabled(true);

        if (gameState.isWinning(currentPositions)) {
            endGame(true, "You Win!");
            return;
        }
        if (currentPositions[targetPiece - 1] == -1) {
            endGame(false, "Game Over! Target Captured.");
            return;
        }
        currentTurn++;
        
        // Update label to show next turn is ready
        if (mainApp.getGameMode() == 1) {
             nextTurnButton.setText("Roll Dice");
        } else {
             nextTurnButton.setText("Next AI Turn");
        }
    }

    private void endGame(boolean won, String message) {
        gameEnded = true;
        infoLabel.setText(message);
        JOptionPane.showMessageDialog(this, message);
        nextTurnButton.setEnabled(false);
        mainApp.recordGameResult(currentPlayerName, currentLevel, won ? "Won" : "Lost");
    }

    private void updateBoard() {
        for (JButton btn : gridButtons) {
            btn.setText("");
            btn.setBackground(Color.LIGHT_GRAY);
            btn.setEnabled(false);
        }
        for (int i = 0; i < currentPositions.length; i++) {
            int pos = currentPositions[i];
            if (pos != -1) {
                int index = (pos / 10) * BOARD_SIZE + (pos % 10);
                if (index >= 0 && index < gridButtons.length) {
                    gridButtons[index].setText("P" + (i + 1));
                    gridButtons[index].setFont(new Font("Arial", Font.BOLD, 16));
                    gridButtons[index].setBackground(i + 1 == targetPiece ? new Color(255, 100, 100) : new Color(100, 200, 255));
                }
            }
        }
    }
}
    
