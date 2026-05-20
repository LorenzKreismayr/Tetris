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

    // for collision checking we need to save the blocks in a 2D array
    private final Map<Integer, Integer> blockscoordinates = new HashMap<>();

    private final int BLOCKS_PER_ROW = 10;
    private double BLOCK_WIDTH;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        BLOCK_WIDTH = gamePane.getPrefWidth() / BLOCKS_PER_ROW;

        test = new Block(10,10, BLOCK_WIDTH, Color.RED);
        gamePane.getChildren().add(test);

        gamePane.sceneProperty().addListener((observable, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
                    switch (event.getCode()) {
                        case A, LEFT:
                            test.moveHorizontal(-BLOCK_WIDTH);
                            break;
                        case D, RIGHT:
                            test.moveHorizontal(BLOCK_WIDTH);
                            break;
                    }
                });
            }
        });

        // @todo add start button
        isRunning = true;
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
        // @todo move to shape class
        test.moveVertical(1);
    }

    public boolean checkCollision(int yofblock, int xofblock) {
        // @todo check if the block collides with other blocks or the walls
        if(!(blockscoordinates.containsKey(xofblock) && blockscoordinates.containsValue(yofblock))) {
            return true;    //you can move the block
        }else {
            return false;   //you are not allowed to move the block
        }
    }

    /**
     * checks if a row is full and deletes it
     */
    private void checkRows() {

    }
}
