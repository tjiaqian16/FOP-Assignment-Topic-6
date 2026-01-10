import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import javax.imageio.ImageIO;

public class HomePage extends BackgroundImagePanel {
    private MainInterface mainApp;
    private SoundManager soundManager;

    public HomePage(MainInterface app) {
        super("menu_bg.jpg"); 
        this.mainApp = app;
        this.soundManager = SoundManager.getInstance();

        // Start Music
        soundManager.playMusic("bgm.wav");

        setLayout(new GridBagLayout()); 

        // --- 1. Title Section (Left Side) ---
        JLabel titleLabel = new JLabel();
        try {
            // Load the image "title.png"
            ImageIcon titleIcon = new ImageIcon(ImageIO.read(new File("title.png")));
            
            int targetWidth = 500;
            if (titleIcon.getIconWidth() > targetWidth) {
                int newHeight = (targetWidth * titleIcon.getIconHeight()) / titleIcon.getIconWidth();
                Image scaledImg = titleIcon.getImage().getScaledInstance(targetWidth, newHeight, Image.SCALE_SMOOTH);
                titleIcon = new ImageIcon(scaledImg);
            }
            
            titleLabel.setIcon(titleIcon);
        } catch (Exception e) {
            titleLabel.setText("<html><h1 style='color:white;'>Title Image<br>Not Found</h1></html>");
        }
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        // --- 2. Button Section (Right Side) ---
        JPanel buttonPanel = new JPanel(new GridLayout(5, 1, 0, 20)); 
        buttonPanel.setOpaque(false); 

        // Create buttons
        JButton playBtn = createRoundedButton("Play");
        JButton leaderboardBtn = createRoundedButton("Leaderboard");
        JButton rulesBtn = createRoundedButton("Game Rules"); 
        JButton settingBtn = createRoundedButton("Settings");
        JButton exitBtn = createRoundedButton("Exit");

        // Actions
        playBtn.addActionListener(e -> {
            soundManager.playSound("click.wav");
            mainApp.showView("PLAY_SELECTION");
        });

        leaderboardBtn.addActionListener(e -> {
            soundManager.playSound("click.wav");
            mainApp.showView("LEADERBOARD");
        });

        rulesBtn.addActionListener(e -> {
            soundManager.playSound("click.wav");
            showRulesDialog();
        });

        settingBtn.addActionListener(e -> {
            soundManager.playSound("click.wav");
            mainApp.showView("SETTINGS"); 
        });

        exitBtn.addActionListener(e -> System.exit(0));

        // Add buttons to panel
        buttonPanel.add(playBtn);
        buttonPanel.add(leaderboardBtn);
        buttonPanel.add(rulesBtn); 
        buttonPanel.add(settingBtn);
        buttonPanel.add(exitBtn);

        // --- Main Layout Constraints ---
        GridBagConstraints gbc = new GridBagConstraints();

        // Left Side (Title)
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.5; 
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.EAST; 
        gbc.insets = new Insets(0, 0, 0, 30); 
        
        add(titleLabel, gbc);

        // Right Side (Buttons)
        gbc.gridx = 1;
        gbc.weightx = 0.5; 
        gbc.fill = GridBagConstraints.NONE; 
        gbc.anchor = GridBagConstraints.WEST; 
        gbc.insets = new Insets(0, 30, 0, 0); 
        
        add(buttonPanel, gbc);
    }

