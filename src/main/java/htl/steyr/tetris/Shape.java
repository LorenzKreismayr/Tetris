package htl.steyr.tetris;

import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Shape {
    private final List<Block> blocks = new ArrayList<>();
    private boolean updateBlocks = true;

    private List<Color> colors = List.of(
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
     *
     * @param curScore
     */
    public void update(int curScore) {
        if (!updateBlocks) {
            return;
        }

        for (Block block : blocks) {
            // move every block down by one,
            // increases exponentially dependent on the players score
            block.moveVertical(1 * (0.000002 * Math.pow(curScore, 2) - 0.001 * curScore + 1));
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
