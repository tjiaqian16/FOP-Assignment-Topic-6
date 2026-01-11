import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;

import javax.imageio.ImageIO;

public class GamePanel extends BackgroundImagePanel {
    private MainInterface mainApp;
    private PrintWriter gameFileWriter;

    // --- Components ---
    private JPanel boardPanel;
    private JLabel statusLabel;
    private JLabel movesLeftLabel; // NEW: Label for the counter
    private JLabel infoLabel;
    private JButton nextTurnButton;
    private JButton settingsButton;
    private JButton[] gridButtons;
    
    // --- Game Logic ---
    private GameState gameState;
    private GameLoader loader;
    private int[] currentPositions;
    private int currentTurn = 0;
    private int targetPiece;

    // --- State Info ---
    private String currentPlayerName;
    private int currentLevel;
    private boolean gameEnded = false;
    private boolean isHumanTurn = false;
    
    // Constant for Max Moves (matches GameMain.java)
    private static final int MAX_GAME_MOVES = 30;

    // --- Players ---
    private HumanPlayer humanPlayer;   
    private RandomPlayer randomPlayer; 
    private AIPlayer aiPlayer; 
    
    private static final int BOARD_SIZE = 10;

    public GamePanel(MainInterface app) {
        super("setup_bg.jpg");
        this.mainApp = app;
        
        this.humanPlayer = new HumanPlayer();
        this.randomPlayer = new RandomPlayer(); 
        
        setLayout(new BorderLayout());

        // ============================
        // 1. TOP HEADER (Status + Moves Left + Settings)
        // ============================
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(15, 25, 0, 25));

        // A. Status Label (Top Left)
        statusLabel = new JLabel("Initializing...");
        statusLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        statusLabel.setForeground(new Color(20, 20, 20)); 
        statusLabel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
        
        // B. Moves Left Label (Top Center) - NEW
        movesLeftLabel = new JLabel("Moves: --");
        movesLeftLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        movesLeftLabel.setForeground(new Color(178, 34, 34)); // Dark Red for visibility
        movesLeftLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // C. Settings Button (Top Right)
        settingsButton = new JButton(); 
        settingsButton.setPreferredSize(new Dimension(60, 60));
        settingsButton.setFocusPainted(false);
        settingsButton.setContentAreaFilled(false);
        settingsButton.setBorderPainted(false);
        settingsButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        settingsButton.setToolTipText("Settings");
        
