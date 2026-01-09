import javax.swing.*;
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
        nameLabel.setForeground(Color.WHITE); 
        
        nameField = new JTextField(15);
        nameField.setFont(new Font("SansSerif", Font.PLAIN, 18));

        // --- 2. Level Selection (Slider) ---
        JLabel levelLabel = new JLabel("Select Level:");
        levelLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        levelLabel.setForeground(Color.WHITE);

        levelSlider = new JSlider(JSlider.HORIZONTAL, 1, 4, 1);
        levelSlider.setMajorTickSpacing(1);
        levelSlider.setPaintTicks(true);
        levelSlider.setPaintLabels(true);
        levelSlider.setSnapToTicks(true);
        levelSlider.setOpaque(false); 
        levelSlider.setForeground(Color.WHITE);
        levelSlider.setFont(new Font("SansSerif", Font.BOLD, 16));
        levelSlider.setPreferredSize(new Dimension(300, 80));

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
        // CHANGED: Only start button remains here
        centerPanel.add(startBtn, gbc);

        add(centerPanel, BorderLayout.CENTER);
    }

    @Override
    public void setVisible(boolean aFlag) {
        super.setVisible(aFlag);
        if (aFlag) {
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
            if (!name.matches("^[a-zA-Z0-9]+$")) {
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
}
