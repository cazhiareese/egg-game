package com.eggame.map;

import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;

public class Obstacle {
    private Image image;
    private double width;
    private double height;
    private double positionX;
    private double positionY;

    public Rectangle2D getBounds() {
        return new Rectangle2D(positionX, positionY, width, height);
    }

    // check intersection with another sprite
    public boolean intersects(Rectangle2D r) {
        return r.intersects(this.getBounds());
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
}
