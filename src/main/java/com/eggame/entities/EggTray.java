package com.eggame.entities;

import java.util.ArrayList;

public class EggTray {
    private Villager owner;
    private int numEggs;
    private ArrayList<Egg> eggs;

    public EggTray(Villager owner) {
        this.owner = owner;
        this.numEggs = 0;
        this.eggs = new ArrayList<>();
    }

    public void addEgg(Egg e) {
        this.eggs.add(e);
        this.numEggs += 1;
    }

    public void removeEgg(Egg e) {
        this.eggs.remove(e);
        this.numEggs -= 1;
    }

    public int getNumAllEggs() {
        return this.numEggs;
    }

    public Villager getOwner() {
        return this.owner;
    }
}
