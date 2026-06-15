package htl.steyr.tetris;

import com.sun.tools.javac.Main;
import htl.steyr.tetris.gametime.Gametime;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

import java.net.URL;
import java.util.Random;
import java.util.ResourceBundle;

public class GameController implements Initializable {
    public Pane gamePane;
    public Pane nextShapePane;
    public Label scoreLabel;
    public Label gametimeLabel;
    public Button gameStateButton;
    private Gametime gametime;

    private Shape activeShape;
    private Shape nextShape;

    private volatile boolean isRunning = false;

    private Thread gameLoop;
    private static final int ROWS = 14;
    private static final int COLS = 10;
    private final Block[][] grid = new Block[ROWS][COLS];

    private double BLOCK_WIDTH;
    private int score = 1;

    private static GameController instance;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        instance = this;

        int BLOCKS_PER_ROW = 10;
        BLOCK_WIDTH = gamePane.getPrefWidth() / BLOCKS_PER_ROW;

        drawGrid(gamePane, ROWS, COLS);
        drawGrid(nextShapePane, 4, 4);
        spawnShape();

        gametime = new Gametime(gametimeLabel);

        gamePane.sceneProperty().addListener((observable, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
                    if (!isRunning) return;
                    if (activeShape == null) return;
                    switch (event.getCode()) {
                        case A, LEFT:   // move every block of the shape to the left by 1 block width (if possible)
                            moveShapeHorizontal(-BLOCK_WIDTH);
                            break;
                        case D, RIGHT:  // move every block of the shape to the right by 1 block width (if possible)
                            moveShapeHorizontal(BLOCK_WIDTH);
                            break;
                        case S, DOWN:   // move every block of the shape down by 1 block width (if possible)
                            moveShapeDown(BLOCK_WIDTH);
                            break;
                        case W, UP:     // rotates the shape 90 degrees clockwise (if possible)
                            rotateShapeWithCollision();
                            break;
                        default: break;
                    }
                });
            }
        });
    }

    public void startGameLoop() {
        //checkAndClearRows();
        // ~60 updates / sek
        gameLoop = new Thread(() -> {
            try {
                while (isRunning) {
                    Platform.runLater(() -> {
                        if (activeShape == null || !activeShape.isUpdatetingBlocks()) return;

                        if (canMoveDown()) {
                            activeShape.update(gametime.getTotalSeconds());
                        } else {
                            placeShape();
                            checkAndClearRows();
                            spawnShape();
                        }
                    });

                    // ~60 updates / sek
                    Thread.sleep(16);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                gametime.stop();
            }
        });
        gameLoop.setDaemon(true);
        gameLoop.start();
    }

    /**
     * Checks if the active shape can move down by 1px without
     * hitting the floor or an already placed block.
     */
    private boolean canMoveDown() {
        for (Block block : activeShape.getBlocks()) {
            double newBottom = block.getY() + BLOCK_WIDTH + 1;

            // Floor check
            if (newBottom > gamePane.getPrefHeight()) {
                return false;
            }

            // Grid check: which row would the bottom edge enter?
            int rowBelow = (int) (newBottom / BLOCK_WIDTH);
            int col = (int) (block.getX() / BLOCK_WIDTH);

            if (rowBelow >= 0 && rowBelow < ROWS && col >= 0 && col < COLS) {
                if (grid[rowBelow][col] != null) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * <p>Snaps the active shape to the grid and registers each block
     * in the placed-blocks grid. The blocks stay as children of the
     * pane (already rendered) but no longer move. </p>
     */
    private void placeShape() {
        for (Block block : activeShape.getBlocks()) {
            // Snap to nearest grid position
            double snappedY = Math.round(block.getY() / BLOCK_WIDTH) * BLOCK_WIDTH;
            double snappedX = Math.round(block.getX() / BLOCK_WIDTH) * BLOCK_WIDTH;
            block.setX(snappedX);
            block.setY(snappedY);

            int row = (int) (snappedY / BLOCK_WIDTH);
            int col = (int) (snappedX / BLOCK_WIDTH);

            if (row >= 0 && row < ROWS && col >= 0 && col < COLS) {
                grid[row][col] = block;
            }
        }

        activeShape.stopmovement();
        activeShape = null;
    }

    /**
     * <p>Spawns a new random shape, centers it horizontally, and adds
     * its blocks to the game pane. If the new shape overlaps placed
     * blocks, the game is over. </p>
     */
    private void spawnShape() {
        ShapeType[] types = ShapeType.values();
        ShapeType randomType = types[new Random().nextInt(types.length)];

        // generate two shapes at start
        if (nextShape == null) {
            activeShape = new Shape(randomType, BLOCK_WIDTH);
            nextShape = new Shape(types[new Random().nextInt(types.length)], BLOCK_WIDTH);
        } else {
            // set next active and generate one shape
            activeShape = nextShape;
            nextShapePane.getChildren().remove(nextShape);
            nextShape = new Shape(randomType, BLOCK_WIDTH);
        }

        // Center the shape horizontally (around column 4)
        double offsetX = BLOCK_WIDTH * 4;
        for (Block block : activeShape.getBlocks()) {
            block.setX(block.getX() + offsetX);
        }

        // center nextShape horizontally
        double nOffsetX = BLOCK_WIDTH;
        for (Block block : nextShape.getBlocks()) {
            block.setX(block.getX() + nOffsetX);
        }

        // Check if the new shape overlaps any placed blocks (game over)
        for (Block block : activeShape.getBlocks()) {
            int row = (int) (block.getY() / BLOCK_WIDTH);
            int col = (int) (block.getX() / BLOCK_WIDTH);
            if (row >= 0 && row < ROWS && col >= 0 && col < COLS && grid[row][col] != null) {
                isRunning = false;
                // dont show the next shape if the game is over
                // because it gets placed incorrect
                nextShapePane.getChildren().clear();
                return;
            }
        }

        // active shape
        gamePane.getChildren().addAll(activeShape.getBlocks());
        // next shape preview
        nextShapePane.getChildren().addAll(nextShape.getBlocks());
    }

    /**
     * <p> Checks if {@code activeShape} can move horizontally by the given
     * amount without hitting a wall or a placed block. If all blocks
     * can move, the shape is moved. </p>
     */
    private void moveShapeHorizontal(double amount) {
        if (activeShape == null) return;

        for (Block block : activeShape.getBlocks()) {
            double newX = block.getX() + amount;

            // Wall check
            if (newX < 0 || newX + BLOCK_WIDTH > gamePane.getPrefWidth()) {
                return;
            }

            // Grid check
            int col = (int) (newX / BLOCK_WIDTH);
            int row = (int) (block.getY() / BLOCK_WIDTH);

            if (row >= 0 && row < ROWS && col >= 0 && col < COLS) {
                if (grid[row][col] != null) {
                    return;
                }
            }
        }

        activeShape.moveHorizontal(amount);
    }

    /**
     * Checks if the active shape can move down by the given amount without
     * hitting the floor or a placed block. If all blocks can move, the shape is moved.
     *
     * @param amount --> describes the amount of pixels (which is the BLOCK_WIDTH) the shape should move down
     */
    public void moveShapeDown(double amount) {
        if (activeShape == null) return;

        for (Block block : activeShape.getBlocks()) {
            double newY = block.getY() + amount;

            // Wall check
            if (newY < 0 || newY + BLOCK_WIDTH > gamePane.getPrefHeight()) {
                return;
            }

            // Grid check
            int row = (int) (newY / BLOCK_WIDTH);
            int col = (int) (block.getX() / BLOCK_WIDTH);

            if (row >= 0 && row < ROWS && col >= 0 && col < COLS) {
                if (grid[row][col] != null) {
                    return;
                }
            }
        }

        activeShape.moveVertical(amount);
    }


    /**
     * Rotates the shape and checks if the result is valid (no overlap
     * with walls or placed blocks). If invalid, undoes the rotation
     * by rotating 3 more times.
     */

    private void rotateShapeWithCollision() {
        if (activeShape == null) return;

        activeShape.rotateShape();

        boolean valid = true;
        for (Block block : activeShape.getBlocks()) {
            double x = block.getX();
            double y = block.getY();

            // Wall / floor check
            if (x < 0 || x + BLOCK_WIDTH > gamePane.getPrefWidth()
                    || y < 0 || y + BLOCK_WIDTH > gamePane.getPrefHeight()) {
                valid = false;
                break;
            }

            // Grid check
            int row = (int) (y / BLOCK_WIDTH);
            int col = (int) (x / BLOCK_WIDTH);
            if (row >= 0 && row < ROWS && col >= 0 && col < COLS) {
                if (grid[row][col] != null) {
                    valid = false;
                    break;
                }
            }
        }

        if (!valid) {
            // Undo: rotate 3 more times = 360 total = back to original
            activeShape.rotateShape();
            activeShape.rotateShape();
            activeShape.rotateShape();
        }
    }


    /**
     * Checks all rows from bottom to top. Full rows are cleared:
     * blocks are removed from the pane, and all rows above shift down.
     */
    private void checkAndClearRows() {
        for (int row = ROWS - 1; row >= 0; row--) {
            boolean full = true;
            for (int col = 0; col < COLS; col++) {
                if (grid[row][col] == null) {
                    full = false;
                    break;
                }
            }

            if (full) {
                // Remove full row blocks from pane
                for (int col = 0; col < COLS; col++) {
                    gamePane.getChildren().remove(grid[row][col]);
                    grid[row][col] = null;
                }

                // Shift all rows above down by one
                for (int r = row; r > 0; r--) {
                    for (int col = 0; col < COLS; col++) {
                        grid[r][col] = grid[r - 1][col];
                        if (grid[r][col] != null) {
                            grid[r][col].setY(r * BLOCK_WIDTH);
                        }
                    }
                }

                // Clear the top row
                for (int col = 0; col < COLS; col++) {
                    grid[0][col] = null;
                    // update score for each block removed
                    // increase/decrease affects falling speed
                    int BLOCK_VAL = 30;
                    score += BLOCK_VAL;
                    scoreLabel.setText(Integer.toString(score));
                }

                // Re-check the same row (rows shifted down into it)
                row++;
            }
        }
    }

    /**
     * Draws a grid on the game pane to visualize the Tetris playing field.
     * The canvas is added as the first child of the game pane so it stays
     * behind all blocks.
     */
    private void drawGrid(Pane target, int rows, int cols) {
        Canvas canvas = new Canvas(target.getPrefWidth(), target.getPrefHeight());
        GraphicsContext gc = canvas.getGraphicsContext2D();

        //white lines
        gc.setStroke(Color.rgb(255, 255, 255, 1));
        gc.setLineWidth(0.5);

        //vertical lines
        for (int col = 0; col <= cols; col++) {
            gc.strokeLine(col * BLOCK_WIDTH, 0, col * BLOCK_WIDTH, target.getPrefHeight());
        }

        //horizontal lines
        for (int row = 0; row <= rows; row++) {
            gc.strokeLine(0, row * BLOCK_WIDTH, target.getPrefWidth(), row * BLOCK_WIDTH);
        }

        //add behind all blocks
        target.getChildren().add(0, canvas);
    }


    public static GameController getInstance() {
        return instance;
    }

    public void onGameStateButtonClicked(ActionEvent actionEvent) {
        if (!isRunning) {
            isRunning = true;
            gametime.start();
            gameStateButton.setText("Pause");

            // Nur starten wenn kein Thread läuft
            if (gameLoop == null || !gameLoop.isAlive()) {
                startGameLoop();
            }
        } else {
            isRunning = false;
            gametime.stop();
            gameStateButton.setText("Continue");
        }
    }
}
