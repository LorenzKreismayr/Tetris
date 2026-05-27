package htl.steyr.tetris;

import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class GameController implements Initializable {
    public Pane gamePane;
    public Pane nextShapePane;
    public Label scoreLabel;

    private Thread gameLoop;
    private final List<Shape> shapes = new ArrayList<>();
    private Shape test;

    private boolean isRunning = false;

    // for collision checking we need to save the blocks in a 2D array
    private final Map<Integer, Integer> blockscoordinates = new HashMap<>();

    private final int BLOCKS_PER_ROW = 10;
    private double BLOCK_WIDTH;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        BLOCK_WIDTH = gamePane.getPrefWidth() / BLOCKS_PER_ROW;

        test = new Shape(ShapeType.SHAPE_I, BLOCK_WIDTH);
        gamePane.getChildren().addAll(test.getBlocks());

        gamePane.setStyle("-fx-border-color: black;");

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
        isRunning = true;

        gameLoop = new Thread(() -> {
            try {
                while (isRunning) {
                    updateShapes();
                    checkRows();
                    checkBorder();
                    if (checkCollision((int) test.getY(), (int) test.getX())) {
                        System.out.println("Collision detected!");
                    }
                    System.out.println("no Collision detected!");

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
        test.update();
    }

    /**
     * @todo check if the block collides with other blocks
     * A HashMap stores which blocks are located at which coordinates.
     * If a block reaches a coordinate that is already occupied by another block, a collision is detected.
     */
    public boolean checkCollision(int yofblock, int xofblock) {
        if (blockscoordinates.containsKey(xofblock) && blockscoordinates.containsValue(yofblock)) {
            return true;    //you are not allowed to move the block
        } else {
            return false;   //you can move the block
        }
    }

    /**
     * checks if a row is full and deletes it
     */
    private void checkRows() {

    }

    /**
     * Checks whether the current block touches the game borders.
     * Depending on the collision type, the block position is corrected:
     * - LEFT   → prevents the block from moving outside the left border
     * - RIGHT  → prevents the block from moving outside the right border
     * - BOTTOM → prevents the block from falling below the game area
     */
    private void checkBorder() {
        String collision = "";

        if (test.getX() < 0) {
            collision = "LEFT";
        } else if ((test.getX() + test.getWidth()) > gamePane.getWidth()) {
            collision = "RIGHT";
        } else if ((test.getY() + test.getHeight()) > gamePane.getHeight()) {
            collision = "BOTTOM";
        }

        switch (collision) {

            case "LEFT":
                test.setX(0);
                break;

            case "RIGHT":
                test.setX(gamePane.getWidth() - test.getWidth());
                break;

            case "BOTTOM":
                test.setY(gamePane.getHeight() - test.getHeight());
                break;
        }
    }
}
