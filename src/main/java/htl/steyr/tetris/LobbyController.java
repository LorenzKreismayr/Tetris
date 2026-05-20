package htl.steyr.tetris;

import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

public class LobbyController implements Initializable {

    public ImageView gameLogo;
    private MainController mainController;

    public void onStartGameButtonPressed(ActionEvent actionEvent) {
        mainController.loadContentView("game-view.fxml");
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        mainController = MainController.getInstance();


    }
}
