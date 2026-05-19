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
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.KeyCode;
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

    // ── Chat ─────────────────────────────────────────────────────────────
    /** A single chat entry — text only, kept forever. */
    private static class ChatMessage {
        final String text;

        ChatMessage(String text) {
            this.text = text;
        }
    }

    /** Full persistent message history (capped at MAX_HISTORY). */
    private final ArrayList<ChatMessage> chatLog = new ArrayList<>();
    private static final int MAX_HISTORY = 200;

    // Side-panel geometry
    private static final double PANEL_WIDTH = 280;
    private static final double PANEL_X = WINDOW_WIDTH - PANEL_WIDTH;
    private static final double PANEL_TOP = 80; // below the timer
    private static final double PANEL_BOTTOM = WINDOW_HEIGHT - 70; // above tray
    private static final double LINE_H = 20;
    private static final double PANEL_PAD = 8;

    /** Whether the chat side-panel is currently open. */
    private boolean chatPanelOpen = false;
    /** Whether the text-input bar is currently accepting keystrokes. */
    private boolean chatInputOpen = false;

    /** JavaFX text field used as the chat input bar. */
    private TextField chatInput;
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

        // ── Chat input field (sits at the bottom of the side panel) ──────
        chatInput = new TextField();
        chatInput.setPromptText("Say something…");
        chatInput.setStyle(
                "-fx-background-color: rgba(20,10,5,0.85);" +
                        "-fx-text-fill: #FFF7D6;" +
                        "-fx-prompt-text-fill: #a0917a;" +
                        "-fx-font-size: 13px;" +
                        "-fx-background-radius: 0 0 6 6;" +
                        "-fx-border-color: #60312B;" +
                        "-fx-border-radius: 0 0 6 6;" +
                        "-fx-border-width: 1px;" +
                        "-fx-padding: 4 8 4 8;");
        chatInput.setPrefWidth(PANEL_WIDTH);
        chatInput.setLayoutX(PANEL_X);
        chatInput.setLayoutY(PANEL_BOTTOM);
        chatInput.setVisible(false);
        root.getChildren().add(chatInput);
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

        // ── Wire chat keyboard shortcuts ─────────────────────────────────
        Game.gameScene.setOnKeyPressed(e -> {
            // T → toggle the chat panel open/closed
            if (e.getCode() == KeyCode.T && !chatInputOpen) {
                toggleChatPanel();
                e.consume();
                return;
            }
            // Enter → open input if panel is open; send if input is already open
            if (e.getCode() == KeyCode.ENTER) {
                if (chatPanelOpen && !chatInputOpen) {
                    openChatInput();
                } else if (chatInputOpen) {
                    sendChat();
                }
                e.consume();
                return;
            }
            // Esc → close input (but keep panel); second Esc closes panel
            if (e.getCode() == KeyCode.ESCAPE) {
                if (chatInputOpen) {
                    closeChatInput();
                } else if (chatPanelOpen) {
                    chatPanelOpen = false;
                }
                e.consume();
                return;
            }
            // Suppress movement keys while the chat input is open
            if (!chatInputOpen) {
                String key = e.getCode().name();
                if (!input.contains(key))
                    input.add(key);
            }
        });
        Game.gameScene.setOnKeyReleased(e -> {
            if (!chatInputOpen) {
                input.remove(e.getCode().name());
            }
        });

        // Create local villager at the correct index
        String playerName = "Player " + (localPlayerId + 1);
        Villager localVillager = new Villager(playerName);
        localVillager.setPlayerId(localPlayerId);
        if (localAvatarState != null) {
            localVillager.setAvatar(localAvatarState.getHeadIndex(), localAvatarState.getHatIndex());
        }

        // Spawn nests and eggs first so we can validate the player spawn position
        Logic.initRound(nests, eggs, farm, WINDOW_WIDTH * 2, WINDOW_HEIGHT * 2);

        // Find a safe spawn that doesn't overlap nests or obstacles
        double preferredX = (WINDOW_WIDTH * 2) / 2.0 + localPlayerId * 150;
        double preferredY = (WINDOW_HEIGHT * 2) / 2.0;
        double[] safe = Logic.findSafeSpawn(preferredX, preferredY, nests, farm);
        localVillager.setPosition(safe[0], safe[1]);

        // Pad list so this player lands at the right index
        while (villagers.size() < localPlayerId) {
            villagers.add(new Villager("Remote"));
        }
        villagers.add(localVillager);

        mainCamera.follow(localVillager.getPositionX(), localVillager.getPositionY(), farm);
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

        // disconnect and send leave
        if (client != null) {
            client.disconnect();
            client = null;
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

        // Run player-vs-player collision detection locally so bumping feels responsive
        Logic.checkPlayerCollisions(localPlayer, villagers);

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
            // Drain incoming chat messages and append them to the log
            for (String msg : client.getLatestChatMessages()) {
                addChatMessage(msg);
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

        // Recount returned eggs per nest and update nest images
        for (Nest nest : nests) {
            int count = 0;
            for (Egg egg : eggs) {
                if (egg.isReturnedToNest() && egg.getFromNest() == nest.getCode()) {
                    count++;
                }
            }
            if (count != nest.getEggsReturned()) {
                nest.setEggsReturned(count);
            }
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
        this.renderChat();
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
        if (player.getEggsReturned() > 1 || player.getEggsReturned() == 0) {
            String info = "eggs delivered";
            gc.strokeText(info, 280, WINDOW_HEIGHT - 32);
            gc.fillText(info, 280, WINDOW_HEIGHT - 32);
        } else {
            String info = "egg delivered";
            gc.strokeText(info, 280, WINDOW_HEIGHT - 32);
            gc.fillText(info, 280, WINDOW_HEIGHT - 32);
        }
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
        eggs.clear();
        nests.clear();

        // Re-initialize eggs/nests first so we can validate spawn position
        Logic.initRound(nests, eggs, farm, WINDOW_WIDTH * 2, WINDOW_HEIGHT * 2);

        String playerName = "Player " + (localPlayerId + 1);
        Villager resetPlayer = new Villager(playerName);
        resetPlayer.setPlayerId(localPlayerId);

        // Find a safe spawn position
        double preferredX = (WINDOW_WIDTH * 2) / 2.0 + localPlayerId * 150;
        double preferredY = (WINDOW_HEIGHT * 2) / 2.0;
        double[] safe = Logic.findSafeSpawn(preferredX, preferredY, nests, farm);
        resetPlayer.setPosition(safe[0], safe[1]);

        // set customized avatar for this player
        if (localAvatarState != null) {
            resetPlayer.setAvatar(localAvatarState.getHeadIndex(), localAvatarState.getHatIndex());
        }
        while (villagers.size() < localPlayerId) {
            villagers.add(new Villager("Remote"));
        }
        villagers.add(resetPlayer);

        // send reset
        waitingForServerReset = true;
        if (client != null) {
            client.sendReset();
        }
    }

    /**
     * Draws the persistent chat side-panel on the right edge of the screen.
     *
     * When the panel is closed only a small "[ T ] Chat" tab is drawn.
     * When open, the full scrolling history is shown with the newest message
     * pinned to the bottom. The text-input bar sits below the history area.
     */
    private void renderChat() {
        gc.save();
        gc.setTextAlign(TextAlignment.LEFT);

        if (!chatPanelOpen) {
            // ── Collapsed: draw a small tab ──────────────────────────────
            double tabH = 28;
            double tabY = PANEL_TOP;
            gc.setGlobalAlpha(0.75);
            gc.setFill(Color.web("#1a0d06"));
            gc.fillRoundRect(PANEL_X - 2, tabY, PANEL_WIDTH + 2, tabH, 6, 6);
            gc.setStroke(Color.web("#60312B"));
            gc.setLineWidth(1.5);
            gc.strokeRoundRect(PANEL_X - 2, tabY, PANEL_WIDTH + 2, tabH, 6, 6);

            gc.setGlobalAlpha(0.9);
            gc.setFont(Font.font("System", 13));
            gc.setFill(Color.web("#FFF7D6"));
            String label = chatLog.isEmpty()
                    ? "Chat  [ T ]"
                    : "Chat (" + chatLog.size() + ")  [ T ]";
            gc.fillText(label, PANEL_X + PANEL_PAD, tabY + 19);

            gc.setGlobalAlpha(1.0);
            gc.restore();
            return;
        }

        // ── Open panel ───────────────────────────────────────────────────
        double inputBarH = chatInputOpen ? 30 : 0;
        double historyBottom = PANEL_BOTTOM - inputBarH;
        double historyHeight = historyBottom - PANEL_TOP;

        // Panel background
        gc.setGlobalAlpha(0.82);
        gc.setFill(Color.web("#120800"));
        gc.fillRect(PANEL_X, PANEL_TOP, PANEL_WIDTH, historyHeight + inputBarH);

        // Border
        gc.setGlobalAlpha(1.0);
        gc.setStroke(Color.web("#60312B"));
        gc.setLineWidth(1.5);
        gc.strokeRect(PANEL_X, PANEL_TOP, PANEL_WIDTH, historyHeight + inputBarH);

        // Header bar
        gc.setGlobalAlpha(0.95);
        gc.setFill(Color.web("#2a1208"));
        gc.fillRect(PANEL_X, PANEL_TOP, PANEL_WIDTH, 24);
        gc.setFont(Font.font("System", 12));
        gc.setFill(Color.web("#c4a060"));
        gc.fillText("Chat  —  [ T ] close  [ Esc ] cancel", PANEL_X + PANEL_PAD, PANEL_TOP + 16);

        // ── Message history ──────────────────────────────────────────────
        // How many lines fit in the history area (minus the 24-px header)
        double msgAreaTop = PANEL_TOP + 24 + 4;
        double msgAreaBottom = historyBottom - 4;
        double msgAreaH = msgAreaBottom - msgAreaTop;
        int maxVisible = (int) (msgAreaH / LINE_H);

        // Show the most-recent maxVisible messages
        int start = Math.max(0, chatLog.size() - maxVisible);
        gc.setFont(Font.font("System", 13));

        for (int i = start; i < chatLog.size(); i++) {
            double y = msgAreaTop + (i - start) * LINE_H + LINE_H - 4;
            String text = chatLog.get(i).text;

            // Alternate row tinting
            if ((i % 2) == 0) {
                gc.setGlobalAlpha(0.12);
                gc.setFill(Color.WHITE);
                gc.fillRect(PANEL_X + 1, y - LINE_H + 4, PANEL_WIDTH - 2, LINE_H);
            }

            // Truncate long lines to fit the panel width
            String display = text;
            while (display.length() > 1 && display.length() * 7.2 > PANEL_WIDTH - PANEL_PAD * 2) {
                display = display.substring(0, display.length() - 1);
            }
            if (!display.equals(text))
                display += "\u2026";

            gc.setGlobalAlpha(0.95);
            gc.setFill(Color.web("#FFF7D6"));
            gc.fillText(display, PANEL_X + PANEL_PAD, y);
        }

        // If no messages yet
        if (chatLog.isEmpty()) {
            gc.setGlobalAlpha(0.45);
            gc.setFont(Font.font("System", 12));
            gc.setFill(Color.web("#a0917a"));
            gc.fillText("No messages yet…", PANEL_X + PANEL_PAD, msgAreaTop + LINE_H);
        }

        // ── Footer hint (when input is closed) ───────────────────────────
        if (!chatInputOpen) {
            gc.setGlobalAlpha(0.6);
            gc.setFill(Color.web("#2a1208"));
            gc.fillRect(PANEL_X, historyBottom, PANEL_WIDTH, 22);
            gc.setFont(Font.font("System", 11));
            gc.setFill(Color.web("#c4a060"));
            gc.fillText("[ Enter ] to type a message", PANEL_X + PANEL_PAD, historyBottom + 15);
        }

        gc.setGlobalAlpha(1.0);
        gc.restore();
    }

    // ── Chat helpers ───────────────────────────────────────────────────────────

    private void toggleChatPanel() {
        chatPanelOpen = !chatPanelOpen;
        if (!chatPanelOpen && chatInputOpen) {
            closeChatInput();
        }
    }

    private void openChatInput() {
        chatInputOpen = true;
        input.clear();
        chatInput.setVisible(true);
        chatInput.requestFocus();
    }

    private void closeChatInput() {
        chatInputOpen = false;
        chatInput.setVisible(false);
        chatInput.clear();
        canvas.requestFocus();
    }

    private void sendChat() {
        String text = chatInput.getText().trim();
        if (!text.isEmpty() && client != null) {
            client.sendChat(text);
            // No local echo — the server broadcasts back to all clients including us
        }
        closeChatInput();
    }

    private void addChatMessage(String text) {
        chatLog.add(new ChatMessage(text));
        if (chatLog.size() > MAX_HISTORY) {
            chatLog.remove(0);
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
