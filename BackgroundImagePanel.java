import javax.swing.*;
import java.awt.*;
import java.net.URL;
import javax.imageio.ImageIO;
import java.io.IOException;

public class BackgroundImagePanel extends JPanel {
    private Image backgroundImage;

    public BackgroundImagePanel(String fileName) {
        try {
            // UPDATED: Use getResource to load from Classpath (works in JARs)
            URL imgUrl = getClass().getResource("/" + fileName);
            if (imgUrl != null) {
                backgroundImage = ImageIO.read(imgUrl);
            } else {
                // Fallback: try loading from file system if resource is null
                // (Useful if running directly from src folder in some IDEs)
                java.io.File f = new java.io.File(fileName);
                if (f.exists()) {
                     backgroundImage = ImageIO.read(f);
                } else {
                     System.out.println("Image not found: " + fileName);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        }
    }
}