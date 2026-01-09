import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import javax.imageio.ImageIO;

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

        // --- Back Button (Image) ---
        JButton backBtn = new JButton();
        try {
            File imgFile = new File("back_icon.png");
            if (imgFile.exists()) {
                ImageIcon icon = new ImageIcon(ImageIO.read(imgFile));
                Image scaled = icon.getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH);
                backBtn.setIcon(new ImageIcon(scaled));
                backBtn.setBorderPainted(false);
                backBtn.setContentAreaFilled(false);
                backBtn.setFocusPainted(false);
                // Remove size preference if using icon to let it auto-size or set manually
                backBtn.setPreferredSize(new Dimension(60, 60)); 
            } else {
                backBtn.setText("Back");
                backBtn.setPreferredSize(new Dimension(150, 40));
                backBtn.setFont(new Font("SansSerif", Font.BOLD, 18));
            }
        } catch (Exception e) {
            backBtn.setText("Back");
        }

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
        label.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 20)); 
        return label;
    }

    private static class ToggleSwitch extends JPanel {
        private boolean isOn;
        private Color switchOnColor = new Color(46, 204, 113); 
        private Color switchOffColor = new Color(189, 195, 199); 
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
            int pad = 4;

            g2.setColor(isOn ? switchOnColor : switchOffColor);
            g2.fillRoundRect(0, 0, w, h, h, h);

            g2.setColor(buttonColor);
            int circleSize = h - (pad * 2);
            int x = isOn ? (w - circleSize - pad) : pad;
            g2.fillOval(x, pad, circleSize, circleSize);
        }
    }
}
