package htl.steyr.tetris.music;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

public class Music {

    private static MediaPlayer currentPlayer;

    public static void play(String path) {

        stop();

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
        }
    }
}
