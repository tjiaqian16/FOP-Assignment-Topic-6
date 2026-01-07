import javax.swing.*;
import java.awt.*;

public class MainInterface extends JFrame {
    private CardLayout cardLayout;
    private JPanel mainContainer;
    
    // Pages
    private GamePanel gamePanel;
    private SetupPage setupPage;
    
    // Game State
    private int selectedMode = 1; // 1 = Human, 2 = Random, 3 = AI

    public MainInterface() {
        setTitle("Einstein Würfelt Nicht");
        setSize(800, 900);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center window
        setResizable(false);

        // Layout Manager
        cardLayout = new CardLayout();
        mainContainer = new JPanel(cardLayout);

        // --- Initialize Pages ---
        // Pass 'this' to pages so they can call methods like showView()
        HomePage home = new HomePage(this);
        PlaySelectionPage playSelection = new PlaySelectionPage(this);
        setupPage = new SetupPage(this);
        LeaderboardPage leaderboard = new LeaderboardPage(this);
        gamePanel = new GamePanel(this); 

        // --- Add Pages to Layout ---
        mainContainer.add(home, "HOME");
        mainContainer.add(playSelection, "PLAY_SELECTION");
        mainContainer.add(setupPage, "SETUP");
        mainContainer.add(leaderboard, "LEADERBOARD");
        mainContainer.add(gamePanel, "GAME");

        add(mainContainer);
        setVisible(true);
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
