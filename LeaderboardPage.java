import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import javax.imageio.ImageIO;

public class LeaderboardPage extends BackgroundImagePanel {
    private MainInterface mainApp;

    public LeaderboardPage(MainInterface app) {
        super("menu_bg.jpg");
        this.mainApp = app;
        setLayout(new BorderLayout());

        // --- Top Panel (Back Button + Title) ---
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Back Button
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
                backBtn.setText("Back to Menu");
            }
        } catch (Exception e) {
            backBtn.setText("Back to Menu");
        }

        backBtn.addActionListener(e -> {
            SoundManager.getInstance().playSound("click.wav");
            mainApp.showView("HOME");
        });
        
        // Title
        JLabel title = new JLabel("Hall of Fame", SwingConstants.CENTER);
        title.setFont(new Font("Serif", Font.BOLD, 30));
        title.setForeground(Color.WHITE);
        
        topPanel.add(backBtn, BorderLayout.WEST);
        topPanel.add(title, BorderLayout.CENTER);
        
        add(topPanel, BorderLayout.NORTH);

        // --- Table Content ---
        String[] columns = {"Rank", "Player", "Level", "Result"};
        Object[][] data = {
            {"1", "Alice", "Level 4", "Won"},
            {"2", "Bob", "Level 2", "Lost"}
        };

        JTable table = new JTable(new DefaultTableModel(data, columns));
        add(new JScrollPane(table), BorderLayout.CENTER);
    }
}
