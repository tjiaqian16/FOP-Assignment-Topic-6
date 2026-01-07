import javax.swing.*;
import java.awt.*;
import java.io.File;
import javax.imageio.ImageIO;
import java.io.IOException;

public class BackgroundImagePanel extends JPanel {
    private Image backgroundImage;

    public BackgroundImagePanel(String filePath) {
        try {
            // Load the image from the file
            backgroundImage = ImageIO.read(new File(filePath));
        } catch (IOException e) {
            System.out.println("Background image not found: " + filePath);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Draw the image scaled to fit the entire panel
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        }
    }
}