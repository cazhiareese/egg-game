package com.eggame.map;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * Represents the farm world — handles rendering the static background
 * (terrain, boundaries, decorations) onto the background canvas.
 */
public class Farm {

    private final int width;
    private final int height;

    public Farm(int width, int height) {
        this.width = width;
        this.height = height;
    }

    /**
     * Renders the static background onto the given GraphicsContext.
     * Called once at startup since the background doesn't change.
     *
     * @param bgGc the background canvas GraphicsContext
     */
    public void renderBackground(GraphicsContext bgGc) {
        // Farm ground base color
        bgGc.setFill(Color.rgb(124, 179, 66));  // Grass green
        bgGc.fillRect(0, 0, width, height);

        // TODO: Draw farm structures (barn, fences, paths)
        // TODO: Draw town area
        // TODO: Draw decorative elements
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
