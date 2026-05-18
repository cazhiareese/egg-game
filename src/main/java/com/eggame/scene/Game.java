package com.eggame.scene;

import java.util.ArrayList;
import java.util.Optional;

import com.eggame.entities.Camera;
import com.eggame.entities.Egg;
import com.eggame.entities.Nest;
import com.eggame.entities.Villager;
import com.eggame.map.Farm;
import com.eggame.network.GameClient;
import com.eggame.rules.Logic;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.control.TextInputDialog;
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
    private double timeRemaining = GAME_DURATION;

    private GameClient client;
    private int localPlayerId;
    private AvatarState localAvatarState;
    private boolean waitingForServerReset = false; // flag for server reset

    private SceneManager sceneManager;

    public Game(SceneManager sceneManager) {
        this.sceneManager = sceneManager;
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

    public void start(ArrayList<String> input, AvatarState avatarState) {
        this.input = input;
        this.localAvatarState = avatarState;

        // Create the farm and draw the background once
        this.farm = new Farm(WINDOW_WIDTH * 2, WINDOW_HEIGHT * 2);
        // farm.renderBackground(bgGc);

        // Initialize entity lists
        this.villagers = new ArrayList<Villager>();
        this.eggs = new ArrayList<Egg>();
        this.nests = new ArrayList<Nest>();
        this.mainCamera = new Camera(WINDOW_WIDTH, WINDOW_HEIGHT);

        // Connect to server first to get player ID
        try {
            TextInputDialog dialog = new TextInputDialog("127.0.0.1");
            dialog.setTitle("Connect to Server");
            dialog.setHeaderText("Enter the host's IP address:");
            Optional<String> result = dialog.showAndWait();
            String serverIP = result.orElse("127.0.0.1");
            client = new GameClient(serverIP, 9876);
            localPlayerId = client.join("Player");
            Thread receiveThread = new Thread(() -> client.run());
            receiveThread.setDaemon(true);
            receiveThread.start();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Create local villager at the correct index
        String playerName = "Player " + (localPlayerId + 1);
        Villager localVillager = new Villager(playerName);
        localVillager.setPosition(WINDOW_WIDTH / 2, WINDOW_HEIGHT / 2);
        localVillager.setPlayerId(localPlayerId);
        if (localAvatarState != null) {
            localVillager.setAvatar(localAvatarState.getHeadIndex(), localAvatarState.getHatIndex());
        }

        // Pad list so this player lands at the right index
        while (villagers.size() < localPlayerId) {
            villagers.add(new Villager("Remote"));
        }
        villagers.add(localVillager);

        mainCamera.follow(localVillager.getPositionX(), localVillager.getPositionY(), farm);

        // Spawn nests and eggs for this round
        Logic.initRound(nests, eggs, farm, WINDOW_WIDTH * 2, WINDOW_HEIGHT * 2);
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

        Villager localPlayer = villagers.get(localPlayerId);
        Logic.handleInput(deltaTime, localPlayer, input);

        // Run collision detection locally so walls feel responsive
        Logic.checkCollisions(deltaTime, localPlayer, farm, nests);

        // Run egg pickup locally so tray UI updates immediately
        Logic.checkEggPickup(localPlayer, eggs);

        // Run nest delivery locally so eggs can be returned
        Logic.checkNestDelivery(localPlayer, nests);

        if (client != null) {
            client.sendPlayerState(localPlayer.getPositionX(),
                    localPlayer.getPositionY(),
                    localPlayer.getVelocityX(),
                    localPlayer.getVelocityY(),
                    localPlayer.getHeadIndex(),
                    localPlayer.getHatIndex());
        }

        if (client != null) {
            String state = client.getLatestGameState();
            if (state != null) {
                applyGameState(state);
            }
        }

        mainCamera.follow(localPlayer.getPositionX(), localPlayer.getPositionY(), farm);

        // add delay to prevent win/lose Scene from popping up immediately after reset
        if (waitingForServerReset && timeRemaining > 110) {
            waitingForServerReset = false;
        }

        if (!waitingForServerReset && Logic.isRoundOver(eggs, nests, timeRemaining)) {
            gameState = GameState.ROUND_OVER;
            showWinScene();
        }

    }

    private void applyGameState(String state) {
        String[] parts = state.split("\\|");
        // Format:
        // GAME_STATE|playerCount|timer|p0x|p0y|p0vx|p0vy|p0returned|...|egg0col|egg0ret|...

        int idx = 1;
        int playerCount = Integer.parseInt(parts[idx++]);
        timeRemaining = Double.parseDouble(parts[idx++]);

        // Update each player's state
        for (int i = 0; i < playerCount; i++) {
            double px = Double.parseDouble(parts[idx++]);
            double py = Double.parseDouble(parts[idx++]);
            double vx = Double.parseDouble(parts[idx++]);
            double vy = Double.parseDouble(parts[idx++]);
            int returned = Integer.parseInt(parts[idx++]);
            int headIdx = Integer.parseInt(parts[idx++]);
            int hatIdx = Integer.parseInt(parts[idx++]);

            // Skip local player — we use our own local position to avoid jitter
            if (i == localPlayerId)
                continue;

            // Create remote villager if we haven't seen them yet
            while (villagers.size() <= i) {
                Villager remote = new Villager("Player " + i);
                villagers.add(remote);
            }

            Villager v = villagers.get(i);
            if ("Remote".equals(v.getName())) {
                v = new Villager("Player " + i);
                villagers.set(i, v);
            }

            v.setPosition(px, py);
            v.setVelocity(vx, vy);
            v.setEggsReturned(returned);
            if (v.getHeadIndex() != headIdx || v.getHatIndex() != hatIdx) {
                v.setAvatar(headIdx, hatIdx);
            }
        }

        // Update egg states from server — only upgrade, never downgrade
        // (prevents stale server broadcasts from undoing local pickups)
        for (int i = 0; i < eggs.size() && idx + 1 < parts.length; i++) {
            boolean collected = parts[idx++].equals("1");
            boolean returned = parts[idx++].equals("1");
            if (collected)
                eggs.get(i).setCollected(true);
            if (returned)
                eggs.get(i).setReturnedToNest(true);
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
            if ("Remote".equals(villager.getName())) {
                continue;
            }
            villager.render(gc);
        }

        gc.restore(); // reset transform back to identity for next frame
        this.showTimer();
        this.showDetails();
        this.showTray();
    }

    private void showTray() {
        Villager player = villagers.get(localPlayerId);

        double trayWidth = 260;
        double startX = WINDOW_WIDTH - trayWidth - 12;

        gc.setLineWidth(12);
        gc.setFill(Color.web("#C48C47"));
        gc.setStroke(Color.web("#60312B")); // Set outline color
        gc.strokeRoundRect(startX, WINDOW_HEIGHT - 60, trayWidth, 48, 32, 32);
        gc.fillRoundRect(startX, WINDOW_HEIGHT - 60, trayWidth, 48, 32, 32);

        ArrayList<Egg> trayEggs = player.getEggTray().getEggs();
        for (int i = 0; i < trayEggs.size(); i++) {
            Image eggImage = trayEggs.get(i).getImage();
            if (eggImage != null) {
                // draw each egg with a little bit of spacing
                gc.drawImage(eggImage, startX + 27 + (i * 44), WINDOW_HEIGHT - 51, 30, 30);
            }
        }
    }

    private void showDetails() {
        Villager player = villagers.get(localPlayerId);

        gc.setLineWidth(12);
        gc.setFill(Color.web("#C48C47"));
        gc.setStroke(Color.web("#60312B")); // Set outline color
        gc.strokeRoundRect(12, WINDOW_HEIGHT - 60, 380, 48, 32, 32);
        gc.fillRoundRect(12, WINDOW_HEIGHT - 60, 380, 48, 32, 32);

        // Calculate placement based on eggs returned vs all players
        int myReturned = player.getEggsReturned();
        int rank = 1;
        for (Villager v : villagers) {
            if (v != player && v.getEggsReturned() > myReturned) {
                rank++;
            }
        }
        String placement;
        switch (rank) {
            case 1:
                placement = "1st";
                break;
            case 2:
                placement = "2nd";
                break;
            case 3:
                placement = "3rd";
                break;
            default:
                placement = rank + "th";
                break;
        }
        gc.setFill(Color.web("#FFF7D6"));
        gc.setFont(placingFont);
        gc.strokeText(placement, 80, WINDOW_HEIGHT - 32);
        gc.fillText(placement, 80, WINDOW_HEIGHT - 32);

        // this handles the number of eggs this will change based around the number of
        // eggs returned
        gc.setLineWidth(6);
        String returned = String.valueOf(player.getEggsReturned());
        gc.setFont(detailsFont);
        gc.strokeText(returned, 172, WINDOW_HEIGHT - 32);
        gc.fillText(returned, 172, WINDOW_HEIGHT - 32);

        String info = "eggs delivered";
        gc.strokeText(info, 280, WINDOW_HEIGHT - 32);
        gc.fillText(info, 280, WINDOW_HEIGHT - 32);
    }

    // Work in progress
    private void showTimer() {
        int mins = (int) timeRemaining / 60, secs = (int) timeRemaining % 60;
        String timeStr = String.format("%d:%02d", mins, secs);
        gc.setFont(timerFont);
        // Color may change depending on the time left
        gc.setFill((mins) * 60 + secs < 31 ? Color.web("#bc6262") : Color.web("#FFF7D6"));
        gc.setStroke(Color.web("#60312B"));
        gc.setLineWidth(8);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.strokeText(timeStr, WINDOW_WIDTH / 2.0, 72);

        gc.fillText(timeStr, WINDOW_WIDTH / 2.0, 72);
    }

    private void showWinScene() {
        Villager localPlayer = villagers.get(localPlayerId);
        int myReturned = localPlayer.getEggsReturned();

        // Find the winner (player with most eggs returned)
        Villager winner = villagers.get(0);
        for (Villager v : villagers) {
            if (v.getEggsReturned() > winner.getEggsReturned()) {
                winner = v;
            }
        }

        boolean iWon = (winner == localPlayer);
        String headerText = iWon ? "You Win!" : winner.getName() + " Wins!";

        // Build scoreboard
        StringBuilder sb = new StringBuilder();
        sb.append("Your Eggs Returned: ").append(myReturned).append("\n");
        if (villagers.size() > 1) {
            // Sort by eggs returned (descending) for display
            ArrayList<Villager> sorted = new ArrayList<>(villagers);
            sorted.sort((a, b) -> b.getEggsReturned() - a.getEggsReturned());
            for (int i = 0; i < sorted.size(); i++) {
                Villager v = sorted.get(i);
                String label = (v == localPlayer) ? v.getName() + " (You)" : v.getName();
                sb.append((i + 1)).append(". ").append(label)
                        .append(" — ").append(v.getEggsReturned()).append(" eggs\n");
            }
        }

        Platform.runLater(() -> {
            sceneManager.switchToWinScene(headerText, sb.toString());
        });
    }

    public void resetGame() {
        gameState = GameState.PLAYING;
        timeRemaining = GAME_DURATION;

        input.clear();

        villagers.clear();
        String playerName = "Player " + (localPlayerId + 1);
        Villager resetPlayer = new Villager(playerName);
        resetPlayer.setPosition(WINDOW_WIDTH / 2, WINDOW_HEIGHT / 2);
        resetPlayer.setPlayerId(localPlayerId);

        // set customized avatar for this player
        if (localAvatarState != null) {
            resetPlayer.setAvatar(localAvatarState.getHeadIndex(), localAvatarState.getHatIndex());
        }
        while (villagers.size() < localPlayerId) {
            villagers.add(new Villager("Remote"));
        }
        villagers.add(resetPlayer);

        eggs.clear();
        nests.clear();

        // re-initialize your eggs/nests here
        Logic.initRound(nests, eggs, farm, WINDOW_WIDTH * 2, WINDOW_HEIGHT * 2);

        // send reset
        waitingForServerReset = true;
        if (client != null) {
            client.sendReset();
        }
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
