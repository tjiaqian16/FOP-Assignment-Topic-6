import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;

public class LeaderboardPage extends BackgroundImagePanel {
    private MainInterface mainApp;
    private JTable table;
    private DefaultTableModel tableModel;
    private static final String FILE_NAME = "leaderboard.txt";

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
                ImageIcon icon = new ImageIcon(javax.imageio.ImageIO.read(imgFile));
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
        
        // Initialize Model with 0 rows
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table read-only
            }
        };

        table = new JTable(tableModel);
        table.setFillsViewportHeight(true);
        table.setRowHeight(30);
        table.setFont(new Font("SansSerif", Font.PLAIN, 16));
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 18));
        
        add(new JScrollPane(table), BorderLayout.CENTER);

        // Load data from file
        loadLeaderboard();
    }

    /**
     * Reads the leaderboard.txt file and populates the table.
     */
    public void loadLeaderboard() {
        tableModel.setRowCount(0); // Clear existing data
        File file = new File(FILE_NAME);
        
        if (!file.exists()) return;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            int rank = 1;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 3) {
                    // parts[0] = Name, parts[1] = Level, parts[2] = Result
                    tableModel.addRow(new Object[]{String.valueOf(rank++), parts[0], "Level " + parts[1], parts[2]});
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Adds a new entry to the leaderboard file and updates the table.
     */
    public void addEntry(String name, int level, String result) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_NAME, true))) {
            // Format: Name,Level,Result
            bw.write(name + "," + level + "," + result);
            bw.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Refresh the table view
        loadLeaderboard();
    }
}
