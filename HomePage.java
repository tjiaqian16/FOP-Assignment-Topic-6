import javax.swing.*;
import java.awt.*;

// 1. Extend BackgroundImagePanel instead of JPanel
public class HomePage extends BackgroundImagePanel {
    private MainInterface mainApp;
    private SoundManager soundManager;

    public HomePage(MainInterface app) {
        // 2. Load the specific image file here
        super("menu_bg.jpg"); 
        
        this.mainApp = app;
        this.soundManager = SoundManager.getInstance();
        
        // Start Music
        soundManager.playMusic("bgm.wav");

        setLayout(new GridBagLayout()); 

        // 3. Title Label (Make it pop!)
        JLabel titleLabel = new JLabel("Einstein Würfelt Nicht");
        titleLabel.setFont(new Font("Serif", Font.BOLD, 45));
        titleLabel.setForeground(Color.WHITE); // White text looks better on backgrounds
        
        // Optional: Add a black outline effect or shadow if needed (simple version here)
        // titleLabel.setForeground(new Color(255, 215, 0)); // Gold color

        // 4. Create Buttons
        JButton playBtn = createStyledButton("Play");
        JButton leaderboardBtn = createStyledButton("Leaderboard");
        JButton settingBtn = createStyledButton("Setting");
        JButton exitBtn = createStyledButton("Exit");

        // Add Actions
        playBtn.addActionListener(e -> {
            soundManager.playSound("click.wav");
            mainApp.showView("SETUP");
        });
        leaderboardBtn.addActionListener(e -> {
            soundManager.playSound("click.wav");
            mainApp.showView("LEADERBOARD");
        });
        settingBtn.addActionListener(e -> {
            soundManager.playSound("click.wav");
            JOptionPane.showMessageDialog(this, "Settings coming soon!");
        });
        exitBtn.addActionListener(e -> System.exit(0));

        // Layout Constraints
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 0, 15, 0);
        gbc.gridx = 0;

        gbc.gridy = 0; add(titleLabel, gbc);
        gbc.gridy = 1; add(playBtn, gbc);
        gbc.gridy = 2; add(leaderboardBtn, gbc);
        gbc.gridy = 3; add(settingBtn, gbc);
        gbc.gridy = 4; add(exitBtn, gbc);
    }

    private JButton createStyledButton(String text) {
        JButton btn = new JButton(text);
        btn.setPreferredSize(new Dimension(250, 50));
        btn.setFont(new Font("SansSerif", Font.BOLD, 20));
        
        // 5. Make buttons look good on an image
        btn.setBackground(new Color(255, 255, 255, 200)); // White with slight transparency
        btn.setForeground(Color.BLACK);
        btn.setFocusPainted(false);
        return btn;
    }
}
