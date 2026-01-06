import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.io.File;

public class GameGUI extends JFrame {
    // UI Components
    private JPanel boardPanel;
    private JLabel statusLabel;
    private JLabel infoLabel;
    private JButton nextTurnButton;
    private JButton restartButton; 
    private JButton[] gridButtons; // Represents the 100 squares (10x10)
    
    // Game Data
    private GameState gameState;
    private GameLoader loader;
    private int[] currentPositions;
    private int currentTurn = 0;
    private int targetPiece;

    // Constants
    private static final int BOARD_SIZE = 10;
    private static final Color EMPTY_COLOR = Color.LIGHT_GRAY;
    private static final Color TARGET_COLOR = new Color(255, 100, 100); // Red-ish
    private static final Color PIECE_COLOR = new Color(100, 200, 255); // Blue-ish

    public GameGUI() {
        // 1. Setup the Window
        setTitle("Einstein Würfelt Nicht - GUI");
        setSize(800, 900);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

                // 2. Control Panel (Top) with Level Selector
        JPanel controlPanel = new JPanel(new FlowLayout());
        
        // Dropdown to select level
        String[] levels = {"Level 1", "Level 2", "Level 3", "Level 4"};
        JComboBox<String> levelSelector = new JComboBox<>(levels);
        
        JButton startBtn = new JButton("Start Game");
        startBtn.addActionListener(e -> {
            // Index 0 is Level 1, Index 1 is Level 2, etc.
            int selectedLevel = levelSelector.getSelectedIndex() + 1;
            startLevel(selectedLevel);
        });
        
        nextTurnButton = new JButton("Next Move (AI)");
        nextTurnButton.setEnabled(false);
        nextTurnButton.addActionListener(e -> playNextTurn());

        restartButton = new JButton("Restart");
        restartButton.setEnabled(false);
        restartButton.addActionListener(e -> {
            int selectedLevel = levelSelector.getSelectedIndex() + 1;
            startLevel(selectedLevel);
        });

        controlPanel.add(new JLabel("Select Level:"));
        controlPanel.add(levelSelector);
        controlPanel.add(startBtn);
        controlPanel.add(nextTurnButton);
        controlPanel.add(restartButton);
        add(controlPanel, BorderLayout.NORTH);

        // 3. Info Panel (Bottom)
        infoLabel = new JLabel("Game Status: Waiting...");
        infoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        infoLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(infoLabel, BorderLayout.SOUTH);

        // 4. Board Grid (Center)
        boardPanel = new JPanel(new GridLayout(BOARD_SIZE, BOARD_SIZE, 2, 2)); 
        gridButtons = new JButton[BOARD_SIZE * BOARD_SIZE]; 

        for (int i = 0; i < gridButtons.length; i++) {
            JButton btn = new JButton();
            btn.setBackground(EMPTY_COLOR);
            btn.setOpaque(true);
            btn.setBorderPainted(false);
            // Add coordinate tooltip (e.g., "Row: 2, Col: 3")
            int r = i / BOARD_SIZE;
            int c = i % BOARD_SIZE;
            btn.setToolTipText("(" + r + "," + c + ")");
            
            gridButtons[i] = btn;
            boardPanel.add(btn);
        }
        add(boardPanel, BorderLayout.CENTER);

        setVisible(true);
    }

    private void startLevel(int levelNum) {
        try {
            // Check if file exists
            String filename = "level" + levelNum + ".txt";
            File f = new File(filename);
            if (!f.exists()) {
                JOptionPane.showMessageDialog(this, "File " + filename + " not found!");
                return;
            }

            // Initialize Game
            loader = new GameLoader(filename);
            gameState = new GameState();
            gameState.targetPiece = loader.targetPiece;
            this.targetPiece = loader.targetPiece;
            
            // Clone initial positions so we don't modify the loader's copy
            currentPositions = loader.initialPositions.clone();
            currentTurn = 0;

            // UI Updates
            statusLabel.setText("Level " + levelNum + " Started. Target Piece: " + targetPiece);
            nextTurnButton.setEnabled(true);
            restartButton.setEnabled(true); // Enable restart when game is active
            updateBoard();
            
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading level: " + e.getMessage());
        }
    }

    private void playNextTurn() {
        if (currentTurn >= 30 || currentTurn >= loader.diceSequence.size()) {
            statusLabel.setText("Game Over! (Turn limit reached)");
            nextTurnButton.setEnabled(false);
            return;
        }

        int dice = loader.diceSequence.get(currentTurn);
        
        // --- LOGIC FROM GAMEMAIN.JAVA ---
        List<Integer> moves = gameState.generatePossibleMoves(dice, currentPositions);
        
        if (moves.isEmpty()) {
            infoLabel.setText("Turn " + (currentTurn + 1) + " | Dice: " + dice + " | No moves possible.");
        } else {
            // Using AI Player logic to automate the move for the GUI
            AIPlayer ai = new AIPlayer(gameState.targetPiece);
            int chosenMove = ai.chooseMove(moves, currentPositions);

            int pieceToMove = chosenMove / 100;
            int destination = chosenMove % 100;

            // Capture logic
            String captureMsg = "";
            for (int i = 0; i < 6; i++) {
                if (currentPositions[i] == destination) {
                    currentPositions[i] = -1; // Captured
                    captureMsg = " (Captured Piece " + (i + 1) + ")";
                }
            }
            currentPositions[pieceToMove - 1] = destination;
            
            infoLabel.setText("Turn " + (currentTurn + 1) + " | Dice: " + dice + " | Moved P" + pieceToMove + " to " + destination + captureMsg);
        }
        // --------------------------------

        updateBoard();

        // Check Win
        if (gameState.isWinning(currentPositions)) {
            statusLabel.setText("WINNER! Target Piece " + targetPiece + " reached 0!");
            nextTurnButton.setEnabled(false);
            JOptionPane.showMessageDialog(this, "Game Over! You Win!");
        }

        currentTurn++;
    }

    private void updateBoard() {
        // 1. Clear Board
        for (JButton btn : gridButtons) {
            btn.setText("");
            btn.setBackground(EMPTY_COLOR);
        }

        // 2. Draw Pieces
        for (int i = 0; i < currentPositions.length; i++) {
            int pos = currentPositions[i];
            
            // If piece is not captured (-1)
            if (pos != -1) {
                int row = pos / 10;
                int col = pos % 10;
                
                // Convert (row, col) to 1D array index (0-99)
                int index = row * BOARD_SIZE + col;

                if (index >= 0 && index < gridButtons.length) {
                    int pieceNum = i + 1;
                    gridButtons[index].setText("P" + pieceNum);
                    
                    if (pieceNum == targetPiece) {
                        gridButtons[index].setBackground(TARGET_COLOR);
                    } else {
                        gridButtons[index].setBackground(PIECE_COLOR);
                    }
                }
            }
        }
    }

    public static void main(String[] args) {
        // Run on Event Dispatch Thread
        SwingUtilities.invokeLater(GameGUI::new);
    }
}