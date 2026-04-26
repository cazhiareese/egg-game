package com.eggame.rules;

import java.util.ArrayList;

import com.eggame.entities.Egg;
import com.eggame.entities.Nest;
import com.eggame.entities.Villager;

public class Logic {

    /**
     * Initializes a new round — spawns nests with random colors
     * and eggs at random coordinates that correspond to those nests.
     * There should be more eggs than nests.
     *
     * @param nests       the list to populate with spawned nests
     * @param eggs        the list to populate with spawned eggs
     * @param worldWidth  the width of the farm world
     * @param worldHeight the height of the farm world
     */
    public static void initRound(ArrayList<Nest> nests, ArrayList<Egg> eggs, int worldWidth, int worldHeight) {
        // TODO: Generate nests with random colors at fixed/random positions
        // TODO: Generate eggs at random coordinates, each linked to a nest
        // TODO: Ensure more eggs than nests
    }

    /**
     * Called every frame — handles all game rule updates.
     *
     * @param deltaTime  seconds since last frame
     * @param villagers  all players in the game
     * @param eggs       all eggs in the world
     * @param nests      all nests in the world
     * @param input      currently pressed keys
     */
    public static void update(double deltaTime, ArrayList<Villager> villagers, ArrayList<Egg> eggs, ArrayList<Nest> nests, ArrayList<String> input) {
        handleInput(deltaTime, villagers, input);
        checkEggPickup(villagers, eggs);
        checkNestDelivery(villagers, nests);
    }

    /**
     * Translates key input into villager movement (velocity).
     *
     * @param deltaTime seconds since last frame
     * @param villagers all players
     * @param input     currently pressed keys
     */
    private static void handleInput(double deltaTime, ArrayList<Villager> villagers, ArrayList<String> input) {
        // TODO: Map WASD/arrow keys to villager velocity
        // TODO: Handle multiple players if needed
    }

    /**
     * Checks if any villager is overlapping an uncollected egg
     * and picks it up if so.
     *
     * @param villagers all players
     * @param eggs      all eggs in the world
     */
    private static void checkEggPickup(ArrayList<Villager> villagers, ArrayList<Egg> eggs) {
        // TODO: For each villager, check intersection with each uncollected egg
        // TODO: If intersecting, mark egg as collected and add to villager's tray
    }

    /**
     * Checks if any villager is overlapping a nest and delivers
     * matching eggs from their tray.
     *
     * @param villagers all players
     * @param nests     all nests in the world
     */
    private static void checkNestDelivery(ArrayList<Villager> villagers, ArrayList<Nest> nests) {
        // TODO: For each villager near a nest, check tray for eggs matching nest color
        // TODO: If match found, remove egg from tray and score the delivery
    }

    /**
     * Checks if the round is over (e.g., all eggs delivered or timer expired).
     *
     * @param eggs  all eggs in the world
     * @param nests all nests in the world
     * @return true if the round is finished
     */
    public static boolean isRoundOver(ArrayList<Egg> eggs, ArrayList<Nest> nests) {
        // TODO: Define win/end condition (all eggs delivered, timer runs out, etc.)
        return false;
    }

    /**
     * Determines the winner based on who delivered the most eggs.
     *
     * @param villagers all players
     * @return the villager with the highest score
     */
    public static Villager getWinner(ArrayList<Villager> villagers) {
        // TODO: Compare egg counts and return the winner
        return null;
    }
}
