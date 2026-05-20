package htl.steyr.tetris;

import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;

public class Shape {
    private List<Block> blocks = new ArrayList<>();

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
        }
    }

    public void update() {
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
}
