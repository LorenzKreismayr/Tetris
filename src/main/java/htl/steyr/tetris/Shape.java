package htl.steyr.tetris;

import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Shape {
    private final List<Block> blocks = new ArrayList<>();
    private boolean updateBlocks = true;

    private final List<Color> colors = List.of(
            new Color(0.72, 0.49, 0.04, 1.0),
            new Color(0.98, 0.44, 0.05, 1.0),
            new Color(0.89, 0.25, 0.11, 1.0),
            new Color(0.71, 0.12, 0.14, 1.0),
            new Color(0.51, 0.11, 0.31, 1.0),
            new Color(0.28, 0.16, 0.52, 1.0),
            new Color(0.18, 0.17, 0.49, 1.0),
            new Color(0.15, 0.18, 0.22, 1.0),
            new Color(0.09, 0.29, 0.65, 1.0),
            new Color(0.02, 0.35, 0.61, 1.0),
            new Color(0.04, 0.38, 0.39, 1.0),
            new Color(0.05, 0.31, 0.22, 1.0),
            new Color(0.13, 0.41, 0.19, 1.0),
            new Color(0.44, 0.44, 0.16, 1.0),
            new Color(0.98, 0.47, 0.05, 1.0)
    );

    public Shape(ShapeType type, double blockSize) {
        Color color = colors.get(new Random().nextInt(colors.size()));

        switch (type) {
            case SHAPE_I:
                blocks.add(new Block(0, 0, blockSize, color));
                blocks.add(new Block(0, blockSize, blockSize, color));
                blocks.add(new Block(0, blockSize * 2, blockSize, color));
                blocks.add(new Block(0, blockSize * 3, blockSize, color));
                break;
            case SHAPE_L:
                blocks.add(new Block(0, 0, blockSize, color));
                blocks.add(new Block(0, blockSize, blockSize, color));
                blocks.add(new Block(0, blockSize * 2, blockSize, color));
                blocks.add(new Block(blockSize, blockSize * 2, blockSize, color));
                break;
            case SHAPE_J:
                blocks.add(new Block(blockSize, 0, blockSize, color));
                blocks.add(new Block(blockSize, blockSize, blockSize, color));
                blocks.add(new Block(blockSize, blockSize * 2, blockSize, color));
                blocks.add(new Block(0, blockSize * 2, blockSize, color));
                break;
            case SHAPE_T:
                blocks.add(new Block(blockSize, 0, blockSize, color));
                blocks.add(new Block(blockSize, blockSize, blockSize, color));
                blocks.add(new Block(blockSize, blockSize * 2, blockSize, color));
                blocks.add(new Block(0, blockSize * 2, blockSize, color));
                blocks.add(new Block(blockSize * 2, blockSize * 2, blockSize, color));
                break;
            case SHAPE_BLOCK_4x4:
                blocks.add(new Block(0, 0, blockSize, color));
                blocks.add(new Block(0, blockSize, blockSize, color));
                blocks.add(new Block(blockSize, 0, blockSize, color));
                blocks.add(new Block(blockSize, blockSize, blockSize, color));
                break;
            case SHAPE_STAIR:
                blocks.add(new Block(0, 0, blockSize, color));
                blocks.add(new Block(0, blockSize, blockSize, color));
                blocks.add(new Block(blockSize, blockSize, blockSize, color));
                blocks.add(new Block(blockSize, blockSize * 2, blockSize, color));
                break;
            case SHAPE_STAIR_MIRRORED:
                blocks.add(new Block(blockSize, 0, blockSize, color));
                blocks.add(new Block(blockSize, blockSize, blockSize, color));
                blocks.add(new Block(0, blockSize, blockSize, color));
                blocks.add(new Block(0, blockSize * 2, blockSize, color));
                break;
        }
    }

    /**
     * Updates the position of the shape's blocks based on the current time.
     * @param curTime --> The current time in milliseconds, used to calculate the falling speed of the blocks.
     */
    public void update(int curTime) {
        if (!updateBlocks) {
            return;
        }

        for (Block block : blocks) {
            // move every block down by one,
            // increases exponentially dependent on the players score
            block.moveVertical(0.008333 * curTime + 1);
        }
    }


    public void stopmovement(){
        System.out.println("hier wird gestoppt");
        updateBlocks = false;
    }


    //move the shape left or right by the given amount
    public void moveHorizontal(double amount) {
        for (Block block : blocks) {
            // move every block left (-) or right (+)
            block.moveHorizontal(amount);
        }
    }

    // every block of the shape gets lowered by the given amount, which is the block width
    public void moveVertical(double blockWidth) {
        for (Block block : blocks) {
            // move every block down by one block width
            block.moveVertical(blockWidth);
        }
    }

    /**
     * Rotates the current shape by 90 degrees clockwise.
     * <p>
     * The second block of the shape is used as the rotation center.
     * Each block position is translated relative to the middle,
     * rotated, and then moved back to its new position.
     * <p>
     * Rotation formula:
     * (x, y) -> (-y, x)
     * <p>
     * This method currently does not check for:
     * - border collisions
     * - collisions with other shapes
     * - invalid wall rotations
     */
    public void rotateShape() {

        Block middle = blocks.get(1);

        double middleX = middle.getX();
        double middleY = middle.getY();

        for (Block block : blocks) {

            double relativeX = block.getX() - middleX;
            double relativeY = block.getY() - middleY;

            double rotatedX = -relativeY;
            double rotatedY = relativeX;

            block.setX(Math.round(middleX + rotatedX));
            block.setY(Math.round(middleY + rotatedY));
        }
    }

    public List<Block> getBlocks() {
        return blocks;
    }

    public boolean isUpdatetingBlocks() {
        return updateBlocks;
    }

    public void setUpdateBlocks(boolean value) {
        this.updateBlocks = value;
    }


}
