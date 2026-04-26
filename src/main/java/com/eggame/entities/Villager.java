package com.eggame.entities;

import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;

public class Villager extends Sprite {
    private String name;
    private int eggsColleced;

    public Villager(String name) {
        this.name = name;
        this.eggsColleced = 0;

        this.setImage("villager.png");
    }

    public void update(double time) {
        super.update(time);
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

    public String getName() {
        return name;
    }

    public int getEggsColleced() {
        return eggsColleced;
    }

    public void setEggsColleced(int eggsColleced) {
        this.eggsColleced = eggsColleced;
    }

    public void updateEggsCollected() {
        this.eggsColleced += 1;
    }
}
