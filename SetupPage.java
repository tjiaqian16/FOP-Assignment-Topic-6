import javax.swing.*;
import java.awt.*;

public class SetupPage extends JPanel {
    private MainInterface mainApp;
    private JTextField nameField;
    private JLabel nameLabel;
    private JComboBox<String> levelSelector;

    public SetupPage(MainInterface app) {
        this.mainApp = app;
        setLayout(new GridBagLayout());
        setBackground(new Color(230, 240, 255));

        JLabel titleLabel = new JLabel("Game Setup");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));

        nameLabel = new JLabel("Player Name:");
        nameField = new JTextField(15);

        JLabel levelLabel = new JLabel("Select Level:");
        String[] levels = {"Level 1", "Level 2", "Level 3", "Level 4"};
        levelSelector = new JComboBox<>(levels);

        JButton startBtn = new JButton("Start Game");
        startBtn.addActionListener(e -> handleStart());

        JButton backBtn = new JButton("Back");
        backBtn.addActionListener(e -> mainApp.showView("PLAY_SELECTION"));

        // Layout
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        add(titleLabel, gbc);

        gbc.gridwidth = 1; gbc.gridy = 1;
        add(nameLabel, gbc);
        gbc.gridx = 1; add(nameField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        add(levelLabel, gbc);
        gbc.gridx = 1; add(levelSelector, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        JPanel btnPanel = new JPanel();
        btnPanel.setOpaque(false);
        btnPanel.add(backBtn);
        btnPanel.add(startBtn);
        add(btnPanel, gbc);
    }

    // Update UI when this page is shown
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

        int level = levelSelector.getSelectedIndex() + 1;
        mainApp.startGame(name, level);
    }
}
