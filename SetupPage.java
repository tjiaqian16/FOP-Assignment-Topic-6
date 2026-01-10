import javax.swing.*;
import javax.swing.plaf.basic.BasicSliderUI;
import java.awt.*;
import java.io.File;
import javax.imageio.ImageIO;

public class SetupPage extends BackgroundImagePanel { 
    private MainInterface mainApp;
    private JTextField nameField;
    private JLabel nameLabel;
    private JSlider levelSlider; 

    public SetupPage(MainInterface app) {
        // Uses "setup_bg.jpg" specifically for this page.
        super("setup_bg.jpg"); 
        
        this.mainApp = app;
        // CHANGED: Use BorderLayout to position Back button at Top-Left
        setLayout(new BorderLayout());
        
        // --- Back Button (Moved to Top-Left) ---
        JButton backBtn = new JButton();
        try {
            File imgFile = new File("back_icon.png");
            if (imgFile.exists()) {
                ImageIcon icon = new ImageIcon(ImageIO.read(imgFile));
                Image scaled = icon.getImage().getScaledInstance(60, 60, Image.SCALE_SMOOTH);
                backBtn.setIcon(new ImageIcon(scaled));
                backBtn.setBorderPainted(false);
                backBtn.setContentAreaFilled(false);
                backBtn.setFocusPainted(false);
                backBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            } else {
                backBtn.setText("Back");
            }
        } catch (Exception e) {
            backBtn.setText("Back");
        }
        backBtn.addActionListener(e -> {
            SoundManager.getInstance().playSound("click.wav");
            mainApp.showView("PLAY_SELECTION");
        });

        // Panel for Back Button
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setOpaque(false);
        topPanel.add(backBtn);
        add(topPanel, BorderLayout.NORTH);

        // --- Center Content Panel ---
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setOpaque(false);
        
        // --- 1. Player Name Input ---
        nameLabel = new JLabel("Enter Player's Name: ");
        nameLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        // CHANGED: Text color to Black
        nameLabel.setForeground(Color.BLACK); 
        
        nameField = new JTextField(15);
        nameField.setFont(new Font("SansSerif", Font.PLAIN, 18));
        // Optional: Add a border to make the white box stand out against the background
        nameField.setBorder(BorderFactory.createLineBorder(new Color(0, 70, 140), 2));

        // --- 2. Level Selection (Slider) ---
        JLabel levelLabel = new JLabel("Select Level:");
        levelLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        // CHANGED: Text color to Black
        levelLabel.setForeground(Color.BLACK);

        levelSlider = new JSlider(JSlider.HORIZONTAL, 1, 4, 1);
        levelSlider.setMajorTickSpacing(1);
        levelSlider.setPaintTicks(true);
        levelSlider.setPaintLabels(true);
        levelSlider.setSnapToTicks(true);
        levelSlider.setOpaque(false); 
        // Style the numbers (labels) to be dark and bold
        levelSlider.setForeground(new Color(20, 20, 20));
        levelSlider.setFont(new Font("SansSerif", Font.BOLD, 16));
        levelSlider.setPreferredSize(new Dimension(350, 80));
        
        // CHANGED: Apply the creative custom UI
        levelSlider.setUI(new CreativeSliderUI(levelSlider));

        // --- 3. Start Game Button (Image) ---
        JButton startBtn = new JButton();
        try {
            File imgFile = new File("start_icon.png"); 
            if (imgFile.exists()) {
                ImageIcon icon = new ImageIcon(ImageIO.read(imgFile));
                if (icon.getIconWidth() > 200) {
                     Image scaled = icon.getImage().getScaledInstance(200, -1, Image.SCALE_SMOOTH);
                     icon = new ImageIcon(scaled);
                }
                startBtn.setIcon(icon);
                startBtn.setBorderPainted(false);
                startBtn.setContentAreaFilled(false);
                startBtn.setFocusPainted(false);
                startBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            } else {
                startBtn.setText("START GAME");
                startBtn.setFont(new Font("SansSerif", Font.BOLD, 24));
                startBtn.setBackground(new Color(46, 204, 113));
                startBtn.setForeground(Color.WHITE);
                startBtn.setPreferredSize(new Dimension(200, 60));
            }
        } catch (Exception e) {
            startBtn.setText("START");
        }
        startBtn.addActionListener(e -> handleStart());

        // --- Layout for Center Panel ---
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 10, 15, 10);
        
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        centerPanel.add(nameLabel, gbc);

