import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.io.*;
import javax.imageio.ImageIO;

public class LeaderboardPage extends BackgroundImagePanel {
    private MainInterface mainApp;
    private JTable table;
    private DefaultTableModel tableModel;
    private static final String FILE_NAME = "leaderboard.txt";
    private Font gummyFont;

    public LeaderboardPage(MainInterface app) {
        // 1. Background
        super("setup_bg.jpg");
        this.mainApp = app;
        setLayout(new BorderLayout());

        // Load Custom "Gummy" Font
        loadGummyFont();

        // --- Top Left: Back Button ---
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topBar.setOpaque(false);
        topBar.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 0));

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
            }
        } catch (Exception e) {
            backBtn.setText("Back");
        }
        backBtn.addActionListener(e -> {
            SoundManager.getInstance().playSound("click.wav");
            mainApp.showView("HOME");
        });
        topBar.add(backBtn);
        add(topBar, BorderLayout.NORTH);


        // --- Center Container (Title + Table) ---
        // Using GridBagLayout to center everything and keep them close
        JPanel centerContainer = new JPanel(new GridBagLayout());
        centerContainer.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 0, 5, 0); // Minimal vertical spacing (Sticky)

        // 2. Title Image (Replacing Text)
        JLabel titleLabel = new JLabel();
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        try {
            File titleFile = new File("leaderboard_title.png");
            if (titleFile.exists()) {
                ImageIcon titleIcon = new ImageIcon(ImageIO.read(titleFile));
                // Scale if necessary
                if (titleIcon.getIconWidth() > 400) {
                    Image scaled = titleIcon.getImage().getScaledInstance(400, -1, Image.SCALE_SMOOTH);
                    titleIcon = new ImageIcon(scaled);
                }
                titleLabel.setIcon(titleIcon);
            } else {
                // Fallback Text with Gummy Font
                titleLabel.setText("LEADERBOARD");
                titleLabel.setFont(gummyFont.deriveFont(Font.BOLD, 50f));
                titleLabel.setForeground(new Color(255, 215, 0)); // Gold
            }
        } catch (Exception e) {
            titleLabel.setText("LEADERBOARD");
        }

        // Add Title to Container
        gbc.gridy = 0;
        centerContainer.add(titleLabel, gbc);


        // 3. Table Setup
        String[] columns = {"Rank", "Player", "Level", "Result"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        table = new JTable(tableModel);
        table.setRowHeight(40);
        table.setFont(gummyFont.deriveFont(20f)); // Apply Gummy Font
        table.setFillsViewportHeight(true);
        table.setShowGrid(true); // Sharper look with grid lines
        table.setGridColor(new Color(0, 0, 0, 50)); // Subtle grid
        table.setIntercellSpacing(new Dimension(1, 1)); // Distinct cells

        // 4. Sharp & High Contrast Header
        JTableHeader header = table.getTableHeader();
        header.setFont(gummyFont.deriveFont(Font.BOLD, 22f));
        header.setBackground(Color.BLACK); // High Contrast Background
        header.setForeground(Color.WHITE); // High Contrast Text
        header.setPreferredSize(new Dimension(0, 50));
        header.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));

        // 5. Custom Renderer for "Nice" Rows & Rank 1 Contrast
        DefaultTableCellRenderer cellRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(JLabel.CENTER);
                
                // Bold text for sharpness
                setFont(gummyFont.deriveFont(Font.BOLD, 18f)); 

                if (isSelected) {
                    c.setBackground(new Color(50, 150, 255));
                    c.setForeground(Color.WHITE);
                } else {
                    // "Contrast between first row (Rank 1) and others"
                    if (row == 0) { 
                        // Rank 1: Gold Background, Black Text
                        c.setBackground(new Color(255, 223, 0)); 
                        c.setForeground(Color.BLACK);
                    } else if (row % 2 == 1) {
                        // Alternate Rows: Light Grey
                        c.setBackground(new Color(240, 240, 240, 230));
                        c.setForeground(Color.BLACK);
                    } else {
                        // Normal Rows: White
                        c.setBackground(new Color(255, 255, 255, 230));
                        c.setForeground(Color.BLACK);
                    }
                }
                return c;
            }
        };

        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(cellRenderer);
        }

        // Scroll Pane
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(700, 400)); // Controlled size
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.BLACK, 3)); // Sharp outer border
        scrollPane.getViewport().setBackground(new Color(255, 255, 255, 150)); // Transparent backing

        // Add Table to Container (right below title)
        gbc.gridy = 1;
        centerContainer.add(scrollPane, gbc);

        add(centerContainer, BorderLayout.CENTER);

        loadLeaderboard();
    }

    private void loadGummyFont() {
        try {
            // Try to load "Gummy.ttf" from file system or classpath
            File fontFile = new File("Gummy.ttf");
            if (fontFile.exists()) {
                gummyFont = Font.createFont(Font.TRUETYPE_FONT, fontFile);
                GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
                ge.registerFont(gummyFont);
            } else {
                // Fallback to "Comic Sans MS" if Gummy.ttf is missing
                gummyFont = new Font("Comic Sans MS", Font.PLAIN, 18);
            }
        } catch (Exception e) {
            // Ultimate fallback
            gummyFont = new Font("SansSerif", Font.PLAIN, 18);
        }
    }

    public void loadLeaderboard() {
        tableModel.setRowCount(0);
        File file = new File(FILE_NAME);
        if (!file.exists()) return;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            int rank = 1;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 3) {
                    tableModel.addRow(new Object[]{String.valueOf(rank++), parts[0], "Level " + parts[1], parts[2]});
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addEntry(String name, int level, String result) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_NAME, true))) {
            bw.write(name + "," + level + "," + result);
            bw.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        loadLeaderboard();
    }
}
