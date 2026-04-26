package com.eggame.scene;

import java.util.ArrayList;

import com.eggame.map.Farm;

import javafx.animation.AnimationTimer;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;

/**
 * The main gameplay scene. Manages the game loop (update + render),
 * the two-layer canvas system, and all game state.
 */
public class Game {
    private static Scene gameScene;

    protected Group root;
    protected Canvas canvas;
    protected Canvas bg;

    private GraphicsContext gc;
    private GraphicsContext bgGc;

    private AnimationTimer gameLoop;
    private ArrayList<String> input;
    private Farm farm;

    public static final int WINDOW_WIDTH = 1200;
    public static final int WINDOW_HEIGHT = 700;

    public Game() {
        this.root = new Group();
        this.canvas = new Canvas(WINDOW_WIDTH, WINDOW_HEIGHT);
        this.bg = new Canvas(WINDOW_WIDTH, WINDOW_HEIGHT);

        this.gc = canvas.getGraphicsContext2D();
        this.bgGc = bg.getGraphicsContext2D();

        this.root.getChildren().addAll(this.bg, this.canvas);
        Game.gameScene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);
    }

    /**
     * Starts the game loop. Must be called after input is bound.
     *
     * @param input the shared input list from SceneManager
     */
    public void start(ArrayList<String> input) {
        this.input = input;

        // Create the farm and draw the background once
        this.farm = new Farm(WINDOW_WIDTH, WINDOW_HEIGHT);
        farm.renderBackground(bgGc);

        // Start the game loop
        this.gameLoop = new AnimationTimer() {
            private long lastTime = 0;

            @Override
            public void handle(long now) {
                // Calculate delta time in seconds
                if (lastTime == 0) {
                    lastTime = now;
                    return;
                }
                double deltaTime = (now - lastTime) / 1_000_000_000.0;
                lastTime = now;

                update(deltaTime);
                render();
            }
        };
        gameLoop.start();
    }

    /**
     * Stops the game loop.
     */
    public void stop() {
        if (gameLoop != null) {
            gameLoop.stop();
        }
    }

    /**
     * Called every frame — update game logic here.
     *
     * @param deltaTime seconds since last frame
     */
    private void update(double deltaTime) {
        // TODO: Update villager movement based on input
        // TODO: Check collisions (egg pickup, nest delivery, wall blocking)
        // TODO: Update game state (score, timer, etc.)
    }

    /**
     * Called every frame — draw all entities here.
     * Clears the foreground canvas and redraws everything.
     */
    private void render() {
        // Clear the foreground canvas
        gc.clearRect(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);

        // TODO: Draw eggs
        // TODO: Draw nests
        // TODO: Draw villagers
        // TODO: Draw walls
        // TODO: Draw HUD (score, timer)
    }

    public Farm getFarm() {
        return farm;
    }

    // --- Accessors ---

    public GraphicsContext getGc() {
        return gc;
    }

    public GraphicsContext getBgGc() {
        return bgGc;
    }

    public ArrayList<String> getInput() {
        return input;
    }

    public static Scene getScene() {
        return gameScene;
    }
}
