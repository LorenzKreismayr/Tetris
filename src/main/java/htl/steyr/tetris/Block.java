package htl.steyr.tetris;

import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class Block extends Rectangle {
    private final double boundX;
    private final double boundY;

    public Block(double posX, double posY, double size, Color color) {
        setX(posX);
        setY(posY);
        setHeight(size);
        setWidth(size);
        setFill(color);

        // border
        setStroke(Color.WHITE);
        setStrokeWidth(1.0);

        // ja ka
        boundX = GameController.getInstance().gamePane.getPrefWidth();
        boundY = GameController.getInstance().gamePane.getPrefHeight();
    }

    public void moveHorizontal(double amount) {
        double newX = getX() + amount;

        if (newX >= 0 && (newX + getWidth()) <= boundX) {
            setX(newX);
        }
    }

    public void moveVertical(double amount) {
        double newY = getY() + amount;

        if ((newY + getHeight()) <= boundY) {
            setY(newY);
        }
    }
}
