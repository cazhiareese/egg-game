package com.eggame.map;

import java.io.BufferedReader;
import java.io.InputStreamReader;

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
    private static final int TILE_SIZE = 95;
    private static final int SPACING = 14; // Controls visual gap between tiles
    private static final int COLS = 11;
    private static final int ROWS = 6;

    public Farm(int width, int height) {
        this.width = width;
        this.height = height;
        loadMap();
    }

    private void loadMap() {
        mapGrid = new Tile[ROWS][COLS];

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

    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
