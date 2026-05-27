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
            case SHAPE_I:
                // .addAll doesn't work?
                blocks.add(new Block(0,0,blockSize,color));
                blocks.add(new Block(0,blockSize,blockSize,color));
                blocks.add(new Block(0,blockSize * 2,blockSize,color));
                blocks.add(new Block(0,blockSize * 3,blockSize,color));
                break;
            case SHAPE_BLOCK_4x4:
                blocks.add(new Block(0,0,blockSize,color));
                blocks.add(new Block(0,blockSize,blockSize,color));
                blocks.add(new Block(blockSize,0,blockSize,color));
                blocks.add(new Block(blockSize,blockSize,blockSize,color));
                break;
            case SHAPE_L:
                blocks.add(new Block(0,0,blockSize,color));
                blocks.add(new Block(0,blockSize,blockSize,color));
                blocks.add(new Block(0,blockSize * 2,blockSize,color));
                blocks.add(new Block(blockSize,blockSize * 2,blockSize,color));
                break;
            case SHAPE_STAIR_LEFT:
                blocks.add(new Block(0,0,blockSize,color));
                blocks.add(new Block(0,blockSize,blockSize,color));
                blocks.add(new Block(blockSize,blockSize,blockSize,color));
                blocks.add(new Block(blockSize,blockSize * 2,blockSize,color));
                break;
            case SHAPE_STAIR_RIGHT:
                blocks.add(new Block(blockSize,0,blockSize,color));
                blocks.add(new Block(blockSize,blockSize,blockSize,color));
                blocks.add(new Block(0,blockSize,blockSize,color));
                blocks.add(new Block(0,blockSize * 2,blockSize,color));
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
