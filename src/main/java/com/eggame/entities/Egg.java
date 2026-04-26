package com.eggame.entities;

import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;

public class Egg extends Sprite {

    private int fromNest;
    private boolean collected;

    public Egg(Nest nest) {
        this.setImage("egg.png");
        this.fromNest = nest.getCode();
    }

    public void update(double deltaTime) {
        super.update(deltaTime);
    }

    public void render(GraphicsContext gc) {
        super.render(gc);
    }

    public Rectangle2D getBounds() {
        return super.getBounds();
    }

    public boolean intersects(Rectangle2D other) {
        return super.intersects(other);
    }

    public void setPosition(double x, double y) {
        super.setPosition(x, y);
    }

    public int getFromNest() {
        return fromNest;
    }

    public boolean isCollected() {
        return collected;
    }

    public void setCollected(boolean collected) {
        this.collected = collected;
    }

}
