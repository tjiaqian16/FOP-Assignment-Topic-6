import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class SettingsPage extends BackgroundImagePanel {
    private MainInterface mainApp;
    private SoundManager soundManager;

    public SettingsPage(MainInterface app) {
        super("menu_bg.jpg");
        this.mainApp = app;
        this.soundManager = SoundManager.getInstance();

        setLayout(new GridBagLayout());

        // --- Title ---
        JLabel titleLabel = new JLabel("Settings");
        titleLabel.setFont(new Font("Serif", Font.BOLD, 40));
        titleLabel.setForeground(Color.WHITE);

        // --- Settings Components ---
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setOpaque(false);
        contentPanel.setBackground(new Color(255, 255, 255, 150)); // Semi-transparent box
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

        // 1. Music Toggle
        JLabel musicLabel = createLabel("Background Music");
        ToggleSwitch musicSwitch = new ToggleSwitch(soundManager.isMusicEnabled());
        musicSwitch.addSwitchListener(isOn -> soundManager.setMusicEnabled(isOn));

        // 2. Sound Effect Toggle
        JLabel soundLabel = createLabel("Sound Effects");
        ToggleSwitch soundSwitch = new ToggleSwitch(soundManager.isSoundEnabled());
        soundSwitch.addSwitchListener(isOn -> soundManager.setSoundEnabled(isOn));

        // Layout the settings inside the content box
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 20, 15, 20);
        gbc.anchor = GridBagConstraints.WEST;
        
        gbc.gridx = 0; gbc.gridy = 0; contentPanel.add(musicLabel, gbc);
        gbc.gridx = 1; contentPanel.add(musicSwitch, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1; contentPanel.add(soundLabel, gbc);
        gbc.gridx = 1; contentPanel.add(soundSwitch, gbc);

        // --- Back Button ---
        JButton backBtn = new JButton("Back");
        backBtn.setPreferredSize(new Dimension(150, 40));
        backBtn.setFont(new Font("SansSerif", Font.BOLD, 18));
        backBtn.addActionListener(e -> {
            soundManager.playSound("click.wav");
            mainApp.showView("HOME");
        });

        // --- Main Layout ---
        GridBagConstraints mainGbc = new GridBagConstraints();
        mainGbc.insets = new Insets(20, 0, 20, 0);
        mainGbc.gridx = 0;
        
        mainGbc.gridy = 0; add(titleLabel, mainGbc);
        mainGbc.gridy = 1; add(contentPanel, mainGbc);
        mainGbc.gridy = 2; add(backBtn, mainGbc);
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.BOLD, 24));
        label.setForeground(Color.WHITE);
        // Add a slight shadow/outline effect for readability
        label.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 20)); 
        return label;
    }

    /**
     * Custom Toggle Switch Component (Inner Class)
     */
    private static class ToggleSwitch extends JPanel {
        private boolean isOn;
        private Color switchOnColor = new Color(46, 204, 113); // Green
        private Color switchOffColor = new Color(189, 195, 199); // Gray
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
            int pad = 4; // Padding between circle and edge

            // Draw Background (Rounded Rectangle)
            g2.setColor(isOn ? switchOnColor : switchOffColor);
            g2.fillRoundRect(0, 0, w, h, h, h); // h is the corner radius for full roundness

            // Draw Switch Button (Circle)
            g2.setColor(buttonColor);
            int circleSize = h - (pad * 2);
            int x = isOn ? (w - circleSize - pad) : pad; // Slide position
            g2.fillOval(x, pad, circleSize, circleSize);
        }
    }
}
