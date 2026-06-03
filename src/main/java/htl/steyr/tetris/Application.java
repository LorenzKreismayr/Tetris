package htl.steyr.tetris;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

public class Application extends javafx.application.Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Application.class.getResource("main-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setResizable(false); // Das Fenster kann nicht in der Größe verändert werden
        stage.initStyle(StageStyle.UNDECORATED); //entfernt die Windows title bar (oberes band)
        stage.setTitle("Tetris!");
        stage.setScene(scene);
        stage.show();
    }
}
