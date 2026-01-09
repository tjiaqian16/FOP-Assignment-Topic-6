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

        JLabel title = new JLabel("Hall of Fame", SwingConstants.CENTER);
        title.setFont(new Font("Serif", Font.BOLD, 30));
        title.setForeground(Color.WHITE);
        title.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        add(title, BorderLayout.NORTH);

        // Dummy Data
        String[] columns = {"Rank", "Player", "Level", "Result"};
        Object[][] data = {
            {"1", "Alice", "Level 4", "Won"},
            {"2", "Bob", "Level 2", "Lost"}
        };

        JTable table = new JTable(new DefaultTableModel(data, columns));
        add(new JScrollPane(table), BorderLayout.CENTER);

        // --- Back Button with Image ---
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
        
        JPanel bottom = new JPanel();
        bottom.setOpaque(false);
        bottom.add(backBtn);
        add(bottom, BorderLayout.SOUTH);
    }
}
