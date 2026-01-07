import javax.swing.*;
import java.awt.*;

public class PlaySelectionPage extends BackgroundImagePanel {
    private MainInterface mainApp;
    private SoundManager soundManager;

    public PlaySelectionPage(MainInterface app) {
        super("menu_bg.jpg");
        this.mainApp = app;
        this.soundManager = SoundManager.getInstance();

        setLayout(new GridBagLayout());

        JLabel titleLabel = new JLabel("Select Opponent");
        titleLabel.setFont(new Font("Serif", Font.BOLD, 40));
        titleLabel.setForeground(Color.WHITE);

        JButton humanBtn = createModeButton("Human Player");
        JButton randomBtn = createModeButton("Random Player");
        JButton aiBtn = createModeButton("AI Player");

        // Set Mode and Go to Setup
        humanBtn.addActionListener(e -> selectMode(1));
        randomBtn.addActionListener(e -> selectMode(2));
        aiBtn.addActionListener(e -> selectMode(3));

        // Bottom Navigation
        JPanel bottomPanel = new JPanel(new FlowLayout());
        bottomPanel.setOpaque(false);
        JButton backBtn = new JButton("Back");
        backBtn.addActionListener(e -> {
            soundManager.playSound("click.wav");
            mainApp.showView("HOME");
        });
        bottomPanel.add(backBtn);

        // Layout
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 0, 15, 0);
        gbc.gridx = 0;

        gbc.gridy = 0; add(titleLabel, gbc);
        gbc.gridy = 1; add(humanBtn, gbc);
        gbc.gridy = 2; add(randomBtn, gbc);
        gbc.gridy = 3; add(aiBtn, gbc);
        gbc.gridy = 4; add(bottomPanel, gbc);
    }

    private void selectMode(int mode) {
        soundManager.playSound("click.wav");
        mainApp.setGameMode(mode);
        mainApp.showView("SETUP");
    }

    private JButton createModeButton(String text) {
        JButton btn = new JButton(text);
        btn.setPreferredSize(new Dimension(300, 60));
        btn.setFont(new Font("SansSerif", Font.BOLD, 20));
        btn.setBackground(new Color(255, 255, 255, 220));
        btn.setFocusPainted(false);
        return btn;
    }
}
