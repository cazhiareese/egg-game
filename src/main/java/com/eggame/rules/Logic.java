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
        nest1.setImage("nest1-0.png");
        nests.add(nest1);

        Nest nest2 = new Nest(2);
        nest2.setPosition(1865, 280);
        nest2.setImage("nest2-0.png");
        nests.add(nest2);

        Nest nest3 = new Nest(3);
        nest3.setPosition(1316, 724);
        nest3.setImage("nest3-0.png");
        nests.add(nest3);

        Nest nest4 = new Nest(4);
        nest4.setPosition(1968, 1148);
        nest4.setImage("nest4-0.png");
        nests.add(nest4);

        Nest nest5 = new Nest(5);
        nest5.setPosition(444, 940);
        nest5.setImage("nest5-0.png");
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

    public static double[] findSafeSpawn(double preferredX, double preferredY, ArrayList<Nest> nests, Farm farm) {

        // Approximate villager collision footprint (matches Sprite.getCollisionBounds)
        double playerW = 78 * 0.8;
        double playerH = 97.2 * 0.6;

        // Try increasingly larger offsets around the preferred point
        double step = 120; // pixels per step
        int maxRings = 15;

        for (int ring = 0; ring <= maxRings; ring++) {
            // Number of candidates along each side of this ring
            int side = ring == 0 ? 1 : ring * 4;
            for (int s = 0; s < side; s++) {
                double angle = (2 * Math.PI * s) / side;
                double cx = preferredX + Math.cos(angle) * ring * step;
                double cy = preferredY + Math.sin(angle) * ring * step;

                // Clamp to world boundaries (leave a 50px margin)
                if (cx < 50 || cy < 50
                        || cx + playerW > farm.getWidth() - 50
                        || cy + playerH > farm.getHeight() - 50) {
                    continue;
                }

                Rectangle2D bounds = new Rectangle2D(cx, cy, playerW, playerH);
                boolean blocked = false;

                // Check nests
                if (nests != null) {
                    for (Nest n : nests) {
                        if (n.getBounds().intersects(bounds)) {
                            blocked = true;
                            break;
                        }
                    }
                }

                // Check obstacle grid
                Obstacle[][] grid = farm.getObstacleGrid();
                if (!blocked && grid != null) {
                    for (int r = 0; r < grid.length && !blocked; r++) {
                        for (int c = 0; c < grid[r].length && !blocked; c++) {
                            if (grid[r][c] != null && grid[r][c].getCollide()
                                    && grid[r][c].intersects(bounds)) {
                                blocked = true;
                            }
                        }
                    }
                }

                // Check walls
                if (!blocked && farm.getHorizontalWallUpper() != null) {
                    for (Obstacle obs : farm.getHorizontalWallUpper())
                        if (obs.getCollide() && obs.intersects(bounds))
                            blocked = true;
                }
                if (!blocked && farm.getHorizontalWallLower() != null) {
                    for (Obstacle obs : farm.getHorizontalWallLower())
                        if (obs.getCollide() && obs.intersects(bounds))
                            blocked = true;
                }
                if (!blocked && farm.getVerticalWallLeft() != null) {
                    for (Obstacle obs : farm.getVerticalWallLeft())
                        if (obs.getCollide() && obs.intersects(bounds))
                            blocked = true;
                }
                if (!blocked && farm.getVerticalWallRight() != null) {
                    for (Obstacle obs : farm.getVerticalWallRight())
                        if (obs.getCollide() && obs.intersects(bounds))
                            blocked = true;
                }

                if (!blocked) {
                    return new double[] { cx, cy };
                }
            }
        }

        // Fallback — return the preferred position if nothing clear was found
        return new double[] { preferredX, preferredY };
    }

    public static void checkPlayerCollisions(Villager player, ArrayList<Villager> villagers) {
        Rectangle2D myBounds = player.getCollisionBounds();

        for (Villager other : villagers) {
            // Skip self and placeholder "Remote" entries that haven't connected yet
            if (other == player || "Remote".equals(other.getName())) {
                continue;
            }

            Rectangle2D otherBounds = other.getCollisionBounds();

            if (!myBounds.intersects(otherBounds)) {
                continue;
            }

            // Centre of each collision box
            double myCx = myBounds.getMinX() + myBounds.getWidth() / 2;
            double myCy = myBounds.getMinY() + myBounds.getHeight() / 2;
            double otCx = otherBounds.getMinX() + otherBounds.getWidth() / 2;
            double otCy = otherBounds.getMinY() + otherBounds.getHeight() / 2;

            // Compute overlap on each axis
            double overlapX = (myBounds.getWidth() + otherBounds.getWidth()) / 2
                    - Math.abs(myCx - otCx);
            double overlapY = (myBounds.getHeight() + otherBounds.getHeight()) / 2
                    - Math.abs(myCy - otCy);

            if (overlapX <= 0 || overlapY <= 0)
                continue;

            // Resolve along the axis of minimum penetration — move only the local player
            if (overlapX < overlapY) {
                double pushDir = (myCx < otCx) ? -overlapX : overlapX;
                player.setPosition(player.getPositionX() + pushDir, player.getPositionY());
            } else {
                double pushDir = (myCy < otCy) ? -overlapY : overlapY;
                player.setPosition(player.getPositionX(), player.getPositionY() + pushDir);
            }

            // Recalculate bounds after resolution for subsequent iterations
            myBounds = player.getCollisionBounds();
        }
    }

    public static void checkCollisions(double deltaTime, Villager player, Farm farm,
            ArrayList<Nest> nests) {

        double prevX = player.getPositionX() - player.getVelocityX() * deltaTime;
        double prevY = player.getPositionY() - player.getVelocityY() * deltaTime;

        Rectangle2D bounds = player.getCollisionBounds();

        double px = player.getPositionX();
        double py = player.getPositionY();
        boolean clampedX = false, clampedY = false;
        if (px < 0) {
            px = 0;
            clampedX = true;
        }
        if (py < 0) {
            py = 0;
            clampedY = true;
        }
        if (px + bounds.getWidth() > farm.getWidth()) {
            px = farm.getWidth() - bounds.getWidth();
            clampedX = true;
        }
        if (py + bounds.getHeight() > farm.getHeight()) {
            py = farm.getHeight() - bounds.getHeight();
            clampedY = true;
        }
        if (clampedX || clampedY) {
            player.setPosition(px, py);
            bounds = player.getCollisionBounds();
        }

        for (int pass = 0; pass < 4; pass++) {
            Rectangle2D hitBox = findFirstCollision(bounds, nests, farm);
            if (hitBox == null)
                break; // No collision — done

            // Compute overlap
            double overlapX = Math.min(bounds.getMaxX(), hitBox.getMaxX())
                    - Math.max(bounds.getMinX(), hitBox.getMinX());
            double overlapY = Math.min(bounds.getMaxY(), hitBox.getMaxY())
                    - Math.max(bounds.getMinY(), hitBox.getMinY());

            // Determine push direction based on player center vs obstacle center
            double playerCx = bounds.getMinX() + bounds.getWidth() / 2;
            double playerCy = bounds.getMinY() + bounds.getHeight() / 2;
            double obsCx = hitBox.getMinX() + hitBox.getWidth() / 2;
            double obsCy = hitBox.getMinY() + hitBox.getHeight() / 2;

            if (overlapX < overlapY) {
                // Push out horizontally
                double pushDir = (playerCx < obsCx) ? -overlapX : overlapX;
                player.setPosition(player.getPositionX() + pushDir, player.getPositionY());
            } else {
                // Push out vertically
                double pushDir = (playerCy < obsCy) ? -overlapY : overlapY;
                player.setPosition(player.getPositionX(), player.getPositionY() + pushDir);
            }
            bounds = player.getCollisionBounds();
        }
    }

    private static Rectangle2D findFirstCollision(Rectangle2D bounds, ArrayList<Nest> nests, Farm farm) {
        if (nests != null) {
            for (Nest nest : nests) {
                if (nest.getCollisionBounds().intersects(bounds)) {
                    return nest.getCollisionBounds();
                }
            }
        }

        Obstacle[][] grid = farm.getObstacleGrid();
        if (grid != null) {
            for (int r = 0; r < grid.length; r++) {
                for (int c = 0; c < grid[r].length; c++) {
                    if (grid[r][c] != null && grid[r][c].getCollide() && grid[r][c].intersects(bounds)) {
                        return grid[r][c].getCollisionBounds();
                    }
                }
            }
        }

        if (farm.getHorizontalWallUpper() != null) {
            for (Obstacle obs : farm.getHorizontalWallUpper())
                if (obs.getCollide() && obs.intersects(bounds))
                    return obs.getCollisionBounds();
        }
        if (farm.getHorizontalWallLower() != null) {
            for (Obstacle obs : farm.getHorizontalWallLower())
                if (obs.getCollide() && obs.intersects(bounds))
                    return obs.getCollisionBounds();
        }
        if (farm.getVerticalWallLeft() != null) {
            for (Obstacle obs : farm.getVerticalWallLeft())
                if (obs.getCollide() && obs.intersects(bounds))
                    return obs.getCollisionBounds();
        }
        if (farm.getVerticalWallRight() != null) {
            for (Obstacle obs : farm.getVerticalWallRight())
                if (obs.getCollide() && obs.intersects(bounds))
                    return obs.getCollisionBounds();
        }

        return null;
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

    public static void checkEggPickup(Villager player, ArrayList<Egg> eggs) {

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

    public static void checkNestDelivery(Villager player, ArrayList<Nest> nests) {

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
                        nest.addEggReturned();
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

        // check if all eggs are returned (fix for winScene null check)
        if (eggs == null || eggs.isEmpty()) {
            return false;
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

}
