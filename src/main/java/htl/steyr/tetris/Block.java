package htl.steyr.tetris;

import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class Block extends Rectangle {

    public Block(double posX, double posY, double size, Color color) {
        setX(posX);
        setY(posY);
        setHeight(size);
        setWidth(size);

        setFill(color);
    }

    public void moveHorizontal(double amount) {
        double newX = getX() + amount;

        System.out.println("[Update X] " + newX);
        setX(newX);
    }

    public void moveVertical(double amount) {
        double newY = getY() + amount;

        System.out.println("[Update Y] " + newY);
        setY(newY);
    }




}
