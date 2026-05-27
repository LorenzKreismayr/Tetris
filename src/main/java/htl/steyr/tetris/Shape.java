package htl.steyr.tetris;

import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;

public class Shape {
    private final List<Block> blocks = new ArrayList<>();
    private boolean updateBlocks = true;

    public Shape(ShapeType type, double blockSize) {
        // @todo random color
        Color color = Color.RED;

        switch (type) {
            // .addAll doesn't work?
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

    public void update() {
        if (!updateBlocks) {
            return;
        }

        for (Block block : blocks) {
            // move every block down by one
            block.moveVertical(1);
        }
    }

    public void moveHorizontal(double amount) {
        for (Block block : blocks) {
            // move every block left (-) or right (+)
            block.moveHorizontal(amount);
        }
    }

    /**
     * Rotates the current shape by 90 degrees clockwise.
     *
     * The second block of the shape is used as the rotation center.
     * Each block position is translated relative to the middle,
     * rotated, and then moved back to its new position.
     *
     * Rotation formula:
     * (x, y) -> (-y, x)
     *
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

            block.setX(middleX + rotatedX);
            block.setY(middleY + rotatedY);
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
