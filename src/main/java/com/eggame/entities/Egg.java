package com.eggame.entities;

public class Egg extends Sprite {

    private int fromNest;
    private boolean collected;

    public Egg(Nest nest) {
        this.setImage("egg.png");
        this.fromNest = nest.getCode();
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
