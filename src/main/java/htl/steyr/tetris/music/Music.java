package htl.steyr.tetris.music;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

public class Music {

    private static MediaPlayer currentPlayer;
    private static boolean stopped = false;

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

    public static void stop() {
        if (currentPlayer != null) {
            currentPlayer.stop();
            stopped = true;
        }
    }

    public static void pause() {
        if (currentPlayer != null) {
            currentPlayer.pause();
            stopped = true;
        }
    }

    public static void resume() {
        if (currentPlayer != null && stopped) {
            currentPlayer.play();
            stopped = false;
        }
    }

    public static void setVolume(double volume) {
        if (currentPlayer != null) {
            currentPlayer.setVolume(volume);
        }
    }

    public static double getVolume() {
        if(currentPlayer != null){
            return currentPlayer.getVolume();
        }
        return 0.5;
    }
}
