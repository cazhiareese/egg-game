package com.eggame.scene;

import java.util.ArrayList;

import com.eggame.network.GameServer;

import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

public class SceneManager {
    private GameServer activeServer = null;
    private Thread serverThread = null;
    private final Stage stage;
    private final ArrayList<String> input;
    private Game game;
    private final AvatarState avatarState;

    public SceneManager(Stage stage) {
        this.stage = stage;
        this.input = new ArrayList<>();
        this.avatarState = new AvatarState();
    }

    public void switchToGame(String serverIp, int playerId) {
        // Stop any existing game loop
        if (this.game != null) {
            this.game.stop();
        }

        this.game = new Game(this);
        Scene gameScene = Game.getScene();

        setScene(gameScene);
        bindInput(gameScene);

        // Start the game loop and pass the playerId
        game.start(input, serverIp, playerId, avatarState);
    }

    public void switchToLobby(String playerName, boolean isHost, String ip) {
        // Stop any existing game loop
        if (this.game != null) {
            this.game.stop();
        }

        Lobby lobby = new Lobby(this, playerName, isHost, ip);
        setScene(lobby.getScene());
    }

    public void switchToMainMenu() {
        if (this.game != null) {
            this.game.stop();
            this.game = null;
        }

        // Shut down any active server so the port is freed for a new game
        shutdownServer();

        MainMenu menu = new MainMenu(this);
        Scene menuScene = menu.getScene();

        setScene(menuScene);
    }

    public void switchToInstructions() {
        if (this.game != null) {
            this.game.stop();
            this.game = null;
        }

        Instructions instructions = new Instructions(this);
        Scene instructionsScene = instructions.getScene();

        setScene(instructionsScene);
    }

    public void switchToCustomize() {
        if (this.game != null) {
            this.game.stop();
            this.game = null;
        }

        CustomizeMenu customizeMenu = new CustomizeMenu(this);
        Scene customizeScene = customizeMenu.getScene();

        setScene(customizeScene);
    }

    public void switchToWinScene(String header, String scoreboard) {
        WinScene winScene = new WinScene(this, header, scoreboard);
        setScene(winScene.getScene());
    }

    public void resetAndResumeGame() {
        if (this.game != null) {
            setScene(Game.getScene());
            bindInput(Game.getScene());
            this.game.resetGame();
        }
    }

    // public void switchToSplash() { ... }

    public AvatarState getAvatarState() {
        return avatarState;
    }

    public ArrayList<String> getInput() {
        return input;
    }

    public Game getGame() {
        return game;
    }

    private void setScene(Scene scene) {
        stage.setScene(scene);
    }

    private void bindInput(Scene scene) {
        scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
            public void handle(KeyEvent e) {
                String code = e.getCode().toString();
                if (!input.contains(code)) {
                    input.add(code);
                }
            }
        });

        scene.setOnKeyReleased(new EventHandler<KeyEvent>() {
            public void handle(KeyEvent e) {
                String code = e.getCode().toString();
                input.remove(code);
            }
        });
    }

    public void setActiveServer(GameServer server, Thread thread) {
        this.activeServer = server;
        this.serverThread = thread;
    }

    public void shutdownServer() {
        if (activeServer != null) {
            activeServer.shutdown();
            activeServer = null;
        }
        if (serverThread != null) {
            serverThread.interrupt();
            serverThread = null;
        }
    }
}
