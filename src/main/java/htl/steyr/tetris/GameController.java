package htl.steyr.tetris;

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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.*;

public class GameController implements Initializable {

    // fix objects on the gamepane
    public Pane gamePane;
    public Pane nextShapePane;
    public Label scoreLabel;
    public Label gametimeLabel;
    public Button gameStateButton;
    public Pane heldShapePane;
    private Gametime gametime;

    // all objects that can be controlled
    private Shape activeShape;
    private Shape nextShape;
    private Shape heldShape;

    // this boolean prevents the ability to switch with the heldshape invinite times at once
    private volatile boolean isRunning = false;

    private Thread gameLoop;

    // for defining the grid on the gamepane (also for the blocks if they can be placed on certain positions)
    private static final int ROWS = 14;
    private static final int COLS = 10;
    private final Block[][] grid = new Block[ROWS][COLS];

    private double BLOCK_WIDTH;
    private int score = 0;

    private boolean isswitched = false;

    private static GameController instance;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        for(int row = 0; row < ROWS; row++) {
            for(int col = 0; col < COLS; col++) {
                grid[row][col] = null;
                System.out.println(grid[row][col]);
            }
        }
        instance = this;

        int BLOCKS_PER_ROW = 10;
        BLOCK_WIDTH = gamePane.getPrefWidth() / BLOCKS_PER_ROW;

        drawGrid(gamePane, ROWS, COLS);
        drawGrid(nextShapePane, 4, 4);
        drawGrid(heldShapePane, 4, 4);
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
                        case Q:
                            toggleHold();
                            isswitched = true;
                            break;
                        default: break;
                    }
                });
            }
        });


        System.out.println(gamePane.getPrefHeight() + " "+ gamePane.getPrefWidth());
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

                //Game Over:
                int finalScore = Integer.parseInt(Objects.equals(scoreLabel.getText(), "x") ? "0" : scoreLabel.getText());
                if (LoginController.score < finalScore) {
                    saveNewHighscore(finalScore, LoginController.username, LoginController.password);

                    LoginController.score = finalScore;

                    Platform.runLater(() -> {
                        MainController.getInstance().setDisplayData(LoginController.username, String.valueOf(finalScore));
                    });
                }


            } catch (InterruptedException | FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                gametime.stop();
            }
        });
        gameLoop.setDaemon(true);
        gameLoop.start();
    }

    private void saveNewHighscore(int score, String username, String password) throws IOException {
        File file = new File("userData.csv");
        if (!file.exists()) return;

        List<String> lines = new ArrayList<>();
        boolean userFound = false;

        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] parts = line.split(";");

                if (parts.length == 3 && parts[0].equalsIgnoreCase(username)) {
                    parts[2] = Integer.toString(score);

                    line = username + ";" + password + ";" + score;
                    userFound = true;
                }
                lines.add(line);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (!userFound) {
            System.out.println("User not found");
            return;
        }

        try (FileWriter writer = new FileWriter(file, false)) {
            for (String updatedLine : lines) {
                writer.write(updatedLine + "\n");
            }
        }
    }

    /**
     * Checks if the active shape can move down by 1px without
     * hitting the floor or an already placed block.
     */
    private boolean canMoveDown() {

        for (Block block : activeShape.getBlocks()) {
            double newBottom = block.getY() + BLOCK_WIDTH +10;

            // Floor check
            if (newBottom >= gamePane.getPrefHeight()) {
                for(Block blocks : activeShape.getBlocks()){
                    System.out.println(blocks.getY() +" + "+ BLOCK_WIDTH +"+ 1");
                }
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
        isswitched = false;

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
            nextShapePane.getChildren().remove(nextShape == null ? activeShape : nextShape);
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
                // don't show the next shape if the game is over
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
        try {
            if (activeShape == null) return;

            for (Block block : activeShape.getBlocks()) {
                double newX = block.getX() + amount;
                System.out.println("Trying to move block to X=" + newX);
                System.out.println("position" + (int) (newX / BLOCK_WIDTH) + " col" + (int) (block.getY() / BLOCK_WIDTH) + " row");
                System.out.println("in grid:" + grid[(int) (block.getY() / BLOCK_WIDTH)][(int) (newX / BLOCK_WIDTH)]);

                // Wall check
                if (newX < 0 || newX + BLOCK_WIDTH > gamePane.getPrefWidth()) {
                    return;
                }

                // Grid check
                int col = (int) (newX / BLOCK_WIDTH); // which column would the block enter?
                int row = (int) (block.getY() / BLOCK_WIDTH) + 1; // which row is the block currently in?

                if (row >= 0 && row < ROWS && col >= 0 && col < COLS) {
                    if (grid[row][col] != null) {
                        return;
                    }
                }
            }

            activeShape.moveHorizontal(amount);
        } catch (ArrayIndexOutOfBoundsException e) {
            // This can happen if the shape is partially outside the grid.
            // We can safely ignore it here since the wall checks should prevent it.
            System.out.println("Caught ArrayIndexOutOfBoundsException: " + e.getMessage());
            System.out.println("One block of the shape is outside the grid. This can happen during rotation near walls. Ignoring this exception.");
        }
    }

    /**
     * Checks if the active shape can move down by the given amount without
     * hitting the floor or a placed block. If all blocks can move, the shape is moved.
     *
     * @param amount --> describes the number of pixels (which is the BLOCK_WIDTH) the shape should move down
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
            int row = (int) (newY / BLOCK_WIDTH) +1; // which row would the block enter?
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
     * ether saves the active shape for later
     * ore switches the active shape for the one being hold
     */
    private void toggleHold() {
        if (activeShape == null || isswitched) {
            return;
        }

        if (heldShape == null) {
            gamePane.getChildren().removeAll(activeShape.getBlocks());

            heldShape = activeShape;
            activeShape = null;

            spawnShape();
            heldShapePane.getChildren().addAll(heldShape.getBlocks());
        } else {
            gamePane.getChildren().removeAll(activeShape.getBlocks());
            heldShapePane.getChildren().removeAll(heldShape.getBlocks());

            Shape tmp = heldShape;
            heldShape = activeShape;
            activeShape = tmp;

            gamePane.getChildren().addAll(activeShape.getBlocks());
            heldShapePane.getChildren().addAll(heldShape.getBlocks());

            // only here since spawnShape positions it itself
            double offsetX = BLOCK_WIDTH * 4;
            for (Block block : activeShape.getBlocks()) {
                block.setX(block.getX() + offsetX);
            }
        }

        // readjust position of held shape for display
        double hOffsetY = heldShape.getBlocks().get(0).getY();
        double hOffsetX = heldShape.getBlocks().get(0).getX();

        for (Block block : heldShape.getBlocks()) {
            // if a block is higher up
            if (block.getY() < hOffsetY) {
                hOffsetY = block.getY();
            }
            // if a block is more to the left
            if (block.getX() < hOffsetX) {
                hOffsetX = block.getX();
            }
        }

        for (Block block : heldShape.getBlocks()) {
            block.setY(block.getY() - hOffsetY);
            block.setX(block.getX() - hOffsetX + BLOCK_WIDTH);
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
     * The canvas is added as the first child of the game pane, so it stays
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

            // only start a new game loop if there isn't already one running
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
