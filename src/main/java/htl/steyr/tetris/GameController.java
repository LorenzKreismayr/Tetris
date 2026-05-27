package htl.steyr.tetris;

import htl.steyr.tetris.gametime.Gametime;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;

import java.net.URL;
import java.util.*;

public class GameController implements Initializable {
    public Pane gamePane;
    public Pane nextShapePane;
    public Label scoreLabel;
    public Label gametimeLabel;
    private Gametime gametime;

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

        gametime = new Gametime(gametimeLabel);
        gametime.start();

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
                        //rotate the block with W or UP
                        case W, UP:
                            test.rotateShape();
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
                    for (Block block : test.getBlocks()) {

                        if (checkCollision((int) block.getY(), (int) block.getX())) {
                            System.out.println("Collision detected!");
                        }
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
            } finally {
                gametime.stop();
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

        for (Block block : test.getBlocks()) {


            if (block.getX() <0){
                collision = "LEFT";
            } else if ((block.getX() + block.getWidth()) > gamePane.getWidth()) {
                collision = "RIGHT";
            } else if ((block.getX() + block.getWidth()) > gamePane.getHeight()) {
                collision = "BOTTOM";
            }

            switch (collision) {

                case "LEFT":
                    block.setX(0);
                    break;

                case "RIGHT":
                    block.setX(gamePane.getWidth() - block.getWidth());
                    break;

                case "BOTTOM":
                    block.setY(gamePane.getHeight() - block.getHeight());
                    break;
            }
        }
    }
}
