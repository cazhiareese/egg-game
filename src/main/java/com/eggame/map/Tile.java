package com.eggame.map;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

public class Tile {
    private Image image;

    private double width;
    private double height;
    private double positionX;
    private double positionY;

    public Tile() {
    }

    public Tile(Image image) {
        setImage(image);
    }

    public void setPosition(double x, double y) {
        this.positionX = x;
        this.positionY = y;
    }

    public void setImage(Image i) {
        image = i;
        width = i.getWidth();
        height = i.getHeight();
    }

    // overload set image by passing file name (loads from classpath)
    public void setImage(String filename) {
        try {
            Image i = new Image(getClass().getResourceAsStream("/com/eggame/" + filename));
            setImage(i);
        } catch (Exception e) {

            width = 32;
            height = 32;
        }
    }

    public void render(GraphicsContext gc) {
        if (image != null) {
            gc.drawImage(image, positionX, positionY);
        }
    }
}
