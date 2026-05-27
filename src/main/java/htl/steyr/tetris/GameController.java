package htl.steyr.tetris;

import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;

import java.net.URL;
import java.util.ResourceBundle;

public class GameController implements Initializable {
    public Pane gamePane;
    public Pane nextShapePane;
    public Label scoreLabel;

    private static final int CELL_SIZE = 40;

    private Thread gameLoop;
    private Block test;

    private boolean isRunning = false;

    private final int BLOCKS_PER_ROW = 10;
    private double BLOCK_WIDTH;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        BLOCK_WIDTH = gamePane.getPrefWidth() / BLOCKS_PER_ROW;

        test = new Block(0,0, BLOCK_WIDTH, Color.RED);
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

        createGridLines(gamePane, 400, 800);
        createGridLines(nextShapePane, 200, 200);

        // @todo add start button
        isRunning = true;
        startGameLoop();
    }

    private void createGridLines(Pane targetPane, int width, int height) {
        for (int x = CELL_SIZE; x < width; x += CELL_SIZE) {
            Line line = new Line(x, 0, x, height);
            line.setStroke(Color.web("#888888"));
            line.setStrokeWidth(0.5);
            targetPane.getChildren().add(line);
        }

        for (int y = CELL_SIZE; y < height; y += CELL_SIZE) {
            Line line = new Line(0, y, width, y);
            line.setStroke(Color.web("#888888"));
            line.setStrokeWidth(0.5);
            targetPane.getChildren().add(line);
        }
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

    /**
     * checks if a row is full and deletes it
     */
    private void checkRows() {

    }
}
