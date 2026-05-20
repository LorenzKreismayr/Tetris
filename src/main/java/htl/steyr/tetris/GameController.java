package htl.steyr.tetris;

import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

import java.net.URL;
import java.util.*;

public class GameController implements Initializable {
    public Pane gamePane;
    public Pane nextShapePane;
    public Label scoreLabel;

    private Thread gameLoop;
    private Block test;

    private boolean isRunning = false;

    // for collision checking we need to save the blocks in a 2D array
    private final Map<Integer, Integer> blockscoordinates = new HashMap<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        test = new Block(0,0,10, Color.AQUAMARINE);
        gamePane.getChildren().add(test);

        gamePane.setStyle("-fx-border-color: black;");

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
        isRunning = true;

        gameLoop = new Thread(() -> {
            try {
                while (isRunning) {
                    updateShapes();
                    checkRows();
                    checkBorder();

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

    private void checkBorder(){
        if (test.getX() < 0){
            test.setX(0);
        }

        if ((test.getX() + test.getWidth()) > gamePane.getWidth()){
            test.setX(gamePane.getWidth() - test.getWidth());
        }
    }
}
