package com.eggame.rules;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

import com.eggame.entities.Egg;
import com.eggame.entities.Nest;
import com.eggame.entities.Villager;
import com.eggame.map.Farm;
import com.eggame.map.Obstacle;

import javafx.geometry.Rectangle2D;

public class Logic {

    public static void initRound(ArrayList<Nest> nests, ArrayList<Egg> eggs, Farm farm, int worldWidth,
            int worldHeight) {

        // initialize five nests
        Nest nest1 = new Nest(1);
        nest1.setPosition(118, 178);
        nest1.setImage("nest1.png");
        nests.add(nest1);

        Nest nest2 = new Nest(2);
        nest2.setPosition(1865, 280);
        nest2.setImage("nest2.png");
        nests.add(nest2);

        Nest nest3 = new Nest(3);
        nest3.setPosition(1316, 724);
        nest3.setImage("nest3.png");
        nests.add(nest3);

        Nest nest4 = new Nest(4);
        nest4.setPosition(1968, 1148);
        nest4.setImage("nest4.png");
        nests.add(nest4);

        Nest nest5 = new Nest(5);
        nest5.setPosition(444, 940);
        nest5.setImage("nest5.png");
        nests.add(nest5);

        // Use a fixed seed so server and all clients generate identical egg positions
        Random rand = new Random(42);

        for (int i = 0; i < 5; i++) {
            int numEggs = 4; // 5 nests * 4 eggs = exactly 20 eggs total
            for (int j = 0; j < numEggs; j++) {
                // create an egg for index i
                Egg egg = new Egg(nests.get(i));
                egg.setImage();

                boolean validLaunch = false;
                int safetyCounter = 500;

                while (!validLaunch && safetyCounter > 0) {
                    int eggX = (int) (rand.nextDouble() * (worldWidth - 100)) + 50;
                    int eggY = (int) (rand.nextDouble() * (worldHeight - 100)) + 50;
                    egg.setPosition(eggX, eggY);

                    Rectangle2D bounds = egg.getBounds();
                    boolean hitObstacle = false;

                    // Don't spawn underneath nests
                    for (Nest n : nests) {
                        if (n.getBounds().intersects(bounds)) {
                            hitObstacle = true;
                            break;
                        }
                    }

                    // Check Grid Obstacles
                    Obstacle[][] grid = farm.getObstacleGrid();
                    if (!hitObstacle && grid != null) {
                        for (int r = 0; r < grid.length && !hitObstacle; r++) {
                            for (int c = 0; c < grid[r].length && !hitObstacle; c++) {
                                if (grid[r][c] != null && grid[r][c].getCollide() && grid[r][c].intersects(bounds)) {
                                    hitObstacle = true;
                                }
                            }
                        }
                    }

                    // Check Solid Wall boundaries natively
                    if (!hitObstacle && farm.getHorizontalWallUpper() != null) {
                        for (Obstacle obs : farm.getHorizontalWallUpper())
                            if (obs.getCollide() && obs.intersects(bounds))
                                hitObstacle = true;
                    }
                    if (!hitObstacle && farm.getHorizontalWallLower() != null) {
                        for (Obstacle obs : farm.getHorizontalWallLower())
                            if (obs.getCollide() && obs.intersects(bounds))
                                hitObstacle = true;
                    }
                    if (!hitObstacle && farm.getVerticalWallLeft() != null) {
                        for (Obstacle obs : farm.getVerticalWallLeft())
                            if (obs.getCollide() && obs.intersects(bounds))
                                hitObstacle = true;
                    }
                    if (!hitObstacle && farm.getVerticalWallRight() != null) {
                        for (Obstacle obs : farm.getVerticalWallRight())
                            if (obs.getCollide() && obs.intersects(bounds))
                                hitObstacle = true;
                    }

                    for (Egg placed : eggs) {
                        if (!hitObstacle && Math.sqrt(
                                Math.pow(eggX - (placed.getPositionX() + placed.getBounds().getWidth() / 2), 2) +
                                        Math.pow(eggY - (placed.getPositionY() + placed.getBounds().getHeight() / 2),
                                                2)) < 150) {
                            hitObstacle = true;
                        }
                    }

                    if (!hitObstacle)
                        validLaunch = true;

                    safetyCounter--;
                }
                egg.setEggIndex(eggs.size());
                eggs.add(egg);
            }
        }
    }

    public static void update(double deltaTime, ArrayList<Villager> villagers, ArrayList<Egg> eggs,
            ArrayList<Nest> nests, Farm farm, ArrayList<String> input) {
        for (Villager player : villagers) {
            handleInput(deltaTime, player, input); // only relevant for local player
            checkEggPickup(player, eggs);
            checkNestDelivery(player, nests);
            checkCollisions(deltaTime, player, farm, nests);
        }
    }

