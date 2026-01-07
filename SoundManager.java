import javax.sound.sampled.*;
import java.io.File;

public class SoundManager {
    private Clip backgroundMusic;

    // Singleton instance (optional, but good for music management)
    private static SoundManager instance;

    public static SoundManager getInstance() {
        if (instance == null) {
            instance = new SoundManager();
        }
        return instance;
    }

    /**
     * Plays a sound effect once (e.g., button click).
     */
    public void playSound(String filePath) {
        try {
            File soundFile = new File(filePath);
            if (soundFile.exists()) {
                AudioInputStream audioInput = AudioSystem.getAudioInputStream(soundFile);
                Clip clip = AudioSystem.getClip();
                clip.open(audioInput);
                clip.start();
            } else {
                System.out.println("Sound file not found: " + filePath);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Plays background music in a continuous loop.
     */
    public void playMusic(String filePath) {
        try {
            // Stop any existing music before starting new
            stopMusic();

            File musicFile = new File(filePath);
            if (musicFile.exists()) {
                AudioInputStream audioInput = AudioSystem.getAudioInputStream(musicFile);
                backgroundMusic = AudioSystem.getClip();
                backgroundMusic.open(audioInput);
                backgroundMusic.loop(Clip.LOOP_CONTINUOUSLY); // Loop forever
                backgroundMusic.start();
            } else {
                System.out.println("Music file not found: " + filePath);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stopMusic() {
        if (backgroundMusic != null && backgroundMusic.isRunning()) {
            backgroundMusic.stop();
            backgroundMusic.close();
        }
    }
}
