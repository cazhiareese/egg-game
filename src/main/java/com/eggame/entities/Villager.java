package com.eggame.entities;

import java.util.ArrayList;

import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

public class Villager extends Sprite {
    private String name;
    private int eggsCollected;
    private int eggsReturned;

    private EggTray eggs;

    // Directional sprites
    private Image imageUp;
    private Image imageDown;
    private Image imageLeft;
    private Image imageRight;
    private Image imageIdle;

    public Villager(String name) {
        this.name = name;
        this.eggs = new EggTray(this);
        this.eggsCollected = this.eggs.getNumAllEggs();
        this.eggsReturned = 0;
        // Load directional sprites
        this.imageIdle = new Image("villager_idle.png");
        this.imageUp = new Image("villager_up.png");
        this.imageDown = new Image("villager_down.png");
        this.imageLeft = new Image("villager_left.png");
        this.imageRight = new Image("villager_right.png");

        // Default to idle
        this.setImage(imageIdle);
    }

    public void update(double time) {
        // Update sprite image based on movement direction
        updateDirectionalImage();
        super.update(time);
    }

    /**
     * Swaps the sprite image based on current velocity direction.
     * Prioritizes horizontal over vertical when moving diagonally.
     */
    private void updateDirectionalImage() {
        double vx = getVelocityX();
        double vy = getVelocityY();

        if (vx == 0 && vy == 0) {
            setImage(imageIdle);
        } else if (Math.abs(vx) >= Math.abs(vy)) {
            // Horizontal movement takes priority
            if (vx > 0) {
                setImage(imageRight);
            } else {
                setImage(imageLeft);
            }
        } else {
            // Vertical movement
            if (vy > 0) {
                setImage(imageDown);
            } else {
                setImage(imageUp);
            }
        }
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

    public String getName() {
        return name;
    }

    public int getEggsColleced() {
        return this.eggs.getNumAllEggs();
    }

    public void addEggs(Egg e) {
        this.eggs.addEgg(e);
    }

    public EggTray getEggTray() {
        return this.eggs;
    }

    public void returnedEggs(Egg e) {
        this.eggs.removeEgg(e);
    }

    public void addVelocity(int i, int j) {
        super.addVelocity(i, j);
    }

    public int getEggsReturned() {
        return eggsReturned;
    }

    public void addEggsReturned() {
        eggsReturned++;
    }
}