    public static void checkCollisions(double deltaTime, Villager player, Farm farm,
            ArrayList<Nest> nests) {

        Rectangle2D bounds = player.getCollisionBounds(); // <--- Use properly tightened collision box!
        boolean collided = false;
        if (player.getPositionX() < 0 || player.getPositionY() < 0 ||
                player.getPositionX() + bounds.getWidth() > farm.getWidth() ||
                player.getPositionY() + bounds.getHeight() > farm.getHeight()) {
            collided = true;
        }

        if (!collided && nests != null) {
            for (Nest nest : nests) {
                if (nest.getCollisionBounds().intersects(bounds)) {
                    collided = true;
                    break;
                }
            }
        }

        Obstacle[][] grid = farm.getObstacleGrid();
        if (!collided && grid != null) {
            for (int r = 0; r < grid.length && !collided; r++) {
                for (int c = 0; c < grid[r].length && !collided; c++) {
                    if (grid[r][c] != null && grid[r][c].getCollide() && grid[r][c].intersects(bounds)) {
                        collided = true;
                    }
                }
            }
        }
        if (!collided && farm.getHorizontalWallUpper() != null) {
            for (Obstacle obs : farm.getHorizontalWallUpper())
                if (obs.getCollide() && obs.intersects(bounds))
                    collided = true;
        }
        if (!collided && farm.getHorizontalWallLower() != null) {
            for (Obstacle obs : farm.getHorizontalWallLower())
                if (obs.getCollide() && obs.intersects(bounds))
                    collided = true;
        }
        if (!collided && farm.getVerticalWallLeft() != null) {
            for (Obstacle obs : farm.getVerticalWallLeft())
                if (obs.getCollide() && obs.intersects(bounds))
                    collided = true;
        }
        if (!collided && farm.getVerticalWallRight() != null) {
            for (Obstacle obs : farm.getVerticalWallRight())
                if (obs.getCollide() && obs.intersects(bounds))
                    collided = true;
        }

        // Apply physical bounce-back block
        if (collided) {
            player.setPosition(
                    player.getPositionX() - player.getVelocityX() * deltaTime,
                    player.getPositionY() - player.getVelocityY() * deltaTime);
        }
    }

    public static void handleInput(double deltaTime, Villager player, ArrayList<String> input) {

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

        player.setVelocity(vx, vy);
        player.update(deltaTime);
    }

    private static void checkEggPickup(Villager player, ArrayList<Egg> eggs) {

        for (Egg currentEgg : eggs) {
            if (!currentEgg.isCollected() && player.intersects(currentEgg.getBounds())) {
                if (player.getEggTray().getNumAllEggs() < 5) {
                    System.out.println("[DEBUG] Collected egg from nest " + currentEgg.getFromNest() + " Egg count: "
                            + player.getEggTray().getNumAllEggs());
                    currentEgg.setCollected(true);
                    player.addEggs(currentEgg);
                }
            }
        }
    }

    private static void checkNestDelivery(Villager player, ArrayList<Nest> nests) {

        for (Nest nest : nests) {
            // Check if villager is touching this nest
            if (player.intersects(nest.getBounds())) {
                // Iterate through the villager's tray and deliver matching eggs
                Iterator<Egg> trayIter = player.getEggTray().getEggs().iterator();
                while (trayIter.hasNext()) {
                    Egg egg = trayIter.next();
                    if (egg.getFromNest() == nest.getCode()) {

                        trayIter.remove();
                        egg.setReturnedToNest(true);
                        player.addEggsReturned();
                        System.out.println("[DEBUG] Delivered egg to nest " + nest.getCode() + ". Total returned: "
                                + player.getEggsReturned() + " Egg count: "
                                + player.getEggTray().getNumAllEggs());
                    }
                }
            }
        }
    }

    public static boolean isRoundOver(ArrayList<Egg> eggs, ArrayList<Nest> nests, double remainingTime) {
        if (remainingTime < 0) {
            return true;
        }

        for (Egg egg : eggs) {
            if (!egg.isReturnedToNest()) {
                return false;
            }
        }
        return true;
    }

    public static void serverUpdate(double deltaTime, ArrayList<Villager> villagers,
            ArrayList<Egg> eggs, ArrayList<Nest> nests, Farm farm) {
        for (Villager player : villagers) {
            // No handleInput — server gets positions from network
            checkEggPickup(player, eggs);
            checkNestDelivery(player, nests);
            checkCollisions(deltaTime, player, farm, nests);
        }
    }

    // public static Villager getWinner(ArrayList<Villager> villagers) {
    // // TODO: Compare egg counts and return the winner (for implementatio in
    // multiplayer)

    // return null;
    // }
    // }
}
