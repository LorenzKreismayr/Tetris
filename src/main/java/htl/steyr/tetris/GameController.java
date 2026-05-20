package htl.steyr.tetris;

import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

import java.net.URL;
import java.util.ResourceBundle;

public class GameController implements Initializable {
    public Pane gamePane;
    public Pane nextShapePane;
    public Label scoreLabel;

    private Thread gameLoop;
    private Block test;

    private boolean isRunning = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        test = new Block(0,0,10, Color.AQUAMARINE);
        gamePane.getChildren().add(test);

        gamePane.sceneProperty().addListener((observable, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
                    switch (event.getCode()) {
                        case A, LEFT:
                            System.out.println("right");
                            test.moveHorizontal(-10);
                            break;
                        case D, RIGHT:
                            System.out.println("left");
                            test.moveHorizontal(10);
                            break;
                    }
                });
            }
        });

        // @todo add start button
        startGameLoop();
    }

    public void startGameLoop() {
        gameLoop = new Thread(() -> {
            try {
                while (isRunning) {
                    updateShapes();
                    checkRows();

//                    // check if a block touches the top
//                    if () {
//                        // delete thread maybe?
//                        break;
//                    }

                    // ~60 updates / sek
                    Thread.sleep(16);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        gameLoop.start();
    }

    /**
     * calls the update function for all shapes
     */
    private void updateShapes() {

    }

    /**
     * checks if a row is full and deletes it
     */
    private void checkRows() {

    }
}