        try {
            File imgFile = new File("settings_icon.png"); 
            if (imgFile.exists()) {
                ImageIcon icon = new ImageIcon(ImageIO.read(imgFile));
                if (icon.getIconWidth() > 50) {
                    Image scaled = icon.getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH);
                    icon = new ImageIcon(scaled);
                }
                settingsButton.setIcon(icon);
            } else {
                settingsButton.setText("⚙");
                settingsButton.setFont(new Font("SansSerif", Font.PLAIN, 40));
                settingsButton.setForeground(new Color(50, 50, 50));
            }
        } catch (Exception e) {
            settingsButton.setText("⚙");
        }
        settingsButton.addActionListener(e -> showSettingsDialog());

        headerPanel.add(statusLabel, BorderLayout.WEST);
        headerPanel.add(movesLeftLabel, BorderLayout.CENTER); // Added to center
        headerPanel.add(settingsButton, BorderLayout.EAST);
        
        add(headerPanel, BorderLayout.NORTH);

        // ============================
        // 2. CENTER AREA
        // ============================
        JPanel centerWrapper = new JPanel(new GridBagLayout());
        centerWrapper.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();

        // --- Board ---
        JPanel boardContainer = new JPanel(new BorderLayout());
        boardContainer.setBackground(new Color(255, 255, 255, 180)); 
        boardContainer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(139, 69, 19), 3),
                new EmptyBorder(10, 10, 10, 10)
        ));

        boardPanel = new JPanel(new GridLayout(BOARD_SIZE, BOARD_SIZE, 3, 3));
        boardPanel.setOpaque(false); 
        boardPanel.setPreferredSize(new Dimension(500, 500)); 

        gridButtons = new JButton[BOARD_SIZE * BOARD_SIZE];
        for (int i = 0; i < gridButtons.length; i++) {
            JButton btn = new JButton();
            btn.setFocusPainted(false);
            btn.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
            btn.setBackground(new Color(245, 245, 245));
            btn.setFont(new Font("SansSerif", Font.BOLD, 14));
            btn.setEnabled(false); 
            
            int finalIndex = i;
            btn.addActionListener(e -> handleGridClick(finalIndex)); 
            
            gridButtons[i] = btn;
            boardPanel.add(btn);
        }
        boardContainer.add(boardPanel, BorderLayout.CENTER);

        gbc.gridx = 0; 
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 10, 20); 
        centerWrapper.add(boardContainer, gbc);

        // --- Next Turn Button ---
        nextTurnButton = new RoundedButton("Start Game");
        nextTurnButton.setPreferredSize(new Dimension(180, 80)); 
        nextTurnButton.addActionListener(e -> playNextTurn());

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER; 
        gbc.insets = new Insets(0, 0, 0, 0);
        centerWrapper.add(nextTurnButton, gbc);

        // --- Info Label ---
        infoLabel = new JLabel("Welcome! Press Next Turn to start.");
        infoLabel.setFont(new Font("SansSerif", Font.BOLD | Font.ITALIC, 18));
        infoLabel.setForeground(new Color(20, 20, 20));
        infoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        infoLabel.setOpaque(true);
        infoLabel.setBackground(new Color(255, 255, 255, 150));
        infoLabel.setBorder(new EmptyBorder(5, 15, 5, 15));

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1; 
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(5, 0, 0, 20); 
        centerWrapper.add(infoLabel, gbc);

        add(centerWrapper, BorderLayout.CENTER);
    }

    // ============================
    // LOGIC METHODS
    // ============================

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
            
            this.aiPlayer = new AIPlayer(this.targetPiece); 
            this.aiPlayer.setDiceSequence(loader.diceSequence);
            
            this.currentPlayerName = playerName;
            this.currentLevel = levelNum;
            this.gameEnded = false;
            this.isHumanTurn = false;

            statusLabel.setText(playerName + " - Level " + levelNum);
            infoLabel.setText("Goal: Move P" + targetPiece + " to Square 0!");
            
            // --- UPDATE MOVES LEFT LABEL ---
            updateMovesLeftLabel();
            
            nextTurnButton.setText("Next Turn");
            nextTurnButton.setEnabled(true);
            
            updateBoard();

            gameFileWriter = new PrintWriter(new FileWriter("moves.txt"));
            loader.printGameDetails(playerName, gameFileWriter);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateMovesLeftLabel() {
        if (loader == null) return;
    
        // Use the constant MAX_GAME_MOVES (30) as the base
        int left = MAX_GAME_MOVES - currentTurn;
    
        // Ensure the counter doesn't go below zero
        if (left < 0) left = 0;
        movesLeftLabel.setText("Moves Left: " + left);
        
        // Visual cue for low moves
        if (left <= 5) {
            movesLeftLabel.setForeground(Color.RED);
        } else {
            movesLeftLabel.setForeground(new Color(178, 34, 34));
        }
    }

    private void playNextTurn() {
        if (gameEnded) return;
        SoundManager.getInstance().playSound("click.wav");

        // Check if moves are exhausted
        if (currentTurn >= MAX_GAME_MOVES || currentTurn >= loader.diceSequence.size()) {
            endGame(false, "FAILED! Puzzle not solved within 30 moves.");
            return;
        }

        int dice = loader.diceSequence.get(currentTurn);
        List<Integer> moves = gameState.generatePossibleMoves(dice, currentPositions);

        if (moves.isEmpty()) {
            infoLabel.setText("Turn " + (currentTurn + 1) + " (Dice " + dice + "): No moves possible.");
            currentTurn++;
            updateMovesLeftLabel(); // Update counter even if skipped
            
            if (currentTurn >= MAX_GAME_MOVES) {
                endGame(false, "FAILED! Puzzle not solved within 30 moves.");
                return;
            }
            
            updateBoard();
            return;
        }

        int mode = mainApp.getGameMode();

        if (mode == 1) { 
            startHumanTurn(dice, moves);
            return; 
        } 
        
        String playerName = (mode == 2) ? "Random Player" : "AI";
        
        int chosenMove = (mode == 2) 
            ? randomPlayer.chooseMove(moves) 
            : aiPlayer.chooseMove(moves, currentPositions);

        if (chosenMove != -1) {
            int pieceId = chosenMove / 100;
            int destination = chosenMove % 100;
            int currentPos = currentPositions[pieceId - 1];

            highlightComputerMove(currentPos, destination);
            infoLabel.setText(playerName + " (Dice " + dice + ") moves P" + pieceId + "...");
            nextTurnButton.setEnabled(false); 

            Timer animationTimer = new Timer(1500, e -> {
                executeMove(pieceId, destination);
                finishTurn();
            });
            animationTimer.setRepeats(false);
            animationTimer.start();
        }
    }

    private void highlightComputerMove(int start, int end) {
        updateBoard(); 
        if (start >= 0 && start < gridButtons.length) {
            gridButtons[start].setBackground(new Color(255, 170, 50)); 
        }
        if (end >= 0 && end < gridButtons.length) {
            gridButtons[end].setBackground(new Color(50, 205, 50)); 
        }
    }

    private void startHumanTurn(int dice, List<Integer> moves) {
        this.isHumanTurn = true;
        humanPlayer.setCurrentMoves(moves);
        
        infoLabel.setText("Your Turn! Dice: " + dice + ". Click a highlighted Piece.");
        nextTurnButton.setEnabled(false);
        highlightValidPieces();
    }

    private void highlightValidPieces() {
        updateBoard(); 
        
        for (int i = 0; i < currentPositions.length; i++) {
            int pos = currentPositions[i];
            int pieceId = i + 1;
            
            if (pos != -1 && humanPlayer.canPieceMove(pieceId)) {
                gridButtons[pos].setEnabled(true); 
                gridButtons[pos].setBackground(new Color(255, 235, 59)); 
                gridButtons[pos].setBorder(BorderFactory.createLineBorder(Color.ORANGE, 2));
            }
        }
    }

    private void handleGridClick(int index) {
        if (!isHumanTurn) return;

        // --- 1. PRIORITY: CHECK FOR MOVE / CAPTURE FIRST ---
        // We check if the clicked square is a valid destination for the CURRENTLY selected piece.
        // If it is, we execute the move immediately. This allows capturing P6 with P3 (or vice versa).
        if (humanPlayer.getSelectedPiece() != -1 && humanPlayer.isValidDestination(index)) {
            executeMove(humanPlayer.getSelectedPiece(), index);
            isHumanTurn = false;
            finishTurn();
            return; // Stop here! Do not try to select the piece we just captured/moved to.
        }

        // --- 2. SECONDARY: CHECK FOR SELECTION ---
        // We only reach this code if the click was NOT a valid move.
        // This handles selecting a new piece or changing selection.
        int clickedPieceId = getPieceAt(index);

        if (clickedPieceId != -1 && humanPlayer.selectPiece(clickedPieceId)) {
            highlightValidPieces(); // Reset board highlights (Yellow)
            
            // Highlight the specifically selected piece (Orange)
            gridButtons[index].setBackground(new Color(255, 140, 0)); 
            
            // Highlight valid destinations for this new selection (Green)
            // We iterate through all squares to find valid moves
            for (int r = 0; r < BOARD_SIZE * BOARD_SIZE; r++) { 
                if (humanPlayer.isValidDestination(r)) {
                    gridButtons[r].setEnabled(true); 
                    gridButtons[r].setBackground(new Color(144, 238, 144)); // Light Green
                    gridButtons[r].setBorder(BorderFactory.createLineBorder(new Color(34, 139, 34), 2));
                }
            }
            
            infoLabel.setText("Selected P" + clickedPieceId + ". Now click a Green Square.");
            SoundManager.getInstance().playSound("click.wav");
            return;
        }
    }

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

        if (gameFileWriter != null) {
            recordMoveToFile();
        }
    }

    private void recordMoveToFile() {
        try {
            if (gameFileWriter != null) {
                for (int i = 0; i < currentPositions.length; i++) {
                    gameFileWriter.print(currentPositions[i]);
                    if (i < currentPositions.length - 1) {
                        gameFileWriter.print(" ");
                    }
                }
                gameFileWriter.println();
                gameFileWriter.flush(); 
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void finishTurn() {
        updateBoard();
        nextTurnButton.setEnabled(true);

        currentTurn++; 
        updateMovesLeftLabel();

        if (gameState.isWinning(currentPositions)) {
            endGame(true, "CONGRATULATIONS! Puzzle solved successfully.");
            return;
        }

        if (currentPositions[targetPiece - 1] == -1) {
            endGame(false, "FAILED! Target piece " + targetPiece + " was captured.");
            return;
        }

        if (mainApp.getGameMode() == 1) {
             nextTurnButton.setText("Roll Dice");
        } else {
             nextTurnButton.setText("Next AI Turn");
        }
    }

    private void endGame(boolean won, String message) {
        gameEnded = true;
        nextTurnButton.setEnabled(false);
        
        if (gameFileWriter != null) {
            gameFileWriter.close();
        }

        // Update label to show final state (likely 0 or whatever)
        updateMovesLeftLabel();

        // Calculate Remaining Moves for display
        int maxMoves = Math.min(MAX_GAME_MOVES, loader.diceSequence.size());
        int movesLeft = Math.max(0, maxMoves - currentTurn); // Note: currentTurn is incremented at end of turn

        mainApp.recordGameResult(currentPlayerName, currentLevel, won ? "Won" : "Lost");
        showGameOverDialog(won, message, movesLeft);
    }

    private void showGameOverDialog(boolean won, String message, int movesLeft) {
        JDialog dialog = new JDialog(mainApp, "Game Over", true);
        dialog.setUndecorated(true);
        dialog.setSize(450, 400); 
        dialog.setLocationRelativeTo(this);
        
        JPanel content = new JPanel(new GridBagLayout());
        content.setBackground(new Color(255, 248, 220)); 
        content.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(139, 69, 19), 5), 
            new EmptyBorder(25, 25, 25, 25)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; 
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 10, 0);

        JLabel titleLabel = new JLabel(won ? "VICTORY!" : "DEFEAT");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 42));
        titleLabel.setForeground(won ? new Color(34, 139, 34) : new Color(178, 34, 34)); 
        content.add(titleLabel, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(0, 10, 10, 10);
        
        JLabel msgLabel = new JLabel("<html><div style='text-align: center; width: 320px; font-size: 14px; color: #333333;'>" + message + "</div></html>");
        msgLabel.setHorizontalAlignment(SwingConstants.CENTER);
        content.add(msgLabel, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(0, 10, 25, 10);
        
        JLabel movesLabel = new JLabel("Moves Remaining: " + movesLeft);
        movesLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        movesLabel.setForeground(new Color(100, 100, 100)); 
        content.add(movesLabel, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(10, 0, 0, 0);
        
        JPanel btnPanel = new JPanel(new GridLayout(1, 2, 25, 0)); 
        btnPanel.setOpaque(false);

        RoundedButton playAgainBtn = new RoundedButton("Play Again");
        playAgainBtn.setBackground(new Color(46, 204, 113)); 
        playAgainBtn.setPreferredSize(new Dimension(150, 60));
        playAgainBtn.addActionListener(e -> {
            dialog.dispose();
            SoundManager.getInstance().playSound("click.wav");
            startLevel(currentLevel, currentPlayerName); 
        });

        RoundedButton quitBtn = new RoundedButton("Exit to Home");
        quitBtn.setBackground(new Color(231, 76, 60)); 
        quitBtn.setPreferredSize(new Dimension(150, 60));
        quitBtn.addActionListener(e -> {
            dialog.dispose();
            SoundManager.getInstance().playSound("click.wav");
            mainApp.showView("HOME");
        });

        btnPanel.add(playAgainBtn);
        btnPanel.add(quitBtn);
        
        content.add(btnPanel, gbc);

        dialog.add(content);
        dialog.setVisible(true);
    }

    private void updateBoard() {
        for (JButton btn : gridButtons) {
            btn.setText("");
            btn.setBackground(new Color(245, 245, 245));
            btn.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
            btn.setEnabled(false);
        }
        
        gridButtons[0].setBorder(BorderFactory.createLineBorder(new Color(255, 215, 0), 2));
        
        for (int i = 0; i < currentPositions.length; i++) {
            int pos = currentPositions[i];
            if (pos != -1) {
                int index = (pos / 10) * BOARD_SIZE + (pos % 10);
                if (index >= 0 && index < gridButtons.length) {
                    JButton btn = gridButtons[index];
                    btn.setText("P" + (i + 1));
                    btn.setFont(new Font("SansSerif", Font.BOLD, 16));
                    
                    if (i + 1 == targetPiece) {
                        btn.setBackground(new Color(255, 100, 100)); 
                        btn.setForeground(Color.WHITE);
                    } else {
                        btn.setBackground(new Color(70, 130, 180)); 
                        btn.setForeground(Color.WHITE);
                    }
                }
            }
        }
    }

    private void showSettingsDialog() {
        SoundManager.getInstance().playSound("click.wav");

        JDialog dialog = new JDialog(mainApp, "Settings", true);
        dialog.setUndecorated(true); 
        dialog.setSize(300, 320);
        dialog.setLocationRelativeTo(this);
        
        JPanel content = new JPanel(new GridLayout(5, 1, 10, 10));
        content.setBackground(new Color(250, 250, 250));
        content.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(50, 50, 50), 2),
            new EmptyBorder(20, 30, 20, 30)
        ));

        JLabel title = new JLabel("SETTINGS");
        title.setFont(new Font("SansSerif", Font.BOLD, 22));
        title.setHorizontalAlignment(SwingConstants.CENTER);
        content.add(title);

        JPanel musicPanel = new JPanel(new BorderLayout());
        musicPanel.setOpaque(false);
        JLabel musicLabel = new JLabel("Background Music");
        musicLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        ToggleSwitch musicSwitch = new ToggleSwitch(SoundManager.getInstance().isMusicEnabled());
        musicSwitch.addSwitchListener(isOn -> SoundManager.getInstance().setMusicEnabled(isOn));
        musicPanel.add(musicLabel, BorderLayout.WEST);
        musicPanel.add(musicSwitch, BorderLayout.EAST);
        content.add(musicPanel);

        JPanel soundPanel = new JPanel(new BorderLayout());
        soundPanel.setOpaque(false);
        JLabel soundLabel = new JLabel("Sound Effects");
        soundLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        ToggleSwitch soundSwitch = new ToggleSwitch(SoundManager.getInstance().isSoundEnabled());
        soundSwitch.addSwitchListener(isOn -> SoundManager.getInstance().setSoundEnabled(isOn));

        soundPanel.add(soundLabel, BorderLayout.WEST);
        soundPanel.add(soundSwitch, BorderLayout.EAST);
        content.add(soundPanel);

        JButton resumeBtn = new RoundedButton("Resume Game");
        resumeBtn.setBackground(new Color(46, 204, 113)); 
        resumeBtn.addActionListener(e -> dialog.dispose());
        content.add(resumeBtn);

        JButton homeBtn = new RoundedButton("Quit to Home");
        homeBtn.setBackground(new Color(231, 76, 60)); 
        homeBtn.addActionListener(e -> {
            SoundManager.getInstance().playSound("click.wav");
            dialog.dispose();
            mainApp.showView("HOME");
        });
        content.add(homeBtn);

        dialog.add(content);
        dialog.setVisible(true);
    }

    private static class RoundedButton extends JButton {
        private Color normalColor = new Color(0, 105, 120);
        private Color hoverColor = new Color(0, 140, 160);
        private Color pressedColor = new Color(0, 70, 80);
        
        private boolean isHovered = false;
        private boolean isPressed = false;

        public RoundedButton(String text) {
            super(text);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setForeground(Color.WHITE);
            setFont(new Font("SansSerif", Font.BOLD, 18));
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            
            addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { isHovered = true; repaint(); }
                public void mouseExited(MouseEvent e) { isHovered = false; isPressed = false; repaint(); }
                public void mousePressed(MouseEvent e) { isPressed = true; repaint(); }
                public void mouseReleased(MouseEvent e) { isPressed = false; repaint(); }
            });
        }
        
        @Override
        public void setBackground(Color bg) {
            this.normalColor = bg;
            this.hoverColor = bg.brighter();
            this.pressedColor = bg.darker();
            super.setBackground(bg);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            if (isPressed) {
                g2.setColor(pressedColor);
            } else if (isHovered) {
                g2.setColor(hoverColor);
            } else {
                g2.setColor(normalColor);
            }
            
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
            super.paintComponent(g2);
            g2.dispose();
        }
    }

    private static class ToggleSwitch extends JPanel {
        private boolean isOn;
        private Color switchOnColor = new Color(46, 204, 113); 
        private Color switchOffColor = new Color(189, 195, 199); 
        private Color buttonColor = Color.WHITE;
        private SwitchListener listener;

        public interface SwitchListener {
            void onSwitchChanged(boolean isOn);
        }

        public ToggleSwitch(boolean initialState) {
            this.isOn = initialState;
            setPreferredSize(new Dimension(80, 40));
            setOpaque(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    isOn = !isOn;
                    repaint();
                    if (listener != null) {
                        listener.onSwitchChanged(isOn);
                    }
                }
            });
        }

        public void addSwitchListener(SwitchListener listener) {
            this.listener = listener;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            int pad = 4;

            g2.setColor(isOn ? switchOnColor : switchOffColor);
            g2.fillRoundRect(0, 0, w, h, h, h);

            g2.setColor(buttonColor);
            int circleSize = h - (pad * 2);
            int x = isOn ? (w - circleSize - pad) : pad;
            g2.fillOval(x, pad, circleSize, circleSize);
        }
    }
}