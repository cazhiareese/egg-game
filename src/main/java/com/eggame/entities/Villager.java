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
    private int playerId = -1;

    private int headIndex = -1;
    private int hatIndex = -1;

    private Image headImage;
    private Image hatImage;

    public Villager(String name) {
        this.name = name;

        this.eggs = new EggTray(this);
        this.eggsCollected = this.eggs.getNumAllEggs();
        this.eggsReturned = 0;
        // Load directional sprites from classpath
        this.imageIdle = new Image(getClass().getResourceAsStream("/com/eggame/villager_idle.png"));
        this.imageUp = new Image(getClass().getResourceAsStream("/com/eggame/villager_up.png"));
        this.imageDown = new Image(getClass().getResourceAsStream("/com/eggame/villager_down.png"));
        this.imageLeft = new Image(getClass().getResourceAsStream("/com/eggame/villager_left.png"));
        this.imageRight = new Image(getClass().getResourceAsStream("/com/eggame/villager_right.png"));

        // Default to idle
        this.setImage(imageIdle);

        // Explicitly lock the graphic scaling footprint to standard frame size
        this.setSize(78, 97.2);
    }

    public void update(double time) {
        // Update sprite image based on movement direction
        updateDirectionalImage();
        super.update(time);
    }

    private void updateDirectionalImage() {
        double vx = getVelocityX();
        double vy = getVelocityY();

        if (vx == 0 && vy == 0) {
            setImage(imageIdle);
        } else if (Math.abs(vx) >= Math.abs(vy)) {
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
        if (headImage == null && hatImage == null) {
            super.render(gc);
        } else {
            // draw the customized avatar
            if (headImage != null) {
                gc.drawImage(headImage, getPositionX(), getPositionY() + 10, width, height);
            }
            if (hatImage != null) {
                double hatWidth = width * 0.9;
                double hatHeight = height * 0.5;
                gc.drawImage(hatImage, getPositionX() + (width - hatWidth) / 2, getPositionY() - hatHeight + 30,
                        hatWidth,
                        hatHeight);
            }
        }
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

    public void setEggsReturned(int count) {
        this.eggsReturned = count;
    }

    public void resetEggsReturned() {
        eggsReturned = 0;
        this.eggs.getEggs().clear();
    }

    public int getPlayerId() {
        return playerId;
    }

    public void setPlayerId(int playerId) {
        this.playerId = playerId;
    }

    public int getHeadIndex() {
        return headIndex;
    }

    public int getHatIndex() {
        return hatIndex;
    }

    // set avatar customization from customization menu
    public void setAvatar(int headIdx, int hatIdx) {
        this.headIndex = headIdx;
        this.hatIndex = hatIdx;

        try {
            var headStream = getClass().getResourceAsStream("/com/eggame/customization/head_" + headIdx + ".png");
            if (headStream != null) {
                this.headImage = new Image(headStream);
            }
        } catch (Throwable e) {

        }

        try {
            var hatStream = getClass().getResourceAsStream("/com/eggame/customization/hat_" + hatIdx + ".png");
            if (hatStream != null) {
                this.hatImage = new Image(hatStream);
            }
        } catch (Throwable e) {

        }
    }
}
