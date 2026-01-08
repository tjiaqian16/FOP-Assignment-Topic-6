import javax.sound.sampled.*;
import java.io.File;

public class SoundManager {
    private static SoundManager instance;
    private Clip backgroundMusic;
    
    // State flags
    private boolean musicEnabled = true;
    private boolean soundEnabled = true;
    private String currentMusicFile = "bgm.wav"; // Default track

    public static SoundManager getInstance() {
        if (instance == null) {
            instance = new SoundManager();
        }
        return instance;
    }

    public boolean isMusicEnabled() { return musicEnabled; }
    public boolean isSoundEnabled() { return soundEnabled; }

    /**
     * Toggles music on or off.
     * If turning ON, it attempts to play the last known track.
     * If turning OFF, it stops the current music.
     */
    public void setMusicEnabled(boolean enabled) {
        this.musicEnabled = enabled;
        if (enabled) {
            playMusic(currentMusicFile);
        } else {
            stopMusic();
        }
    }

    /**
     * Toggles sound effects (SFX) on or off.
     */
    public void setSoundEnabled(boolean enabled) {
        this.soundEnabled = enabled;
    }

    public void playSound(String filePath) {
        if (!soundEnabled) return; // Don't play if disabled

        try {
            File soundFile = new File(filePath);
            if (soundFile.exists()) {
                AudioInputStream audioInput = AudioSystem.getAudioInputStream(soundFile);
                Clip clip = AudioSystem.getClip();
                clip.open(audioInput);
                clip.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void playMusic(String filePath) {
        this.currentMusicFile = filePath; // Remember the track
        if (!musicEnabled) return; // Don't play if disabled

        try {
            // Only start if not already playing or if it's a new track
            if (backgroundMusic != null && backgroundMusic.isRunning()) {
                return; 
            }
            
            stopMusic(); // Ensure clean state

            File musicFile = new File(filePath);
            if (musicFile.exists()) {
                AudioInputStream audioInput = AudioSystem.getAudioInputStream(musicFile);
                backgroundMusic = AudioSystem.getClip();
                backgroundMusic.open(audioInput);
                backgroundMusic.loop(Clip.LOOP_CONTINUOUSLY);
                backgroundMusic.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stopMusic() {
        if (backgroundMusic != null) {
            backgroundMusic.stop();
            backgroundMusic.close();
        }
    }
}
