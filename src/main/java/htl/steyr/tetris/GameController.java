package htl.steyr.tetris;

import htl.steyr.tetris.gametime.Gametime;
import javafx.application.Platform;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;

import java.net.URL;
import java.util.Random;
import java.util.ResourceBundle;

public class GameController implements Initializable {
    public Pane gamePane;
    public Pane nextShapePane;
    public Label scoreLabel;
    public Label gametimeLabel;
    private Gametime gametime;

    private Thread gameLoop;
    private Shape activeShape;

    private volatile boolean isRunning = false;

    private static final int ROWS = 14;
    private static final int COLS = 10;
    private final Block[][] grid = new Block[ROWS][COLS];

    private final int BLOCKS_PER_ROW = 10;
    private double BLOCK_WIDTH;
    private int score = 1;

    private static GameController instance;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        instance = this;

        BLOCK_WIDTH = gamePane.getPrefWidth() / BLOCKS_PER_ROW;

        spawnShape();

        gamePane.setStyle("-fx-border-color: black;");

        gametime = new Gametime(gametimeLabel);
        gametime.start();

        gamePane.sceneProperty().addListener((observable, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
                    if (activeShape == null) return;
                    switch (event.getCode()) {
                        case A, LEFT:
                            moveShapeHorizontal(-BLOCK_WIDTH);
                            break;
                        case D, RIGHT:
                            moveShapeHorizontal(BLOCK_WIDTH);
                            break;
                        case W, UP:
                            rotateShapeWithCollision();
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
                    Platform.runLater(() -> {
                        if (activeShape == null || !activeShape.isUpdatetingBlocks()) return;

                        if (canMoveDown()) {
                            activeShape.update();
                        } else {
                            placeShape();
                            //checkAndClearRows();
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
     * Snaps the active shape to the grid and registers each block
     * in the placed-blocks grid. The blocks stay as children of the
     * pane (already rendered) but no longer move.
     */
    private void placeShape() {
        for (Block block : activeShape.getBlocks()) {
            // Snap to nearest grid position
            double snappedY = Math.round(block.getY() / BLOCK_WIDTH) * BLOCK_WIDTH;
            block.setY(snappedY);

            int row = (int) (snappedY / BLOCK_WIDTH);
            int col = (int) (block.getX() / BLOCK_WIDTH);

            if (row >= 0 && row < ROWS && col >= 0 && col < COLS) {
                grid[row][col] = block;
            }
        }

        activeShape.stopmovement();
        activeShape = null;
    }

    private void updateShapes() {
        test.update(score);
    }

    /**
     * Spawns a new random shape, centers it horizontally, and adds
     * its blocks to the game pane. If the new shape overlaps placed
     * blocks, the game is over.
     */
    private void spawnShape() {
        ShapeType[] types = ShapeType.values();
        ShapeType randomType = types[new Random().nextInt(types.length)];
        activeShape = new Shape(randomType, BLOCK_WIDTH);

        // Center the shape horizontally (around column 4)
        double offsetX = BLOCK_WIDTH * 4;
        for (Block block : activeShape.getBlocks()) {
            block.setX(block.getX() + offsetX);
        }

        // Check if the new shape overlaps any placed blocks (game over)
        for (Block block : activeShape.getBlocks()) {
            int row = (int) (block.getY() / BLOCK_WIDTH);
            int col = (int) (block.getX() / BLOCK_WIDTH);
            if (row >= 0 && row < ROWS && col >= 0 && col < COLS && grid[row][col] != null) {
                isRunning = false;
                return;
            }
        }

        gamePane.getChildren().addAll(activeShape.getBlocks());
    }

    /**
     * Checks if the active shape can move horizontally by the given
     * amount without hitting a wall or a placed block. If all blocks
     * can move, the shape is moved.
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
     * @ToDo: Rotates the shape and checks if the result is valid (no overlap
     *        with walls or placed blocks). If invalid, undoes the rotation
     *        by rotating 3 more times.
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
                }

                // Re-check the same row (rows shifted down into it)
                row++;
            }
        }
    }


    public static GameController getInstance() {
        return instance;
    }
}
