package htl.steyr.tetris;

import htl.steyr.tetris.music.Music;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.ResourceBundle;

public class MainController implements Initializable {
    public HBox menuBarHBox;
    public AnchorPane contentPane;
    private static MainController instance;
    protected final ToggleGroup menuBarToggleGroup = new ToggleGroup();
    public Slider volumeSlider;

    public Label displayUsernameLabel;
    public Label displayHighscoreLabel;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        instance = this;

        // add buttons to toggleGroup
        for (Node node : menuBarHBox.getChildren()) {
            if (node instanceof ToggleButton tmp)
                if (!node.getId().equals("groupIgnore") && !node.getId().isEmpty()) {
                    tmp.setToggleGroup(menuBarToggleGroup);
                }
        }

        // prevent "untoggling" when clicking the toggled button
        menuBarToggleGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (newToggle == null) {
                menuBarToggleGroup.selectToggle(oldToggle);
            }
        });

        volumeSlider.setMin(0);
        volumeSlider.setMax(100);

        volumeSlider.setValue(Music.getVolume() * 100);

        volumeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            double volume = newVal.doubleValue() / 100.0;
            Music.setVolume(volume);
        });

        // load default site
        loadContentView("login-view.fxml");
    }

    /**
     * gets the name of the fxml file that should be loaded to the {@code contentPane} from the Button ID
     * and calls loadContentView with that value
     *
     * @param actionEvent clicked Button
     */
    public void onMenuBarButtonClicked(ActionEvent actionEvent) {
        ToggleButton src = (ToggleButton) actionEvent.getSource();
        String target = src.getId();

        if (!target.isEmpty()) {
            loadContentView(target);
        }
    }

    /**
     * loads a fxml file in {@code contentPane} and plays music if needed
     *
     * @param view fxml file in /com/frontend/view/content/
     */
    public void loadContentView(String view) {

        switch (view) {
            case "lobby-view.fxml":
                Music.play("/htl/steyr/tetris/songs/lobby.mp3");
                break;

            case "game-view.fxml":
                Music.play("/htl/steyr/tetris/songs/game.mp3");
                break;
        }

        loadView(contentPane, view, "MainController");
    }

    /**
     * loads a fxml file to a pane
     *
     * @param targetPane pane where the fxml file should be loaded
     * @param view       fxml file in /com/frontend/view/content/
     * @param src        name of the controller for debugging
     */
    public void loadView(AnchorPane targetPane, String view, String src) {
        try {
            FXMLLoader contentLoader = new FXMLLoader(getClass().getResource(getContentViewFolder() + view));
            Node tmp = contentLoader.load();

            AnchorPane.setTopAnchor(tmp, 0.0);
            AnchorPane.setLeftAnchor(tmp, 0.0);
            AnchorPane.setRightAnchor(tmp, 0.0);
            AnchorPane.setBottomAnchor(tmp, 0.0);

            // should prevent flickering over .clear();, .add();
            targetPane.getChildren().setAll(Collections.singleton(tmp));
            // return controller if still needed
            contentLoader.getController();
        } catch (IOException e) {
            System.out.printf("[%s] Could not find target fxml", src);
        }

    }

    public void setDisplayData(String username, String highscore) {
        displayUsernameLabel.setText("Username: " + username);

        if (highscore == null) {
            displayHighscoreLabel.setText("Highscore: 0");
        } else {
            displayHighscoreLabel.setText("Highscore: " + highscore);
        }
    }

    private String getContentViewFolder() {
        return "/htl/steyr/tetris/";
    }

    /**
     * if a loaded pane wants tho load something directly on the main-view
     *
     * @return {@code this}
     */
    public static MainController getInstance() {
        return instance;
    }

    public void onVolumeButtonClicked(ActionEvent actionEvent) {
        volumeSlider.setVisible(true);
    }

    public void onMusicPausedButtonClicked(ActionEvent actionEvent) {
        Music.pause();
    }

    public void onMusicStartButtonClicked(ActionEvent actionEvent) {
        Music.resume();
    }

    public void onexitButtonClicked(ActionEvent actionEvent) {
        System.exit(0);
    }
}