        gbc.gridy = 1;
        centerPanel.add(nameField, gbc);

        gbc.gridy = 2;
        gbc.insets = new Insets(30, 10, 5, 10); 
        centerPanel.add(levelLabel, gbc);

        gbc.gridy = 3;
        gbc.insets = new Insets(0, 10, 30, 10);
        centerPanel.add(levelSlider, gbc);

        gbc.gridy = 4;
        gbc.insets = new Insets(10, 10, 10, 10);
        centerPanel.add(startBtn, gbc);

        add(centerPanel, BorderLayout.CENTER);
    }

    @Override
    public void setVisible(boolean aFlag) {
        super.setVisible(aFlag);
        if (aFlag) {
            nameField.setText("");
            int mode = mainApp.getGameMode();
            boolean isHuman = (mode == 1);
            nameLabel.setVisible(isHuman);
            nameField.setVisible(isHuman);
        }
    }

    private void handleStart() {
        SoundManager.getInstance().playSound("click.wav");
        
        int mode = mainApp.getGameMode();
        String name = "Player";

        if (mode == 1) {
            name = nameField.getText().trim();
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Name cannot be empty.");
                return;
            }
            if (!name.matches("^[a-zA-Z0-9 ]+$")) {
                JOptionPane.showMessageDialog(this, "Invalid Name! Alphanumeric only.");
                return;
            }
        } else if (mode == 2) {
            name = "RandomPlayer";
        } else {
            name = "AIPlayer";
        }

        int level = levelSlider.getValue();
        mainApp.startGame(name, level);
    }

    // --- Custom UI for Creative Slider ---
    private static class CreativeSliderUI extends BasicSliderUI {
        private static final Color GRADIENT_START = new Color(255, 100, 80);  // Coral/Orange
        private static final Color GRADIENT_END = new Color(70, 130, 180);    // Steel Blue
        private static final Color THUMB_COLOR = new Color(255, 215, 0);      // Gold

        public CreativeSliderUI(JSlider b) {
            super(b);
        }

        @Override
        public void paintTrack(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            Rectangle trackBounds = trackRect;
            int h = 14; // Thicker track for better visuals
            int y = trackBounds.y + (trackBounds.height - h) / 2;

            // Gradient Track
            GradientPaint gp = new GradientPaint(trackBounds.x, y, GRADIENT_START, 
                                                 trackBounds.x + trackBounds.width, y, GRADIENT_END);
            g2.setPaint(gp);
            g2.fillRoundRect(trackBounds.x, y, trackBounds.width, h, h, h);
            
            // Track Border
            g2.setColor(new Color(50, 50, 50, 100));
            g2.drawRoundRect(trackBounds.x, y, trackBounds.width, h, h, h);
        }

        @Override
        public void paintThumb(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            Rectangle thumbBounds = thumbRect;
            int w = thumbBounds.width;
            int h = thumbBounds.height;

            // Make thumb slightly smaller than full bounds for margin
            int pad = 2;
            
            // Shadow
            g2.setColor(new Color(0, 0, 0, 50));
            g2.fillOval(thumbBounds.x + pad + 2, thumbBounds.y + pad + 2, w - 2*pad, h - 2*pad);

            // Gold Knob
            g2.setColor(THUMB_COLOR);
            g2.fillOval(thumbBounds.x + pad, thumbBounds.y + pad, w - 2*pad, h - 2*pad);
            
            // Shine effect
            g2.setColor(new Color(255, 255, 255, 150));
            g2.fillOval(thumbBounds.x + pad + 4, thumbBounds.y + pad + 4, (w - 2*pad)/3, (h - 2*pad)/3);
            
            // Border
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(2));
            g2.drawOval(thumbBounds.x + pad, thumbBounds.y + pad, w - 2*pad, h - 2*pad);
        }
        
        @Override
        protected Dimension getThumbSize() {
            // Larger thumb for better grip visually
            return new Dimension(26, 26);
        }
    }
}