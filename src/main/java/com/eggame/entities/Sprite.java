package com.eggame.entities;

import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

public class Sprite {
    private Image image;
    private double positionX;
    private double positionY;
    private double velocityX;
    private double velocityY;

    private double width;
    private double height;
    private boolean sizeLocked = false;

    public void setSize(double w, double h) {
        this.width = w;
        this.height = h;
        this.sizeLocked = true;
    }

    public void setImage(Image i) {
        image = i;
        if (!sizeLocked) {
            width = i.getWidth();
            height = i.getHeight();
        }
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

    public void setPosition(double x, double y) {
        positionX = x;
        positionY = y;

    }

    public void setVelocity(double x, double y) {
        velocityX = x;
        velocityY = y;
    }

    public void addVelocity(double x, double y) {
        velocityX += x;
        velocityY += y;
    }

    public void update(double time) {
        positionX += velocityX * time;
        positionY += velocityY * time;
    }

    public void render(GraphicsContext gc) {
        if (image != null) {
            gc.drawImage(image, positionX, positionY, width, height);
        } else {
            // Fallback: draw a colored rectangle if image is missing
            gc.setFill(Color.MAGENTA);
            gc.fillRect(positionX, positionY, width, height);
        }
    }

    public Rectangle2D getBounds() {
        return new Rectangle2D(positionX, positionY, width, height);
    }

    public Rectangle2D getCollisionBounds() {

        double hitWidth = width * 0.8;
        double hitHeight = height * 0.6;
        double offsetX = (width - hitWidth) / 2;
        double offsetY = height - hitHeight;
        return new Rectangle2D(positionX + offsetX, positionY + offsetY, hitWidth, hitHeight);
    }

    // check intersection with another sprite
    public boolean intersects(Rectangle2D r) {
        return r.intersects(this.getBounds());
    }

    public double getVelocityX() {
        return velocityX;
    }

    public double getVelocityY() {
        return velocityY;
    }

    public double getPositionX() {
        return positionX;
    }

    public double getPositionY() {
        return positionY;
    }

}
