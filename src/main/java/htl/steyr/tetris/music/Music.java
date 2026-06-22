package htl.steyr.tetris.music;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

public class Music {

    private static MediaPlayer currentPlayer;
    private static boolean stopped = false;

    /**
     * Methode to start the music
     * @param path is for the path of the song files
     */
    public static void play(String path) {

        stop();
        stopped = false;

        Media media = new Media(
                Music.class.getResource(path).toExternalForm()
        );

        currentPlayer = new MediaPlayer(media);
        currentPlayer.setCycleCount(MediaPlayer.INDEFINITE);
        currentPlayer.play();
    }

    /**
     * Methode to stop the stop Song
     */
    public static void stop() {
        if (currentPlayer != null) {
            currentPlayer.stop();
            stopped = true;
        }
    }

    /**
     * Methode to pause the current song
     */
    public static void pause() {
        if (currentPlayer != null) {
            currentPlayer.pause();
            stopped = true;
        }
    }

    /**
     * Methode to continue the current Song
     */
    public static void resume() {
        if (currentPlayer != null && stopped) {
            currentPlayer.play();
            stopped = false;
        }
    }

    /**
     * Setter fot the volume of the music linked with the music slider
     * @param volume
     */
    public static void setVolume(double volume) {
        if (currentPlayer != null) {
            currentPlayer.setVolume(volume);
        }
    }

    /**
     * Getter fot the volume of the music linked with the music slider
     * Standard Value is 0,5
     */
    public static double getVolume() {
        if(currentPlayer != null){
            return currentPlayer.getVolume();
        }
        return 0.5;
    }
}
