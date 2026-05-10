package com.eggame.entities;

import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;

public class Egg extends Sprite {

    private int fromNest;
    private boolean collected;
    private boolean returnedToNest;

    public Egg(Nest nest) {
        this.fromNest = nest.getCode();
    }

    public void setImage() {
        switch (this.fromNest) {
            case 1:
                super.setImage("egg1.png");
                break;
            case 2:
                super.setImage("egg2.png");
                break;
            case 3:
                super.setImage("egg3.png");
                break;
            case 4:
                super.setImage("egg4.png");
                break;
            case 5:
                super.setImage("egg5.png");
                break;
        }
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

    public boolean isReturnedToNest() {

        return returnedToNest;
    }

    public void setReturnedToNest(boolean returnedToNest) {
        this.returnedToNest = returnedToNest;
    }

    public void setCollected(boolean collected) {
        this.collected = collected;
    }

}
