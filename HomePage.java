import javax.swing.*;
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
        // UPDATED: Now uses an image instead of text
        JLabel titleLabel = new JLabel();
        try {
            // Load the image "title.png". Ensure this file exists in your project folder!
            ImageIcon titleIcon = new ImageIcon(ImageIO.read(new File("title.png")));
            
            // Optional: Scale image if it's too big (Example: limit width to 500)
            int targetWidth = 500;
            if (titleIcon.getIconWidth() > targetWidth) {
                int newHeight = (targetWidth * titleIcon.getIconHeight()) / titleIcon.getIconWidth();
                Image scaledImg = titleIcon.getImage().getScaledInstance(targetWidth, newHeight, Image.SCALE_SMOOTH);
                titleIcon = new ImageIcon(scaledImg);
            }
            
            titleLabel.setIcon(titleIcon);
        } catch (Exception e) {
            // Fallback if image is missing
            titleLabel.setText("<html><h1 style='color:white;'>Title Image<br>Not Found</h1></html>");
            System.out.println("Could not find 'title.png'. Please add the file.");
        }
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        // --- 2. Button Section (Right Side) ---
        JPanel buttonPanel = new JPanel(new GridLayout(4, 1, 0, 25)); 
        buttonPanel.setOpaque(false); 

        // Create buttons
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
        gbc.weightx = 0.5; 
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.EAST; // Push title to the center line
        gbc.insets = new Insets(0, 0, 0, 30); // Gap between title and center
        
        add(titleLabel, gbc);

        // Constraint for Right Side (Buttons)
        gbc.gridx = 1;
        gbc.weightx = 0.5; 
        gbc.fill = GridBagConstraints.NONE; 
        gbc.anchor = GridBagConstraints.WEST; // Push buttons to the center line
        gbc.insets = new Insets(0, 30, 0, 0); // Gap between buttons and center
        
        add(buttonPanel, gbc);
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
        // UPDATED COLORS: Deep Teal/Blue to contrast nicely with Orange
        private Color normalColor = new Color(0, 105, 120); // Deep Teal
        private Color hoverColor = new Color(0, 150, 170);   // Lighter Teal
        private Color pressedColor = new Color(0, 70, 80);   // Darker Teal
        
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
            g2.setColor(new Color(0, 50, 60, 150)); // Semi-transparent dark shadow
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
