import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

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
        // Using HTML to wrap the long text "Einstein Würfelt Nicht" into three big lines
        JLabel titleLabel = new JLabel("<html><div style='text-align: center;'>Einstein<br>Würfelt<br>Nicht</div></html>");
        titleLabel.setFont(new Font("Serif", Font.BOLD, 85)); // Increased Font Size
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        // --- 2. Button Section (Right Side) ---
        JPanel buttonPanel = new JPanel(new GridLayout(4, 1, 0, 25)); // Increased gap to 25
        buttonPanel.setOpaque(false); 

        // Create buttons with "Pill" style, using ORIGINAL names
        JButton playBtn = createRoundedButton("Play");
        JButton leaderboardBtn = createRoundedButton("Leaderboard");
        JButton settingBtn = createRoundedButton("Setting");
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

        settingBtn.addActionListener(e -> {
            soundManager.playSound("click.wav");
            mainApp.showView("SETTINGS"); 
        });

        exitBtn.addActionListener(e -> System.exit(0));

        // Add buttons to panel
        buttonPanel.add(playBtn);
        buttonPanel.add(leaderboardBtn);
        buttonPanel.add(settingBtn);
        buttonPanel.add(exitBtn);

        // --- Main Layout Constraints ---
        GridBagConstraints gbc = new GridBagConstraints();

        // Constraint for Left Side (Title)
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.55; 
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.CENTER;
        
        JPanel titleWrapper = new JPanel(new GridBagLayout());
        titleWrapper.setOpaque(false);
        titleWrapper.add(titleLabel);
        add(titleWrapper, gbc);

        // Constraint for Right Side (Buttons)
        gbc.gridx = 1;
        gbc.weightx = 0.45; 
        gbc.fill = GridBagConstraints.NONE; 
        gbc.anchor = GridBagConstraints.CENTER; 
        add(buttonPanel, gbc);
    }

    /**
     * Helper to create the styled buttons
     */
    private JButton createRoundedButton(String text) {
        RoundedButton btn = new RoundedButton(text);
        // Made buttons BIGGER (300 width, 75 height)
        btn.setPreferredSize(new Dimension(300, 75)); 
        // Increased Font Size to 30
        btn.setFont(new Font("SansSerif", Font.BOLD, 30));
        return btn;
    }

    /**
     * Custom Button Class (Orange Pill Shape)
     */
    private static class RoundedButton extends JButton {
        private Color normalColor = new Color(255, 180, 80); 
        private Color hoverColor = new Color(255, 200, 100); 
        private Color pressedColor = new Color(220, 140, 50); 
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
            g2.setColor(new Color(200, 120, 40)); 
            // 40 is the corner radius (increased for bigger buttons)
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
