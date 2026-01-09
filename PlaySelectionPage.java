import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import javax.imageio.ImageIO;

public class PlaySelectionPage extends BackgroundImagePanel {
    private MainInterface mainApp;
    private SoundManager soundManager;

    public PlaySelectionPage(MainInterface app) {
        super("menu_bg.jpg");
        this.mainApp = app;
        this.soundManager = SoundManager.getInstance();

        // CHANGED: Use BorderLayout to place Back button at Top-Left
        setLayout(new BorderLayout());

        // --- Back Button (Moved to Top-Left) ---
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
            } else {
                backBtn.setText("Back"); 
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

        // --- Center Content Panel ---
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setOpaque(false);

        // --- 1. Title Section (Image with Fallback) ---
        JLabel titleLabel = new JLabel();
        try {
            File imgFile = new File("select_mode.png");
            if (imgFile.exists()) {
                ImageIcon titleIcon = new ImageIcon(ImageIO.read(imgFile));
                int targetWidth = 500;
                if (titleIcon.getIconWidth() > targetWidth) {
                    int newHeight = (targetWidth * titleIcon.getIconHeight()) / titleIcon.getIconWidth();
                    Image scaledImg = titleIcon.getImage().getScaledInstance(targetWidth, newHeight, Image.SCALE_SMOOTH);
                    titleIcon = new ImageIcon(scaledImg);
                }
                titleLabel.setIcon(titleIcon);
            } else {
                titleLabel.setText("Select Mode");
                titleLabel.setFont(new Font("SansSerif", Font.BOLD | Font.ITALIC, 45)); 
                titleLabel.setForeground(Color.WHITE);
            }
        } catch (Exception e) {
            titleLabel.setText("Select Mode");
            titleLabel.setForeground(Color.WHITE);
        }

        // --- 2. Button Section ---
        JButton humanBtn = createModeButton("Human Player");
        JButton randomBtn = createModeButton("Random Player");
        JButton aiBtn = createModeButton("AI Player");

        humanBtn.addActionListener(e -> selectMode(1));
        randomBtn.addActionListener(e -> selectMode(2));
        aiBtn.addActionListener(e -> selectMode(3));

        // --- Layout Constraints for Center ---
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 0, 15, 0);
        gbc.gridx = 0;

        gbc.gridy = 0; centerPanel.add(titleLabel, gbc);
        gbc.gridy = 1; centerPanel.add(humanBtn, gbc);
        gbc.gridy = 2; centerPanel.add(randomBtn, gbc);
        gbc.gridy = 3; centerPanel.add(aiBtn, gbc);
        
        add(centerPanel, BorderLayout.CENTER);
    }

    private void selectMode(int mode) {
        soundManager.playSound("click.wav");
        mainApp.setGameMode(mode);
        mainApp.showView("SETUP");
    }

    private JButton createModeButton(String text) {
        RoundedButton btn = new RoundedButton(text);
        btn.setPreferredSize(new Dimension(300, 70));
        btn.setFont(new Font("SansSerif", Font.BOLD, 24));
        return btn;
    }

    private static class RoundedButton extends JButton {
        private Color normalColor = new Color(0, 70, 140);
        private Color hoverColor = new Color(30, 100, 180);
        private Color pressedColor = new Color(0, 40, 90);
        
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
            g2.setColor(new Color(0, 20, 60, 150)); 
            g2.fillRoundRect(0, 8, getWidth(), getHeight() - 8, 40, 40);
            if (isPressed) g2.setColor(pressedColor);
            else if (isHovered) g2.setColor(hoverColor);
            else g2.setColor(normalColor);
            int yOffset = isPressed ? 4 : 0;
            g2.fillRoundRect(0, yOffset, getWidth(), getHeight() - 8, 40, 40);
            super.paintComponent(g2);
            g2.dispose();
        }
    }
}