    // ==========================================
    // UPDATED: RULES POPUP DIALOG
    // ==========================================
    private void showRulesDialog() {
        JDialog dialog = new JDialog(mainApp, "Game Rules", true);
        dialog.setUndecorated(true);
        dialog.setSize(550, 520); // Increased size to fit detailed rules
        dialog.setLocationRelativeTo(this);

        // Main Container
        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(new Color(255, 250, 240)); // Floral White
        content.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(139, 69, 19), 4), // Brown Border
            new EmptyBorder(15, 15, 15, 15)
        ));

        // Title
        JLabel title = new JLabel("GAME RULES");
        title.setFont(new Font("SansSerif", Font.BOLD, 28));
        title.setForeground(new Color(139, 69, 19));
        title.setHorizontalAlignment(SwingConstants.CENTER);
        title.setBorder(new EmptyBorder(0, 0, 10, 0));
        content.add(title, BorderLayout.NORTH);

        // UPDATED RULES TEXT
        String rulesHtml = "<html><body style='width: 400px; font-family: sans-serif; font-size: 11px;'>" +
                "<ul>" +
                "<li><b>Game Flow:</b> The dice sequence is fixed for each level. You have a <b>limit of 30 moves</b>.</li><br>" +
                "<li><b>Choosing a Piece:</b>" +
                "   <ul>" +
                "   <li>If the dice number <b>matches</b> a piece on the board, you <b>must</b> move that piece.</li>" +
                "   <li>If the dice number <b>does not match</b> any piece, you can choose:" +
                "       <ol>" +
                "       <li>The piece with the <b>smallest number > dice</b>.</li>" +
                "       <li>The piece with the <b>biggest number < dice</b>.</li>" +
                "       </ol>" +
                "   </li>" +
                "   </ul>" +
                "</li><br>" +
                "<li><b>Movement:</b> A piece can move to any of the <b>8 adjacent squares</b>. Only one move per turn.</li><br>" +
                "<li><b>Capturing:</b> If you move to a square occupied by another piece, that piece is <b>captured</b> and removed.</li><br>" +
                "<li><b>WIN:</b> The Target Piece reaches <b>Square 0</b> within 30 moves.</li>" +
                "<li><b>LOSE:</b> The Target Piece fails to reach Square 0 after 30 moves or target piece is captured.</li>" +
                "</ul></body></html>";

        JEditorPane rulesPane = new JEditorPane("text/html", rulesHtml);
        rulesPane.setEditable(false);
        rulesPane.setOpaque(false);
        
        JScrollPane scrollPane = new JScrollPane(rulesPane);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        content.add(scrollPane, BorderLayout.CENTER);

        // Close Button
        JButton closeBtn = new JButton("Understood");
        closeBtn.setFont(new Font("SansSerif", Font.BOLD, 16));
        closeBtn.setBackground(new Color(46, 204, 113));
        closeBtn.setForeground(Color.WHITE);
        closeBtn.setFocusPainted(false);
        closeBtn.setBorderPainted(false);
        closeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeBtn.addActionListener(e -> {
            soundManager.playSound("click.wav");
            dialog.dispose();
        });
        
        JPanel btnPanel = new JPanel();
        btnPanel.setOpaque(false);
        btnPanel.setBorder(new EmptyBorder(10, 0, 0, 0));
        btnPanel.add(closeBtn);
        content.add(btnPanel, BorderLayout.SOUTH);

        dialog.add(content);
        dialog.setVisible(true);
    }

    /**
     * Helper to create the styled buttons
     */
    private JButton createRoundedButton(String text) {
        RoundedButton btn = new RoundedButton(text);
        btn.setPreferredSize(new Dimension(300, 75)); 
        btn.setFont(new Font("SansSerif", Font.BOLD, 30));
        return btn;
    }

    /**
     * Custom Button Class
     */
    private static class RoundedButton extends JButton {
        private Color normalColor = new Color(0, 105, 120); 
        private Color hoverColor = new Color(0, 150, 170);   
        private Color pressedColor = new Color(0, 70, 80);   
        
        private boolean isHovered = false;
        private boolean isPressed = false;

        public RoundedButton(String text) {
            super(text);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setForeground(Color.WHITE);
            setCursor(new Cursor(Cursor.HAND_CURSOR));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) { isHovered = true; repaint(); }
                @Override
                public void mouseExited(MouseEvent e) { isHovered = false; isPressed = false; repaint(); }
                @Override
                public void mousePressed(MouseEvent e) { isPressed = true; repaint(); }
                @Override
                public void mouseReleased(MouseEvent e) { isPressed = false; repaint(); }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Draw Shadow
            g2.setColor(new Color(0, 50, 60, 150)); 
            g2.fillRoundRect(0, 8, getWidth(), getHeight() - 8, 40, 40);

            // Choose color
            if (isPressed) g2.setColor(pressedColor);
            else if (isHovered) g2.setColor(hoverColor);
            else g2.setColor(normalColor);
            
            // Draw Main Button
            int yOffset = isPressed ? 4 : 0;
            g2.fillRoundRect(0, yOffset, getWidth(), getHeight() - 8, 40, 40);

            // Draw Text
            super.paintComponent(g2);
            g2.dispose();
        }
    }
}