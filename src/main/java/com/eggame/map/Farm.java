package com.eggame.map;

import java.util.ArrayList;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * Represents the farm world — handles rendering the static background
 * (terrain, boundaries, decorations) onto the background canvas.
 */
public class Farm {

    private final int width;
    private final int height;
    private Tile[][] mapGrid;
    private Obstacle[][] obstacleGrid;
    private ArrayList<Obstacle> verticalWallLeft;
    private ArrayList<Obstacle> verticalWallRight;
    private ArrayList<Obstacle> horizontalWallUpper;
    private ArrayList<Obstacle> horizontalWallLower;

    private static final int TILE_SIZE = 95;
    private static final int SPACING = 14; // Controls visual gap between tiles
    private static final int COLS = 22;
    private static final int ROWS = 12;
    private static final int WALL_SPACING = 4;

    public Farm(int width, int height) {
        this.width = width;
        this.height = height;
        loadMap();
    }

    private void loadMap() {
        mapGrid = new Tile[ROWS][COLS];
        obstacleGrid = new Obstacle[ROWS][COLS];
        verticalWallLeft = new ArrayList<Obstacle>();
        verticalWallRight = new ArrayList<Obstacle>();
        horizontalWallUpper = new ArrayList<Obstacle>();
        horizontalWallLower = new ArrayList<Obstacle>();

        // Systematically center the physical bounds of the entire grid within the
        // window
        double startX = (width - (COLS * TILE_SIZE + (COLS - 1) * SPACING)) / 2.0;
        double startY = (height - (ROWS * TILE_SIZE + (ROWS - 1) * SPACING)) / 2.0;

        try {

            java.io.File file = new java.io.File("src/main/java/com/eggame/map/map_layout.txt");
            java.util.Scanner scanner = new java.util.Scanner(file);

            for (int row = 0; row < ROWS && scanner.hasNextLine(); row++) {
                String line = scanner.nextLine().trim();
                String[] tokens = line.split("\\s+");
                for (int col = 0; col < COLS && col < tokens.length; col++) {
                    String tileType = tokens[col];
                    // Create new tile parsing tile string 1 to 5 into .png filename
                    Tile tile = new Tile();
                    tile.setImage("tile" + tileType + ".png");

                    // Factor in spacing dynamically across the index offsets
                    double posX = startX + col * (TILE_SIZE + SPACING);
                    double posY = startY + row * (TILE_SIZE + SPACING);

                    tile.setPosition(posX, posY);
                    mapGrid[row][col] = tile;
                }
            }
            scanner.close();

            System.out.println("[DEBUG] Map loaded successfully");
        } catch (Exception e) {
            System.out.println("[DEBUG] Failed to load map_layout.txt: " + e.getMessage());
        }

        try {
            java.io.File file = new java.io.File("src/main/java/com/eggame/map/obstacle_layout.txt");
            java.util.Scanner scanner = new java.util.Scanner(file);

            for (int row = 0; row < ROWS && scanner.hasNextLine(); row++) {
                String line = scanner.nextLine().trim();
                String[] tokens = line.split("\\s+");
                for (int col = 0; col < COLS && col < tokens.length; col++) {
                    String obstacleType = tokens[col];

                    if (obstacleType.equals("0")) {
                        continue;
                    }
                    // Create new tile parsing tile string 1 to 5 into .png filename
                    Obstacle obstacle = new Obstacle(true);
                    obstacle.setImage("obstacle" + obstacleType + ".png");
                    // Factor in spacing dynamically across the index offsets
                    double posX = startX + col * (TILE_SIZE + SPACING);
                    double posY = startY + row * (TILE_SIZE + SPACING);

                    obstacle.setPosition(posX, posY);
                    obstacleGrid[row][col] = obstacle;
                }
            }
            scanner.close();

            System.out.println("[DEBUG] Map loaded successfully");
        } catch (Exception e) {
            System.out.println("[DEBUG] Failed to load map_layout.txt: " + e.getMessage());
        }


        for (int i = 0; i < 24; i++) {
            Obstacle wall = new Obstacle(true);
            wall.setImage("wall6.png");
            wall.setPosition(0, 24 + i * (80));
            verticalWallLeft.add(wall);
        }

        for (int i = 0; i < 24; i++) {
            Obstacle wall = new Obstacle(true);
            wall.setImage("wall6.png");
            wall.setPosition(width-20, 24+ i * (80));
            verticalWallRight.add(wall);
        }

                // Create upper horizontal walls
        for (int i = 0; i < 70; i++) {
            Obstacle wall = new Obstacle(true);
            wall.setImage("wall5.png");
            wall.setPosition( i * (68), 16);
            horizontalWallUpper.add(wall);
        }

        for (int i = 0; i < 70; i++) {
            Obstacle wall = new Obstacle(true);
            wall.setImage("wall1.png");
            wall.setPosition(10 + i * (40 + WALL_SPACING), height-64);
            horizontalWallLower.add(wall);
        }

    }

    /**
     * Renders the static background onto the given GraphicsContext.
     * Called once at startup since the background doesn't change.
     *
     * @param bgGc the background canvas GraphicsContext
     */
    public void renderBackground(GraphicsContext bgGc) {
        bgGc.setFill(Color.web("#F4DEB3"));
        bgGc.fillRect(0, 0, width, height);

        // Render the tile grid
        if (mapGrid != null) {
            for (int row = 0; row < mapGrid.length; row++) {
                for (int col = 0; col < mapGrid[row].length; col++) {
                    if (mapGrid[row][col] != null) {
                        mapGrid[row][col].render(bgGc);
                    }
                }
            }
        }

        // Render static obstacles on top of the tiles
        if (obstacleGrid != null) {
            for (int row = 0; row < obstacleGrid.length; row++) {
                for (int col = 0; col < obstacleGrid[row].length; col++) {
                    if (obstacleGrid[row][col] != null) {
                        obstacleGrid[row][col].render(bgGc);
                    }
                }
            }
        }

        if (horizontalWallUpper != null) {
            for (int i = 0; i < horizontalWallUpper.size(); i++) {
                horizontalWallUpper.get(i).render(bgGc);
            }
        }

        if (horizontalWallLower != null) {
            for (int i = 0; i < horizontalWallLower.size(); i++) {
                horizontalWallLower.get(i).render(bgGc);
            }
        }

        if (verticalWallLeft != null) {
            for (int i = 0; i < verticalWallLeft.size(); i++) {
                verticalWallLeft.get(i).render(bgGc);
            }
        }

        if (verticalWallRight != null) {
            for (int i = 0; i < verticalWallRight.size(); i++) {
                verticalWallRight.get(i).render(bgGc);
            }
        }
    }

    public Obstacle[][] getObstacleGrid() {
        return obstacleGrid;
    }

    public ArrayList<Obstacle> getHorizontalWallUpper() {
        return horizontalWallUpper;
    }

    public ArrayList<Obstacle> getHorizontalWallLower() {
        return horizontalWallLower;
    }

    public ArrayList<Obstacle> getVerticalWallLeft() {
        return verticalWallLeft;
    }

    public ArrayList<Obstacle> getVerticalWallRight() {
        return verticalWallRight;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
