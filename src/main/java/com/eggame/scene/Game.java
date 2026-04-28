package com.eggame.scene;

import java.util.ArrayList;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import com.eggame.entities.Egg;
import com.eggame.entities.Nest;
import com.eggame.entities.Villager;
import com.eggame.map.Farm;
import com.eggame.rules.Logic;
import javafx.animation.AnimationTimer;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;

public class Game {
    private static Scene gameScene;
    private GameState gameState = GameState.PLAYING;

    protected Group root;
    protected Canvas canvas;
    protected Canvas bg;

    private GraphicsContext gc;
    private GraphicsContext bgGc;

    private AnimationTimer gameLoop;
    private ArrayList<String> input;
    private Farm farm;

    private ArrayList<Villager> villagers;
    private ArrayList<Egg> eggs;
    private ArrayList<Nest> nests;

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

    public void start(ArrayList<String> input) {
        this.input = input;

        // Create the farm and draw the background once
        this.farm = new Farm(WINDOW_WIDTH, WINDOW_HEIGHT);
        farm.renderBackground(bgGc);

        // Initialize entity lists
        this.villagers = new ArrayList<Villager>();
        this.eggs = new ArrayList<Egg>();
        this.nests = new ArrayList<Nest>();

        Villager player1 = new Villager("Player 1");
        player1.setPosition(WINDOW_WIDTH / 2, WINDOW_HEIGHT / 2);
        villagers.add(player1);

        // Spawn nests and eggs for this round
        Logic.initRound(nests, eggs, farm, WINDOW_WIDTH, WINDOW_HEIGHT);

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

    public void stop() {
        if (gameLoop != null) {
            gameLoop.stop();
        }
    }

    private void update(double deltaTime) {
        // Delegate all game logic to Logic
        if (gameState == GameState.ROUND_OVER)
            return;
        Logic.update(deltaTime, villagers, eggs, nests, farm, input);

        if (Logic.isRoundOver(eggs, nests)) {
            gameState = GameState.ROUND_OVER;
            showRoundOverPopup();
        }
    }

    private void render() {
        // Clear the foreground canvas
        gc.clearRect(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);

        // Draw nests
        for (Nest nest : nests) {
            nest.render(gc);
        }
        for (Egg egg : eggs) {
            if (!egg.isCollected()) {
                egg.render(gc);
            }
        }

        // Draw villagers
        for (Villager villager : villagers) {
            villager.render(gc);
        }

    }

    private void showRoundOverPopup() {
        Villager player = villagers.get(0);
        int returned = player.getEggsReturned();
        int total = eggs.size(); // The world list no longer gets depleted

        Platform.runLater(() -> {
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Results");
            alert.setHeaderText("Round Over!");
            alert.setContentText("Eggs Returned: " + returned + " / " + total);

            ButtonType playAgainButton = new ButtonType("Play Again");
            alert.getButtonTypes().setAll(playAgainButton);

            alert.showAndWait().ifPresent(type -> {
                if (type == playAgainButton) {
                    resetGame();
                }
            });
        });
    }

    private void resetGame() {
        gameState = GameState.PLAYING;

        input.clear();

        villagers.clear();
        Villager player1 = new Villager("Player 1");
        player1.setPosition(WINDOW_WIDTH / 2, WINDOW_HEIGHT / 2);
        villagers.add(player1);

        eggs.clear();
        nests.clear();

        // re-initialize your eggs/nests here
        Logic.initRound(nests, eggs, farm, WINDOW_WIDTH, WINDOW_HEIGHT);
    }

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
