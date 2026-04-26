package com.eggame.scene;

import java.util.ArrayList;

import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

/**
 * Manages all scenes in the application (splash, menu, gameplay, etc.)
 * and handles scene transitions and input forwarding.
 */
public class SceneManager {

    private final Stage stage;
    private final ArrayList<String> input;

    private Game game;

    /**
     * Creates a new SceneManager bound to the primary stage.
     *
     * @param stage the application's primary stage
     */
    public SceneManager(Stage stage) {
        this.stage = stage;
        this.input = new ArrayList<>();
    }

    /**
     * Sets up and switches to the gameplay scene.
     */
    public void switchToGame() {
        this.game = new Game();
        Scene gameScene = Game.getScene();
        setScene(gameScene);
        bindInput(gameScene);
    }

    // Add more scene switches here as needed, e.g.:
    // public void switchToMainMenu() { ... }
    // public void switchToGameOver() { ... }
    // public void switchToSplash() { ... }

    /**
     * Returns the list of currently pressed key codes.
     *
     * @return the active input list
     */
    public ArrayList<String> getInput() {
        return input;
    }

    /**
     * Returns the current Game instance, or null if not in gameplay.
     *
     * @return the Game instance
     */
    public Game getGame() {
        return game;
    }

    // --- Private helpers ---

    /**
     * Applies a scene to the stage.
     */
    private void setScene(Scene scene) {
        stage.setScene(scene);
    }

    /**
     * Binds key pressed/released handlers to track input on the given scene.
     */
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
}
