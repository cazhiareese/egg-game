package com.eggame.map;

import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

public class Obstacle {
    private Image image;
    private double width;
    private double height;
    private double positionX;
    private double positionY;
    private boolean collide;

    public Obstacle(boolean collide) {
        this.collide = collide;
    }

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

    public boolean getCollide() {
        return collide;
    }

    public void setImage(Image i) {
        image = i;
        width = i.getWidth();
        height = i.getHeight();
    }

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
