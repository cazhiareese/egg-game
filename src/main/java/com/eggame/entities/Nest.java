package com.eggame.entities;

import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;

public class Nest extends Sprite {
    private int code;
    private int eggsReturned = 0;

    public Nest(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public void setImage(String filename) {
        super.setImage(filename);
    }

    public void update(double deltaTime) {
        super.update(deltaTime);
    }

    public void render(GraphicsContext gc) {
        super.render(gc);
    }

    public void setPosition(double x, double y) {
        super.setPosition(x, y);
    }

    public Rectangle2D getBounds() {
        return super.getBounds();
    }

    public boolean intersects(Rectangle2D other) {
        return super.intersects(other);
    }

    public int getEggsReturned() {
        return eggsReturned;
    }

    public void addEggReturned() {
        eggsReturned++;
        updateImage();
    }

    public void setEggsReturned(int count) {
        this.eggsReturned = count;
        updateImage();
    }

    public void resetEggsReturned() {
        eggsReturned = 0;
        updateImage();
    }

    private void updateImage() {
        String filename = "nest" + code + "-" + eggsReturned + ".png";
        try {
            super.setImage(filename);
        } catch (Exception e) {
            // Fallback: if the specific file doesn't exist keep the current image
        }
    }

}
