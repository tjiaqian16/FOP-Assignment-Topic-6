import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import javax.imageio.ImageIO;

public class SettingsPage extends BackgroundImagePanel {
    private MainInterface mainApp;
    private SoundManager soundManager;

    // 1. Declare switches as class fields so they can be accessed later
    private ToggleSwitch musicSwitch;
    private ToggleSwitch soundSwitch;

    public SettingsPage(MainInterface app) {
        super("menu_bg.jpg");
        this.mainApp = app;
        this.soundManager = SoundManager.getInstance();

        // Use BorderLayout
        setLayout(new BorderLayout());

        // --- Back Button (Top-Left) ---
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

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setOpaque(false);
        topPanel.add(backBtn);
        add(topPanel, BorderLayout.NORTH);


        // --- Center Content Wrapper ---
        JPanel centerWrapper = new JPanel(new GridBagLayout());
        centerWrapper.setOpaque(false);

        // --- Title ---
        JLabel titleLabel = new JLabel("Settings");
        titleLabel.setFont(new Font("Serif", Font.BOLD, 40));
        titleLabel.setForeground(Color.WHITE);

        // --- Settings Components Box ---
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setOpaque(false);
        contentPanel.setBackground(new Color(255, 255, 255, 150)); // Semi-transparent box
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

        // 2. Initialize Music Toggle and assign to field
        JLabel musicLabel = createLabel("Background Music");
        musicSwitch = new ToggleSwitch(SoundManager.getInstance().isMusicEnabled());
        musicSwitch.addSwitchListener(isOn -> SoundManager.getInstance().setMusicEnabled(isOn));
        
        // 3. Initialize Sound Effect Toggle and assign to field
        JLabel soundLabel = createLabel("Sound Effects");
        soundSwitch = new ToggleSwitch(SoundManager.getInstance().isSoundEnabled());
        soundSwitch.addSwitchListener(isOn -> SoundManager.getInstance().setSoundEnabled(isOn));

        // Layout the settings inside the content box
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 20, 15, 20);
        gbc.anchor = GridBagConstraints.WEST;
        
        gbc.gridx = 0; gbc.gridy = 0; contentPanel.add(musicLabel, gbc);
        gbc.gridx = 1; contentPanel.add(musicSwitch, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1; contentPanel.add(soundLabel, gbc);
        gbc.gridx = 1; contentPanel.add(soundSwitch, gbc);

        // Add Title and Box to Center Wrapper
        GridBagConstraints mainGbc = new GridBagConstraints();
        mainGbc.insets = new Insets(20, 0, 20, 0);
        mainGbc.gridx = 0;
        
        mainGbc.gridy = 0; centerWrapper.add(titleLabel, mainGbc);
        mainGbc.gridy = 1; centerWrapper.add(contentPanel, mainGbc);
        
        add(centerWrapper, BorderLayout.CENTER);
    }

    // 4. Override setVisible to refresh state every time the page is shown
    @Override
    public void setVisible(boolean aFlag) {
        super.setVisible(aFlag);
        if (aFlag) {
            // Check SoundManager for the current real state and update UI
            if (musicSwitch != null) {
                musicSwitch.setState(SoundManager.getInstance().isMusicEnabled());
            }
            if (soundSwitch != null) {
                soundSwitch.setState(SoundManager.getInstance().isSoundEnabled());
            }
        }
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

        // 5. Add method to set state programmatically without triggering listener loop
        public void setState(boolean state) {
            this.isOn = state;
            repaint();
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