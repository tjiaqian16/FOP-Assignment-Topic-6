import javax.swing.*;
import java.awt.*;
import java.io.File;
import javax.imageio.ImageIO;

public class LoadingPage extends JPanel {
    
    public LoadingPage() {
        setLayout(new GridBagLayout());
        // Light Cream/Orange tint background to match the theme
        setBackground(new Color(255, 245, 230)); 

        JLabel iconLabel = new JLabel();
        JLabel textLabel = new JLabel("Loading...");
        
        try {
            // Load the icon "loading_icon.png"
            File imgFile = new File("loading_icon.png");
            if (imgFile.exists()) {
                 ImageIcon icon = new ImageIcon(ImageIO.read(imgFile));
                 
                 // Optional: Resize if the image is too big (e.g., wider than 200px)
                 if (icon.getIconWidth() > 200) {
                     int newHeight = (200 * icon.getIconHeight()) / icon.getIconWidth();
                     Image scaled = icon.getImage().getScaledInstance(200, newHeight, Image.SCALE_SMOOTH);
                     icon = new ImageIcon(scaled);
                 }
                 iconLabel.setIcon(icon);
            } else {
                // Fallback text if image is missing
                iconLabel.setText("Icon Not Found");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Style the "Loading..." text
        textLabel.setFont(new Font("SansSerif", Font.BOLD | Font.ITALIC, 24));
        textLabel.setForeground(new Color(200, 100, 0)); // Dark Orange
        textLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0)); // Space between icon and text

        // Add components vertically
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; 
        gbc.gridy = 0;
        add(iconLabel, gbc);
        
        gbc.gridy = 1;
        add(textLabel, gbc);
    }
}