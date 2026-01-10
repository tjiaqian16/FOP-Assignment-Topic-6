import javax.swing.*;
import java.awt.*;
import java.io.File;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

public class MainInterface extends JFrame {
    private CardLayout cardLayout;
    private JPanel mainContainer;
    
    private GamePanel gamePanel;
    private SetupPage setupPage;
    private SettingsPage settingsPage;
    private LeaderboardPage leaderboardPage; 
    
    private int selectedMode = 1;

    public MainInterface() {
        setTitle("Einstein Würfelt Nicht - GUI");
        
        try {
            BufferedImage bgImage = ImageIO.read(new File("menu_bg.jpg"));
            setSize(bgImage.getWidth(), bgImage.getHeight());
        } catch (Exception e) {
            setSize(1000, 700); 
        }

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(true); 

        cardLayout = new CardLayout();
        mainContainer = new JPanel(cardLayout);

        // Initialize Pages
        LoadingPage loadingPage = new LoadingPage(); 
        HomePage home = new HomePage(this);
        PlaySelectionPage playSelection = new PlaySelectionPage(this);
        this.setupPage = new SetupPage(this); 
        this.leaderboardPage = new LeaderboardPage(this);
        this.gamePanel = new GamePanel(this);
        this.settingsPage = new SettingsPage(this);

        mainContainer.add(loadingPage, "LOADING");
        mainContainer.add(home, "HOME");
        mainContainer.add(playSelection, "PLAY_SELECTION");
        mainContainer.add(setupPage, "SETUP");
        mainContainer.add(leaderboardPage, "LEADERBOARD");
        mainContainer.add(gamePanel, "GAME");
        mainContainer.add(settingsPage, "SETTINGS");

        add(mainContainer);
        
        cardLayout.show(mainContainer, "LOADING");
        setVisible(true);

        Timer timer = new Timer(3000, e -> showView("HOME"));
        timer.setRepeats(false); 
        timer.start();
    }

    // UPDATED: Standard View Switcher
    public void showView(String viewName) {
        cardLayout.show(mainContainer, viewName);
        
        // --- ADDED: Sync Sound and Clear Name Logic ---
        if (viewName.equals("SETUP")) {
            setupPage.setVisible(true); // This will trigger the name clearing if you added it there
        }
    }

    public void setGameMode(int mode) { this.selectedMode = mode; }
    public int getGameMode() { return selectedMode; }

    public void startGame(String playerName, int level) {
        gamePanel.startLevel(level, playerName);
        showView("GAME");
    }

    public void recordGameResult(String playerName, int level, String result) {
        leaderboardPage.addEntry(playerName, level, result);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MainInterface::new);
    }
}