package com.eggame.scene;

import java.util.ArrayList;

import com.eggame.entities.Camera;
import com.eggame.entities.Egg;
import com.eggame.entities.Nest;
import com.eggame.entities.Villager;
import com.eggame.map.Farm;
import com.eggame.rules.Logic;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

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
    private Camera mainCamera;
    private ArrayList<Egg> eggs;
    private ArrayList<Nest> nests;
    private Font timerFont;
    private Font detailsFont;
    private Font placingFont;

    public static final int WINDOW_WIDTH = 1200;
    public static final int WINDOW_HEIGHT = 700;
    private static final double GAME_DURATION = 121.0;
    private double         timeRemaining = GAME_DURATION;



    public Game() {
        this.root = new Group();
        this.canvas = new Canvas(WINDOW_WIDTH, WINDOW_HEIGHT);
        this.bg = new Canvas(WINDOW_WIDTH, WINDOW_HEIGHT);

        this.gc = canvas.getGraphicsContext2D();
        this.bgGc = bg.getGraphicsContext2D();

        this.timerFont = Font.loadFont(getClass().getResourceAsStream("/com/eggame/fonts.ttf"), 64);
        this.placingFont = Font.loadFont(getClass().getResourceAsStream("/com/eggame/fonts.ttf"), 72);
        this.detailsFont = Font.loadFont(getClass().getResourceAsStream("/com/eggame/fonts.ttf"), 24);

        this.root.getChildren().add(this.canvas);
        Game.gameScene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);
    }

    public void start(ArrayList<String> input) {
        this.input = input;

        // Create the farm and draw the background once
        this.farm = new Farm(WINDOW_WIDTH*2, WINDOW_HEIGHT*2);
        // farm.renderBackground(bgGc);

        // Initialize entity lists
        this.villagers = new ArrayList<Villager>();
        this.eggs = new ArrayList<Egg>();
        this.nests = new ArrayList<Nest>();

        Villager player1 = new Villager("Player 1");
        this.mainCamera = new Camera(WINDOW_WIDTH, WINDOW_HEIGHT);
        player1.setPosition(WINDOW_WIDTH / 2, WINDOW_HEIGHT / 2);

        mainCamera.follow(player1.getPositionX(), player1.getPositionY(), farm);

        villagers.add(player1);

        // Spawn nests and eggs for this round
        Logic.initRound(nests, eggs, farm, WINDOW_WIDTH*2, WINDOW_HEIGHT*2);

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
        
        Villager player = villagers.get(0);
        this.timeRemaining -= deltaTime;

        mainCamera.follow(player.getPositionX(), player.getPositionY(), farm);

        if (Logic.isRoundOver(eggs, nests, timeRemaining)) {
            gameState = GameState.ROUND_OVER;
            showRoundOverPopup();
        }
    }

    private void render() {
        // Clear the foreground canvas
        gc.clearRect(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);

        gc.save();
        gc.translate(-mainCamera.getX(), -mainCamera.getY());

        farm.renderBackground(gc); // on the main gc, inside the camera transform


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

        gc.restore(); // reset transform back to identity for next frame
        this.showTimer();
        this.showDetails();


    }


    private void showDetails(){
        Villager player = villagers.get(0);

        gc.setLineWidth(12);
        gc.setFill(Color.web("#C48C47"));
        gc.setStroke(Color.web("#60312B")); // Set outline color
        gc.strokeRoundRect(12, WINDOW_HEIGHT-60, 380, 48, 32, 32);
        gc.fillRoundRect(12, WINDOW_HEIGHT-60,  380, 48, 32, 32);
        
        // compare which player has the most eggs returned essentially
        String placement = "1st";
        gc.setFill(Color.web("#FFF7D6"));
        gc.setFont(placingFont);
        gc.strokeText(placement, 80, WINDOW_HEIGHT-32);
        gc.fillText(placement, 80, WINDOW_HEIGHT-32);

        // this handles the number of eggs this will change based around the number of eggs returned
        gc.setLineWidth(6);
        String returned = String.valueOf(player.getEggsReturned());
        gc.setFont(detailsFont);
        gc.strokeText(returned, 172, WINDOW_HEIGHT-32);
        gc.fillText(returned, 172, WINDOW_HEIGHT-32);

        String info = "eggs delivered";
        gc.strokeText(info, 280, WINDOW_HEIGHT-32);
        gc.fillText(info, 280, WINDOW_HEIGHT-32);
    }

    // Work in progress
    private void showTimer(){
        int mins = (int) timeRemaining / 60, secs = (int) timeRemaining % 60;
        String timeStr = String.format("%d:%02d", mins, secs);
        gc.setFont(timerFont);
        // Color may change depending on the time left
        gc.setFill((mins)*60 + secs < 31 ? Color.web("#bc6262") : Color.web("#FFF7D6"));
        gc.setStroke(Color.web("#60312B")); 
        gc.setLineWidth(8);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.strokeText(timeStr, WINDOW_WIDTH / 2.0, 72);

        gc.fillText(timeStr, WINDOW_WIDTH / 2.0, 72);
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
