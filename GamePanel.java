import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.io.File;
import javax.imageio.ImageIO;

public class GamePanel extends BackgroundImagePanel {
    private MainInterface mainApp;
    
    // --- Components ---
    private JPanel boardPanel;
    private JLabel statusLabel;
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

    // --- Players ---
    private HumanPlayer humanPlayer;   
    private RandomPlayer randomPlayer; 
    private AIPlayer aiPlayer; // Field is now properly used
    
    private static final int BOARD_SIZE = 10;

    public GamePanel(MainInterface app) {
        // Use the desk background for a nice game atmosphere
        super("setup_bg.jpg");
        this.mainApp = app;
        
        // Initialize static players (Human and Random don't depend on level data)
        this.humanPlayer = new HumanPlayer();
        this.randomPlayer = new RandomPlayer(); 
        
        setLayout(new BorderLayout());

        // ============================
        // 1. TOP HEADER (Status + Settings)
        // ============================
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(15, 25, 0, 25));

        // Status Label (Top Left)
        statusLabel = new JLabel("Initializing...");
        statusLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        statusLabel.setForeground(new Color(20, 20, 20)); 
        statusLabel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));

        // Settings Button (Top Right) - Image Only
        settingsButton = new JButton(); // No text initially
        settingsButton.setPreferredSize(new Dimension(60, 60));
        settingsButton.setFocusPainted(false);
        settingsButton.setContentAreaFilled(false);
        settingsButton.setBorderPainted(false);
        settingsButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        settingsButton.setToolTipText("Settings");
        
        // Try to load 'settings_icon.png'
        try {
            File imgFile = new File("settings_icon.png"); 
            if (imgFile.exists()) {
                ImageIcon icon = new ImageIcon(ImageIO.read(imgFile));
                // Scale if too big
                if (icon.getIconWidth() > 50) {
                    Image scaled = icon.getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH);
                    icon = new ImageIcon(scaled);
                }
                settingsButton.setIcon(icon);
            } else {
                // Fallback if image missing: A simple unicode gear
                settingsButton.setText("⚙");
                settingsButton.setFont(new Font("SansSerif", Font.PLAIN, 40));
                settingsButton.setForeground(new Color(50, 50, 50));
            }
        } catch (Exception e) {
            settingsButton.setText("⚙");
        }

        settingsButton.addActionListener(e -> showSettingsDialog());

        headerPanel.add(statusLabel, BorderLayout.WEST);
        headerPanel.add(settingsButton, BorderLayout.EAST);
        
        add(headerPanel, BorderLayout.NORTH);

        // ============================
        // 2. CENTER AREA (Grid + Button + Label)
        // ============================
        JPanel centerWrapper = new JPanel(new GridBagLayout());
        centerWrapper.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();

        // --- A. The Board (Grid) ---
        JPanel boardContainer = new JPanel(new BorderLayout());
        boardContainer.setBackground(new Color(255, 255, 255, 180)); // Milky translucent white
        boardContainer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(139, 69, 19), 3), // Brown border
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
            btn.setEnabled(false); // Default disabled
            
            int finalIndex = i;
            btn.addActionListener(e -> handleGridClick(finalIndex)); 
            
            gridButtons[i] = btn;
            boardPanel.add(btn);
        }
        boardContainer.add(boardPanel, BorderLayout.CENTER);

        // Add Grid to Center Wrapper (Row 0, Col 0)
        gbc.gridx = 0; 
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 10, 20); // Gap right for the button
        centerWrapper.add(boardContainer, gbc);

        // --- B. Next Turn Button (Beside Grid) ---
        nextTurnButton = new RoundedButton("Start Game");
        nextTurnButton.setPreferredSize(new Dimension(140, 80)); // Taller button
        nextTurnButton.addActionListener(e -> playNextTurn());

        // Add Button to Center Wrapper (Row 0, Col 1)
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER; 
        gbc.insets = new Insets(0, 0, 0, 0);
        centerWrapper.add(nextTurnButton, gbc);

        // --- C. Info Label (Below Grid) ---
        infoLabel = new JLabel("Welcome! Press Next Turn to start.");
        infoLabel.setFont(new Font("SansSerif", Font.BOLD | Font.ITALIC, 18));
        infoLabel.setForeground(new Color(20, 20, 20));
        infoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        // Background for readability
        infoLabel.setOpaque(true);
        infoLabel.setBackground(new Color(255, 255, 255, 150));
        infoLabel.setBorder(new EmptyBorder(5, 15, 5, 15));

        // Add Label to Center Wrapper (Row 1, Col 0 - aligning with Grid)
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1; // Match grid width
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(5, 0, 0, 20); // Top gap, Right gap to match grid
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
            
            // Initialize the AIPlayer here since we now know the target piece
            this.aiPlayer = new AIPlayer(this.targetPiece); 
            
            this.currentPlayerName = playerName;
            this.currentLevel = levelNum;
            this.gameEnded = false;
            this.isHumanTurn = false;

            statusLabel.setText(playerName + " - Level " + levelNum);
            infoLabel.setText("Goal: Move P" + targetPiece + " to Square 0!");
            
            nextTurnButton.setText("Next Turn");
            nextTurnButton.setEnabled(true);
            
            updateBoard();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void playNextTurn() {
        if (gameEnded) return;
        SoundManager.getInstance().playSound("click.wav");

        // --- 1. Check Turn Limit First (Updated to match GameMain logic) ---
        if (currentTurn >= 30 || currentTurn >= loader.diceSequence.size()) {
            endGame(false, "FAILED! Puzzle not solved within 30 moves.");
            return;
        }

        int dice = loader.diceSequence.get(currentTurn);
        List<Integer> moves = gameState.generatePossibleMoves(dice, currentPositions);

        if (moves.isEmpty()) {
            infoLabel.setText("Turn " + (currentTurn + 1) + " (Dice " + dice + "): No moves possible.");
            currentTurn++;
            
            // Check if we hit the limit immediately after skipping
            if (currentTurn >= 30) {
                endGame(false, "FAILED! Puzzle not solved within 30 moves.");
                return;
            }
            
            updateBoard();
            return;
        }

        int mode = mainApp.getGameMode();

        // 2. HUMAN TURN
        if (mode == 1) { 
            startHumanTurn(dice, moves);
            return; 
        } 
        
        // 3. COMPUTER TURN
        String playerName = (mode == 2) ? "Random Player" : "AI";
        
        // Use the instance field 'aiPlayer' which was initialized in startLevel()
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
        updateBoard(); // Reset visuals
        
        for (int i = 0; i < currentPositions.length; i++) {
            int pos = currentPositions[i];
            int pieceId = i + 1;
            
            if (pos != -1 && humanPlayer.canPieceMove(pieceId)) {
                gridButtons[pos].setEnabled(true); 
                gridButtons[pos].setBackground(new Color(255, 235, 59)); // Yellow
                gridButtons[pos].setBorder(BorderFactory.createLineBorder(Color.ORANGE, 2));
            }
        }
    }

    private void handleGridClick(int index) {
        if (!isHumanTurn) return;

        int clickedPieceId = getPieceAt(index);

        // Select Piece
        if (clickedPieceId != -1 && humanPlayer.selectPiece(clickedPieceId)) {
            highlightValidPieces();
            gridButtons[index].setBackground(new Color(255, 140, 0)); // Dark Orange
            
            // Enable Destinations
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

        // Move Piece
        if (humanPlayer.isValidDestination(index)) {
            executeMove(humanPlayer.getSelectedPiece(), index);
            isHumanTurn = false;
            finishTurn();
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
    }

    private void finishTurn() {
        updateBoard();
        nextTurnButton.setEnabled(true);

        // --- CHECK WIN CONDITION (Logic from GameMain) ---
        if (gameState.isWinning(currentPositions)) {
            endGame(true, "CONGRATULATIONS! Puzzle solved successfully.");
            return;
        }

        // --- CHECK TARGET CAPTURED (Logic from GameMain) ---
        if (currentPositions[targetPiece - 1] == -1) {
            endGame(false, "FAILED! Target piece " + targetPiece + " was captured.");
            return;
        }

        currentTurn++;
        
        if (mainApp.getGameMode() == 1) {
             nextTurnButton.setText("Roll Dice");
        } else {
             nextTurnButton.setText("Next AI Turn");
        }
    }

    private void endGame(boolean won, String message) {
        gameEnded = true;
        nextTurnButton.setEnabled(false);

        // 1. Record Result to Leaderboard
        mainApp.recordGameResult(currentPlayerName, currentLevel, won ? "Won" : "Lost");

        // 2. Play Sound (Optional, using generic click for now if win/lose sounds aren't loaded)
        // SoundManager.getInstance().playSound(won ? "win.wav" : "lose.wav");

        // 3. Create Custom Options for the Popup
        Object[] options = {"Play Again", "Quit to Home"};
        
        // 4. Show the Dialog
        int choice = JOptionPane.showOptionDialog(
            this,
            message + "\n\nWhat would you like to do?", 
            won ? "Game Won!" : "Game Over",          
            JOptionPane.YES_NO_OPTION,
            won ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE,
            null, 
            options,
            options[0]
        );

        // 5. Handle User Choice
        if (choice == JOptionPane.YES_OPTION) {
            // Restart the same level
            startLevel(currentLevel, currentPlayerName);
        } else {
            // Quit to Home
            mainApp.showView("HOME");
        }
    }

    private void updateBoard() {
        for (JButton btn : gridButtons) {
            btn.setText("");
            btn.setBackground(new Color(245, 245, 245));
            btn.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
            btn.setEnabled(false);
        }
        
        // Goal
        gridButtons[0].setBorder(BorderFactory.createLineBorder(new Color(255, 215, 0), 2));
        
        // Pieces
        for (int i = 0; i < currentPositions.length; i++) {
            int pos = currentPositions[i];
            if (pos != -1) {
                int index = (pos / 10) * BOARD_SIZE + (pos % 10);
                if (index >= 0 && index < gridButtons.length) {
                    JButton btn = gridButtons[index];
                    btn.setText("P" + (i + 1));
                    btn.setFont(new Font("SansSerif", Font.BOLD, 16));
                    
                    if (i + 1 == targetPiece) {
                        btn.setBackground(new Color(255, 100, 100)); // Target
                        btn.setForeground(Color.WHITE);
                    } else {
                        btn.setBackground(new Color(70, 130, 180)); // Normal
                        btn.setForeground(Color.WHITE);
                    }
                }
            }
        }
    }

    // ============================
    // SETTINGS DIALOG
    // ============================
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

        // Music Switch Row 
        JPanel musicPanel = new JPanel(new BorderLayout());
        musicPanel.setOpaque(false);
        JLabel musicLabel = new JLabel("Background Music");
        musicLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        ToggleSwitch musicSwitch = new ToggleSwitch(SoundManager.getInstance().isMusicEnabled());
        musicSwitch.addSwitchListener(isOn -> SoundManager.getInstance().setMusicEnabled(isOn));
        musicPanel.add(musicLabel, BorderLayout.WEST);
        musicPanel.add(musicSwitch, BorderLayout.EAST);
        content.add(musicPanel);

        // Sound Switch Row 
        JPanel soundPanel = new JPanel(new BorderLayout());
        soundPanel.setOpaque(false);
        JLabel soundLabel = new JLabel("Sound Effects");
        soundLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        ToggleSwitch soundSwitch = new ToggleSwitch(SoundManager.getInstance().isSoundEnabled());
        soundSwitch.addSwitchListener(isOn -> SoundManager.getInstance().setSoundEnabled(isOn));
        soundPanel.add(soundLabel, BorderLayout.WEST);
        soundPanel.add(soundSwitch, BorderLayout.EAST);
        content.add(soundPanel);

        // Resume Button
        JButton resumeBtn = new RoundedButton("Resume Game");
        resumeBtn.setBackground(new Color(46, 204, 113)); 
        resumeBtn.addActionListener(e -> dialog.dispose());
        content.add(resumeBtn);

        // Home Button
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

    // ============================
    // CUSTOM BUTTON
    // ============================
    private static class RoundedButton extends JButton {
        private Color normalColor = new Color(0, 105, 120);
        private Color hoverColor = new Color(0, 140, 160);
        private Color pressedColor = new Color(0, 70, 80);
        
        // Flags to track state instead of overwriting background property
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
            
            // Choose color based on current state flags
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

    // ============================
    // TOGGLE SWITCH CLASS
    // ============================
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