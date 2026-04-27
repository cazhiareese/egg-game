package com.eggame.rules;

import java.util.ArrayList;
import java.util.Iterator;

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

        // initialize five nests
        Nest nest1 = new Nest(1);
        nest1.setPosition(50, 20);
        nest1.setImage("nest1.png");
        nests.add(nest1);

        Nest nest2 = new Nest(2);
        nest2.setPosition(880, 20);
        nest2.setImage("nest2.png");
        nests.add(nest2);

        Nest nest3 = new Nest(3);
        nest3.setPosition(960, 300);
        nest3.setImage("nest3.png");
        nests.add(nest3);

        Nest nest4 = new Nest(4);
        nest4.setPosition(380, 600);
        nest4.setImage("nest4.png");
        nests.add(nest4);

        Nest nest5 = new Nest(5);
        nest5.setPosition(580, 600);
        nest5.setImage("nest5.png");
        nests.add(nest5);

        for (int i = 0; i < 5; i++) {
            int numEggs = 4; // 5 nests * 4 eggs = exactly 20 eggs total
            for (int j = 0; j < numEggs; j++) {
                // get the nest at index i
                Egg egg = new Egg(nests.get(i));
                egg.setImage(); // Load image first

                // Offset by 50px so they don't clip outside the viewable canvas edges
                int eggX = (int) (Math.random() * (worldWidth - 100)) + 50;
                int eggY = (int) (Math.random() * (worldHeight - 100)) + 50;
                egg.setPosition(eggX, eggY);

                eggs.add(egg);
            }
        }
    }

    /**
     * Called every frame — handles all game rule updates.
     *
     * @param deltaTime seconds since last frame
     * @param villagers all players in the game
     * @param eggs      all eggs in the world
     * @param nests     all nests in the world
     * @param input     currently pressed keys
     */
    public static void update(double deltaTime, ArrayList<Villager> villagers, ArrayList<Egg> eggs,
            ArrayList<Nest> nests, ArrayList<String> input) {
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

        Villager currentPlayer = villagers.get(0);

        double vx = 0;
        double vy = 0;
        double speed = 250; // pixels per second

        if (input.contains("LEFT"))
            vx -= speed;
        if (input.contains("RIGHT"))
            vx += speed;
        if (input.contains("UP"))
            vy -= speed;
        if (input.contains("DOWN"))
            vy += speed;

        currentPlayer.setVelocity(vx, vy);
        currentPlayer.update(deltaTime);
    }

    /**
     * Checks if any villager is overlapping an uncollected egg
     * and picks it up if so.
     *
     * @param villagers all players
     * @param eggs      all eggs in the world
     */
    private static void checkEggPickup(ArrayList<Villager> villagers, ArrayList<Egg> eggs) {

        Villager currentPlayer = villagers.get(0);

        for (Egg currentEgg : eggs) {
            if (!currentEgg.isCollected() && currentPlayer.intersects(currentEgg.getBounds())) {
                System.out.println("[DEBUG] Collected egg from nest " + currentEgg.getFromNest());
                currentEgg.setCollected(true);
                currentPlayer.addEggs(currentEgg);
            }
        }
    }

    /**
     * Checks if any villager is overlapping a nest and delivers
     * matching eggs from their tray.
     *
     * @param villagers all players
     * @param nests     all nests in the world
     */
    private static void checkNestDelivery(ArrayList<Villager> villagers, ArrayList<Nest> nests) {

        Villager currentPlayer = villagers.get(0);

        for (Nest nest : nests) {
            // Check if villager is touching this nest
            if (currentPlayer.intersects(nest.getBounds())) {
                // Iterate through the villager's tray and deliver matching eggs
                Iterator<Egg> trayIter = currentPlayer.getEggTray().getEggs().iterator();
                while (trayIter.hasNext()) {
                    Egg egg = trayIter.next();
                    if (egg.getFromNest() == nest.getCode()) {
                        System.out.println("[DEBUG] Delivered egg to nest " + nest.getCode() + ". Total returned: "
                                + (currentPlayer.getEggsReturned() + 1));
                        trayIter.remove();
                        egg.setReturnedToNest(true);
                        currentPlayer.addEggsReturned();
                    }
                }
            }
        }
    }

    /**
     * Checks if the round is over (e.g., all eggs delivered or timer expired).
     *
     * @param eggs  all eggs in the world
     * @param nests all nests in the world
     * @return true if the round is finished
     */
    public static boolean isRoundOver(ArrayList<Egg> eggs, ArrayList<Nest> nests) {

        for (Egg egg : eggs) {
            if (!egg.isReturnedToNest()) {
                return false; // At least one egg hasn't been delivered yet
            }
        }
        return true;
    }

    /**
     * Determines the winner based on who delivered the most eggs.
     *
     * @param villagers all players
     * @return the villager with the highest score
     */
    // public static Villager getWinner(ArrayList<Villager> villagers) {
    // // TODO: Compare egg counts and return the winner (for implementatio in
    // multiplayer)

    // return null;
    // }
    // }
}
