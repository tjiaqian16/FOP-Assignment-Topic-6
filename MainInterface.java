import javax.swing.*;
import java.awt.*;
import java.io.File;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

public class MainInterface extends JFrame {
    private CardLayout cardLayout;
    private JPanel mainContainer;
    
    // Pages - Fields
    private GamePanel gamePanel;
    private SetupPage setupPage;
    private SettingsPage settingsPage;
    
    // Game State
    private int selectedMode = 1; // 1 = Human, 2 = Random, 3 = AI

    public MainInterface() {
        setTitle("Einstein Würfelt Nicht");
        
        // 1. Set Window Size
        try {
            BufferedImage bgImage = ImageIO.read(new File("menu_bg.jpg"));
            setSize(bgImage.getWidth(), bgImage.getHeight());
        } catch (Exception e) {
            System.out.println("Could not read background image for sizing. Using default.");
            setSize(1000, 700); 
        }

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center window
        setResizable(true); 

        // Layout Manager
        cardLayout = new CardLayout();
        mainContainer = new JPanel(cardLayout);

        // --- Initialize Pages ---
        LoadingPage loadingPage = new LoadingPage(); 
        HomePage home = new HomePage(this);
        PlaySelectionPage playSelection = new PlaySelectionPage(this);
        
        // CORRECTION: Initialize the class field, do not declare a new local 'SetupPage setupPage'
        this.setupPage = new SetupPage(this); 
        
        LeaderboardPage leaderboard = new LeaderboardPage(this);
        
        // Initialize other fields
        this.gamePanel = new GamePanel(this);
        this.settingsPage = new SettingsPage(this);

        // --- Add Pages to Layout ---
        mainContainer.add(loadingPage, "LOADING");
        mainContainer.add(home, "HOME");
        mainContainer.add(playSelection, "PLAY_SELECTION");
        mainContainer.add(setupPage, "SETUP");
        mainContainer.add(leaderboard, "LEADERBOARD");
        mainContainer.add(gamePanel, "GAME");
        mainContainer.add(settingsPage, "SETTINGS");

        add(mainContainer);
        
        // 2. Show Loading Page First
        cardLayout.show(mainContainer, "LOADING");
        setVisible(true);

        // 3. Timer to switch to HOME after 3 seconds (3000 ms)
        Timer timer = new Timer(3000, e -> showView("HOME"));
        timer.setRepeats(false); 
        timer.start();
    }

    // Switch Screens
    public void showView(String viewName) {
        cardLayout.show(mainContainer, viewName);
    }

    // Set Game Mode (1=Human, 2=Random, 3=AI)
    public void setGameMode(int mode) {
        this.selectedMode = mode;
    }

    public int getGameMode() {
        return selectedMode;
    }

    // Start the Game
    public void startGame(String playerName, int level) {
        gamePanel.startLevel(level, playerName);
        showView("GAME");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MainInterface::new);
    }
}
   
