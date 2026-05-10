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

    public Rectangle2D getCollisionBounds() {

        double padX = width * 0.05;
        double padY = height * 0.1;
        return new Rectangle2D(positionX + padX, positionY + padY, width - 2 * padX, height - 2 * padY);
    }

    public boolean intersects(Rectangle2D r) {
        return r.intersects(this.getCollisionBounds());
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
