import javax.swing.*;
import java.awt.*;

public class HomePage extends BackgroundImagePanel {
    private MainInterface mainApp;
    private SoundManager soundManager;

    public HomePage(MainInterface app) {
        super("menu_bg.jpg"); // Ensure this file exists
        this.mainApp = app;
        this.soundManager = SoundManager.getInstance();

        // Start Music
        soundManager.playMusic("bgm.wav");

        setLayout(new GridBagLayout()); 

        JLabel titleLabel = new JLabel("Einstein Würfelt Nicht");
        titleLabel.setFont(new Font("Serif", Font.BOLD, 45));
        titleLabel.setForeground(Color.WHITE);

        JButton playBtn = createStyledButton("Play");
        JButton leaderboardBtn = createStyledButton("Leaderboard");
        JButton settingBtn = createStyledButton("Setting");
        JButton exitBtn = createStyledButton("Exit");

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
            JOptionPane.showMessageDialog(this, "Settings coming soon!");
        });

        exitBtn.addActionListener(e -> System.exit(0));

        // Layout
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
        btn.setBackground(new Color(255, 255, 255, 200));
        btn.setFocusPainted(false);
        return btn;
    }
}
