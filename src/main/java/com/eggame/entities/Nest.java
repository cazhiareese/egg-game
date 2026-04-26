package com.eggame.entities;

import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;

public class Nest extends Sprite {
    private int code;

    public Nest(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
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
}
