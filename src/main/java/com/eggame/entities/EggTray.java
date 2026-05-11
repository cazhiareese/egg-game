package com.eggame.entities;

import java.util.ArrayList;

public class EggTray {
    private Villager owner;
    private ArrayList<Egg> eggs;

    public EggTray(Villager owner) {
        this.owner = owner;
        this.eggs = new ArrayList<>();
    }

    public void addEgg(Egg e) {
        this.eggs.add(e);
    }

    public void removeEgg(Egg e) {
        this.eggs.remove(e);
    }

    public int getNumAllEggs() {
        return this.eggs.size();
    }

    public Villager getOwner() {
        return this.owner;
    }

    public ArrayList<Egg> getEggs() {
        return this.eggs;
    }
}
